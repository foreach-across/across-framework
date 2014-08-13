package com.foreach.across.core.concurrent;

import com.foreach.across.core.AcrossException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementation of a {@link com.foreach.across.core.concurrent.DistributedLockManager}
 * that uses a relational dbms as
 * <p/>
 * Includes the monitor implementation that notifies the central lock repository on
 * which locks are still being used.
 */
public class SqlBasedDistributedLockManager implements DistributedLockManager
{
	private static final Logger LOG = LoggerFactory.getLogger( DistributedLockRepository.class );

	private static final String SQL_TAKE_LOCK = "UPDATE across_locks " +
			"SET owner_id = ?, created = ?, updated = ? " +
			"WHERE lock_id = ? AND owner_id IS NULL";
	private static final String SQL_SELECT_LOCK = "SELECT lock_id, owner_id, created, updated " +
			"FROM across_locks " +
			"WHERE lock_id = ?";
	private static final String SQL_INSERT_LOCK = "INSERT INTO across_locks (lock_id, owner_id, created, updated) " +
			"VALUES (?,?,?,?)";
	private static final String SQL_RELEASE_LOCK = "UPDATE across_locks " +
			"SET owner_id = NULL " +
			"WHERE lock_id = ? AND owner_id = ?";
	private static final String SQL_NOTIFY_LOCK = "UPDATE across_locks " +
			"SET updated = ? " +
			"WHERE lock_id = ? AND owner_id = ?";

	private final JdbcTemplate jdbcTemplate;

	public SqlBasedDistributedLockManager( DataSource dataSource ) {
		jdbcTemplate = new JdbcTemplate( dataSource );
	}

	@Override
	public boolean acquire( DistributedLock lock ) {
		LOG.trace( "Owner {} is trying to acquire lock {}", lock.getOwnerId(), lock.getLockId() );

		long timestamp = System.currentTimeMillis();
		int updated = jdbcTemplate.update( SQL_TAKE_LOCK, lock.getOwnerId(), timestamp, timestamp, lock.getLockId() );

		if ( updated > 1 ) {
			throw new AcrossException(
					"DistributedLockRepository table corrupt, more than one lock with id " + lock.getLockId() );
		}

		if ( updated == 1 ) {
			LOG.trace( "Owner {} directly acquired lock {}", lock.getOwnerId(), lock.getLockId() );
			return true;
		}

		try {
			LockInfo lockInfo = jdbcTemplate.queryForObject( SQL_SELECT_LOCK,
			                                                 new Object[] { lock.getLockId() },
			                                                 new LockInfoMapper() );

			// if too old, take it over, if already mine, return
			LOG.trace( "Lock {} is held by {} since {} ms", lock.getLockId(), lockInfo.getOwnerId(),
			           ( timestamp - lockInfo.getCreated() ) );
		}
		catch ( EmptyResultDataAccessException erdae ) {
			LOG.trace( "Lock {} currently does not exist, creating", lock.getLockId() );

			int created;

			try {
				created = jdbcTemplate.update( SQL_INSERT_LOCK, lock.getLockId(), lock.getOwnerId(), timestamp,
				                               timestamp );
			}
			catch ( DataAccessException dae ) {
				created = 0;
			}

			if ( created != 1 ) {
				LOG.trace( "Failed to create lock record {} - was possibly created in the meantime",
				           lock.getLockId() );
			}
			else {
				LOG.trace( "Lock {} created by {}", lock.getLockId(), lock.getOwnerId() );
				return true;
			}
		}

		try {
			Thread.sleep( 533 );
			return acquire( lock );
		}
		catch ( InterruptedException ie ) {
			return false;
		}
	}

	public boolean release( DistributedLock lock ) {
		LOG.trace( "Owner {} is releasing lock {}", lock.getOwnerId(), lock.getLockId() );
		if ( jdbcTemplate.update( SQL_RELEASE_LOCK, lock.getLockId(), lock.getOwnerId() ) != 1 ) {
			LOG.trace( "Releasing lock {} failed - possibly it was forcibly taken already" );
		}

		return true;
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
