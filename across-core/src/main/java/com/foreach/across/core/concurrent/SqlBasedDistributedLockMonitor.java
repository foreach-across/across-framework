package com.foreach.across.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Monitor to be executed at regular intervals that will check if the
 * registered locks are still valid.
 *
 * @author Arne Vandamme
 */
public class SqlBasedDistributedLockMonitor implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( SqlBasedDistributedLockMonitor.class );

	private final SqlBasedDistributedLockManager lockManager;
	private final Map<ActiveLock, DistributedLock> activeLocks = new HashMap<>();

	SqlBasedDistributedLockMonitor( SqlBasedDistributedLockManager lockManager ) {
		this.lockManager = lockManager;
	}

	public synchronized void addLock( String ownerId, DistributedLock lock ) {
		String lockId = lock.getLockId();
		String existingOwnerId = getOwnerForLock( lockId );

		if ( existingOwnerId != null ) {
			// This guy just had his lock stolen
			reportStolen( existingOwnerId, lockId );
		}

		activeLocks.put( new ActiveLock( ownerId, lockId ), lock );
	}

	@Override
	public void run() {
		for ( Map.Entry<ActiveLock, DistributedLock> activeLock : getActiveLocks().entrySet() ) {
			ActiveLock key = activeLock.getKey();

			// Before checking, ensure that it is still supposed to be active
			if ( activeLocks.containsKey( key ) ) {
				LOG.trace( "Verifying lock {} is still owned by {}", key.getLockId(), key.getOwnerId() );

				// If not active, report stolen
				if ( !lockManager.verifyLockedByOwner( key.getOwnerId(), key.getLockId() ) ) {
					reportStolen( key.getOwnerId(), key.getLockId() );
				}
			}
		}
	}

	private void reportStolen( String ownerId, String lockId ) {
		// Remove the lock from the monitor -  it's possible this has happened already in the meantime
		DistributedLock removedLock = removeLock( ownerId, lockId );

		if ( removedLock != null ) {
			LOG.trace( "Lock {} was supposed to be owned by {}, but it appears to be stolen",
			           lockId, ownerId );

			// Execute the stolen callback if there is one
			DistributedLock.LockStolenCallback callback = removedLock.getStolenCallback();

			if ( callback != null ) {
				callback.stolen( lockId, ownerId, removedLock );
			}
		}
	}

	public synchronized DistributedLock removeLock( String ownerId, String lockId ) {
		return activeLocks.remove( new ActiveLock( ownerId, lockId ) );
	}

	public synchronized Map<ActiveLock, DistributedLock> getActiveLocks() {
		return new HashMap<>( activeLocks );
	}

	public synchronized String getOwnerForLock( String lockId ) {
		for ( ActiveLock activeLock : activeLocks.keySet() ) {
			if ( activeLock.getLockId().equals( lockId ) ) {
				return activeLock.getOwnerId();
			}
		}

		return null;
	}

	public static class ActiveLock
	{
		private String ownerId, lockId;

		ActiveLock( String ownerId, String lockId ) {
			this.ownerId = ownerId;
			this.lockId = lockId;
		}

		public String getOwnerId() {
			return ownerId;
		}

		public String getLockId() {
			return lockId;
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			ActiveLock that = (ActiveLock) o;

			return Objects.equals( lockId, that.lockId ) && Objects.equals( ownerId, that.ownerId );
		}

		@Override
		public int hashCode() {
			return Objects.hash( ownerId, lockId );
		}
	}
}
