package com.foreach.across.core.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link com.foreach.across.core.concurrent.DistributedLock} that implements
 * behavior most alike standard locks, in such a way that locking happens on a thread basis.
 *
 * The owner id of this lock is dynamic, based on the thread that it is operating on.
 * In most scenarios this is probably the implementation you want to use.
 *
 * For an alternative implementation and use case see the
 * {@link com.foreach.across.core.concurrent.SharedDistributedLock}.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.concurrent.SharedDistributedLock
 */
public class ThreadBasedDistributedLock implements DistributedLock
{
	private final String ownerId, lockId;
	private final DistributedLockManager lockManager;

	ThreadBasedDistributedLock( DistributedLockManager lockManager, String owner, String id ) {
		this.ownerId = owner;
		this.lockId = id;
		this.lockManager = lockManager;
	}

	@Override
	public String getOwnerId() {
		return ownerId + "[" + Thread.currentThread().getId() + "]";
	}

	@Override
	public String getLockId() {
		return lockId;
	}

	@Override
	public void lock() {
		lockManager.acquire( this );
	}

	@Override
	public boolean tryLock() {
		return lockManager.acquire( this );
	}

	@Override
	public boolean tryLock( long time, TimeUnit unit ) {
		return true;
	}

	@Override
	public void unlock() {
		lockManager.release( this );
	}
}
