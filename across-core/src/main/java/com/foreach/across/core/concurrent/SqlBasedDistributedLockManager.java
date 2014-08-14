package com.foreach.across.core.concurrent;

import com.foreach.across.core.AcrossException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a {@link com.foreach.across.core.concurrent.DistributedLockManager}
 * that uses a relational dbms as backend for synchronizing the lock access.
 * <p/>
 * Includes the monitor implementation that notifies the central lock repository on
 * which locks are still being used, as well as the cleanup thread that deletes old
 * unused locks from the database.
 */
public class SqlBasedDistributedLockManager implements DistributedLockManager
{
	private static final Logger LOG = LoggerFactory.getLogger( SqlBasedDistributedLockManager.class );

	/**
	 * Number of milliseconds between tries for acquiring a lock.
	 */
	public static final long DEFAULT_RETRY_INTERVAL = 533;

	/**
	 * Number of milliseconds between monitor runs that will update all actively held locks.
	 */
	public static final long DEFAULT_VERIFY_INTERVAL = 3000;

	/**
	 * Number of milliseconds that a held lock can go without update before another owner can steal it.
	 */
	public static final long DEFAULT_MAX_IDLE_BEFORE_STEAL = 15000;

	/**
	 * Number of milliseconds a lock record should be unlocked before it gets actually deleted from the store.
	 */
	public static final long DEFAULT_MIN_AGE_BEFORE_DELETE = 3600000;

	/**
	 * Number of milliseconds between running the database cleanup.
	 */
	public static final long DEFAULT_CLEANUP_INTERVAL = 900000;

	private static final String SQL_TAKE_LOCK = "UPDATE across_locks " +
			"SET owner_id = ?, created = ?, updated = ? " +
			"WHERE lock_id = ? AND (owner_id IS NULL OR owner_id = ?)";
	private static final String SQL_STEAL_LOCK = "UPDATE across_locks " +
			"SET owner_id = ?, created = ?, updated = ? " +
			"WHERE lock_id = ? AND (owner_id IS NULL OR (owner_id = ? AND updated = ?))";

	private static final String SQL_SELECT_LOCK = "SELECT lock_id, owner_id, created, updated " +
			"FROM across_locks " +
			"WHERE lock_id = ?";
	private static final String SQL_INSERT_LOCK = "INSERT INTO across_locks (lock_id, owner_id, created, updated) " +
			"VALUES (?,?,?,?)";
	private static final String SQL_RELEASE_LOCK = "UPDATE across_locks " +
			"SET owner_id = NULL " +
			"WHERE lock_id = ? AND owner_id = ?";
	private static final String SQL_VERIFY_LOCK = "UPDATE across_locks " +
			"SET updated = ? " +
			"WHERE lock_id = ? AND owner_id = ?";
	private static final String SQL_CLEANUP = "DELETE FROM across_locks WHERE owner_id IS NULL AND updated < ?";

	private final ScheduledExecutorService monitorThread = Executors.newSingleThreadScheduledExecutor();

	private final long retryInterval;
	private final long verifyInterval;
	private final long maxIdleBeforeSteal;
	private final long cleanupInterval;
	private final long cleanupAge;

	private final JdbcTemplate jdbcTemplate;
	private final SqlBasedDistributedLockMonitor lockMonitor;

	private boolean destroyed = false;

	public SqlBasedDistributedLockManager( DataSource dataSource ) {
		this( dataSource, DEFAULT_RETRY_INTERVAL,
		      DEFAULT_VERIFY_INTERVAL,
		      DEFAULT_MAX_IDLE_BEFORE_STEAL,
		      DEFAULT_CLEANUP_INTERVAL,
		      DEFAULT_MIN_AGE_BEFORE_DELETE );
	}

	public SqlBasedDistributedLockManager( DataSource dataSource,
	                                       long retryInterval,
	                                       long verifyInterval,
	                                       long maxIdleBeforeSteal,
	                                       long cleanupInterval,
	                                       long cleanupAge ) {
		this.retryInterval = retryInterval;
		this.verifyInterval = verifyInterval;
		this.maxIdleBeforeSteal = maxIdleBeforeSteal;
		this.cleanupInterval = cleanupInterval;
		this.cleanupAge = cleanupAge;

		jdbcTemplate = new JdbcTemplate( dataSource );
		lockMonitor = new SqlBasedDistributedLockMonitor( this );

		monitorThread.scheduleWithFixedDelay( lockMonitor, verifyInterval, verifyInterval, TimeUnit.MILLISECONDS );
		monitorThread.scheduleWithFixedDelay( new CleanupMonitor(), 0, cleanupInterval, TimeUnit.MILLISECONDS );
	}

	class CleanupMonitor implements Runnable
	{
		@Override
		public void run() {
			try {
				long cleanupStart = System.currentTimeMillis();
				long staleRecordsTimestamp = cleanupStart - cleanupAge;
				int recordsDeleted = jdbcTemplate.update( SQL_CLEANUP, staleRecordsTimestamp );

				LOG.info(
						"Deleted {} locks that have been unused for {} ms - cleanup time was {} ms, next run in {} ms",
						recordsDeleted,
						cleanupAge, System.currentTimeMillis() - cleanupStart, cleanupInterval );
			}
			catch ( Exception e ) {
				LOG.warn( "Exception trying to cleanup unused locks", e );
			}
		}
	}

	public void close() {
		LOG.trace( "Destruction of the distributed lock manager requested" );

		try {
			Map<SqlBasedDistributedLockMonitor.ActiveLock, DistributedLock> activeLocks = lockMonitor.getActiveLocks();

			LOG.info( "Destroying distributed lock manager - releasing {} held locks", activeLocks.size() );

			for ( SqlBasedDistributedLockMonitor.ActiveLock activeLock : activeLocks.keySet() ) {
				release( activeLock.getOwnerId(), activeLock.getLockId() );
			}

			monitorThread.shutdown();

			try {
				monitorThread.awaitTermination( verifyInterval * 2, TimeUnit.MILLISECONDS );
			}
			catch ( InterruptedException ie ) {
				LOG.warn( "Failed to wait for clean shutdown of lock monitor" );
			}
		}
		finally {
			destroyed = true;
		}
	}

	@Override
	public void acquire( DistributedLock lock ) {
		checkDestroyed();

		boolean acquired = tryAcquire( lock );

		try {
			while ( !acquired ) {
				Thread.sleep( retryInterval );
				acquired = tryAcquire( lock );
			}
		}
		catch ( InterruptedException ie ) {
			throw new DistributedLockWaitException( ie );
		}
	}

	@Override
	public boolean tryAcquire( DistributedLock lock, long time, TimeUnit unit ) {
		checkDestroyed();

		boolean acquired = tryAcquire( lock );

		long delay = retryInterval;
		long timeRemaining = unit.toMillis( time );

		try {
			while ( !acquired && timeRemaining > 0 ) {
				if ( timeRemaining < delay ) {
					delay = timeRemaining;
				}

				Thread.sleep( delay );
				acquired = tryAcquire( lock );

				timeRemaining -= delay;
			}
		}
		catch ( InterruptedException ie ) {
			throw new DistributedLockWaitException( ie );
		}

		return acquired;
	}

	@Override
	public boolean tryAcquire( DistributedLock lock ) {
		checkDestroyed();

		String lockId = lock.getLockId();
		String ownerId = lock.getOwnerId();

		return tryAcquire( lockId, ownerId, lock );
	}

	private boolean tryAcquire( String lockId, String ownerId, DistributedLock lock ) {
		boolean acquired = false;

		LOG.trace( "Owner {} is trying to acquire lock {}", ownerId, lockId );

		long timestamp = System.currentTimeMillis();
		int updated = jdbcTemplate.update( SQL_TAKE_LOCK, ownerId, timestamp, timestamp, lockId, ownerId );

		if ( updated > 1 ) {
			throw new AcrossException(
					"DistributedLockRepository table corrupt, more than one lock with id " + lockId );
		}

		if ( updated == 1 ) {
			LOG.trace( "Owner {} directly acquired lock {}", ownerId, lockId );
			acquired = true;
		}
		else {
			LockInfo lockInfo = getLockInfo( lockId );

			if ( lockInfo != null ) {
				if ( ownerId.equals( lockInfo.getOwnerId() ) ) {
					acquired = true;
				}
				else {
					timestamp = System.currentTimeMillis();
					long lastUpdateAge = timestamp - lockInfo.getUpdated();
					if ( lastUpdateAge > maxIdleBeforeSteal ) {
						LOG.trace( "Lock {} was last updated {} ms ago - attempting to steal the lock",
						           lockId, lastUpdateAge );
						updated = jdbcTemplate.update( SQL_STEAL_LOCK, ownerId, timestamp, timestamp, lockId,
						                               lockInfo.getOwnerId(), lockInfo.getUpdated() );

						acquired = updated == 1;
					}
					else if ( LOG.isTraceEnabled() ) {
						long duration = System.currentTimeMillis() - lockInfo.getCreated();
						LOG.trace( "Lock {} is held by {} since {} ms", lockId, lockInfo.getOwnerId(),
						           duration );
					}
				}
			}
			else {
				LOG.trace( "Lock {} currently does not exist, creating", lockId );

				int created;

				try {
					timestamp = System.currentTimeMillis();
					created = jdbcTemplate.update( SQL_INSERT_LOCK, lockId, ownerId, timestamp, timestamp );
				}
				catch ( DataAccessException dae ) {
					created = 0;
				}

				if ( created != 1 ) {
					LOG.trace( "Failed to create lock record {} - was possibly created in the meantime",
					           lockId );
				}
				else {
					LOG.trace( "Lock {} created by {}", lockId, ownerId );
					acquired = true;
				}
			}

		}

		if ( acquired ) {
			lockMonitor.addLock( ownerId, lock );
		}
		else {
			// Cleanup any stale record already, we're sure we no longer have the lock
			lockMonitor.removeLock( ownerId, lockId );
		}

		return acquired;
	}

	@Override
	public boolean isLocked( String lockId ) {
		checkDestroyed();
		return getLockOwner( lockId ) != null;
	}

	@Override
	public boolean isLockedByOwner( String ownerId, String lockId ) {
		checkDestroyed();
		return StringUtils.equals( ownerId, getLockOwner( lockId ) );
	}

	private String getLockOwner( String lockId ) {
		String ownerId = lockMonitor.getOwnerForLock( lockId );

		if ( ownerId == null ) {
			// Owner not found in current repository, dispatch to backend database
			LockInfo lockInfo = getLockInfo( lockId );

			if ( lockInfo != null ) {
				ownerId = lockInfo.getOwnerId();
			}
		}

		return ownerId;
	}

	private LockInfo getLockInfo( String lockId ) {
		try {
			return jdbcTemplate.queryForObject( SQL_SELECT_LOCK,
			                                    new Object[] { lockId },
			                                    new LockInfoMapper() );
		}
		catch ( EmptyResultDataAccessException erdae ) {
			return null;
		}
	}

	@Override
	public boolean verifyLockedByOwner( String ownerId, String lockId ) {
		checkDestroyed();
		return jdbcTemplate.update( SQL_VERIFY_LOCK, System.currentTimeMillis(), lockId, ownerId ) == 1;
	}

	@Override
	public void release( DistributedLock lock ) {
		checkDestroyed();
		release( lock.getOwnerId(), lock.getLockId() );
	}

	private void checkDestroyed() {
		if ( destroyed ) {
			throw new IllegalStateException(
					"The DistributedLockManager has been destroyed - creating locks is impossible." );
		}
	}

	private void release( String ownerId, String lockId ) {
		LOG.trace( "Owner {} is releasing lock {}", ownerId, lockId );
		lockMonitor.removeLock( ownerId, lockId );
		if ( jdbcTemplate.update( SQL_RELEASE_LOCK, lockId, ownerId ) != 1 ) {
			LOG.trace( "Releasing lock {} failed - possibly it was forcibly taken already", lockId );
		}
	}

	private static final class LockInfo
	{
		private String lockId, ownerId;
		private long created, updated;

		public String getLockId() {
			return lockId;
		}

		public void setLockId( String lockId ) {
			this.lockId = lockId;
		}

		public String getOwnerId() {
			return ownerId;
		}

		public void setOwnerId( String ownerId ) {
			this.ownerId = ownerId;
		}

		public long getCreated() {
			return created;
		}

		public void setCreated( long created ) {
			this.created = created;
		}

		public long getUpdated() {
			return updated;
		}

		public void setUpdated( long updated ) {
			this.updated = updated;
		}
	}

	private static final class LockInfoMapper implements RowMapper<LockInfo>
	{
		@Override
		public LockInfo mapRow( ResultSet rs, int rowNum ) throws SQLException {
			LockInfo lockInfo = new LockInfo();
			lockInfo.setLockId( rs.getString( "lock_id" ) );
			lockInfo.setOwnerId( rs.getString( "owner_id" ) );
			lockInfo.setCreated( rs.getLong( "created" ) );
			lockInfo.setUpdated( rs.getLong( "updated" ) );

			return lockInfo;
		}
	}
}
