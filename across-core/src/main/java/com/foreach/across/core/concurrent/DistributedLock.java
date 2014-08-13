package com.foreach.across.core.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Arne Vandamme
 */
public class DistributedLock implements Lock
{
	private final String ownerId, lockId;
	private final DistributedLockRepository repository;

	public DistributedLock( DistributedLockRepository repository, String owner, String id ) {
		this.ownerId = owner;
		this.lockId = id;
		this.repository = repository;
	}

	public String getOwnerId() {
		return ownerId + "[" + Thread.currentThread().getId() + "]";
	}

	public String getLockId() {
		return lockId;
	}

	@Override
	public void lock() {
		repository.acquire( this );
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		return repository.acquire( this );
	}

	@Override
	public boolean tryLock( long time, TimeUnit unit ) throws InterruptedException {
		return true;
	}

	@Override
	public void unlock() {
		repository.release( this );
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException( "newCondition() is not implemented" );
	}
}
