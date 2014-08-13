package com.foreach.across.core.concurrent;

/**
 * Implementation of {@link com.foreach.across.core.concurrent.DistributedLock} that uses the specified
 * owner id to manage the lock.  Unlike the {@link com.foreach.across.core.concurrent.ThreadBasedDistributedLock}
 * this implementation does not take the actual thread into account when determining the owner id.
 *
 * The uniqueness of the owner id is entirely up to the end user.
 *
 * A SharedDistributedLock can be useful for situations where you want different threads to be able to acquire
 * or release the lock; eg in a master/slave setup.
 *
 * @see com.foreach.across.core.concurrent.ThreadBasedDistributedLock
 */
public class SharedDistributedLock extends ThreadBasedDistributedLock
{
	private final String ownerId;

	SharedDistributedLock( DistributedLockManager lockManager, String ownerId, String lockId ) {
		super( lockManager, ownerId, lockId );

		this.ownerId = ownerId;
	}

	@Override
	public String getOwnerId() {
		return ownerId;
	}
}
