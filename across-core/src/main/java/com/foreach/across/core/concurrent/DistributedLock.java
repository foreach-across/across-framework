package com.foreach.across.core.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Interface for custom locks that can be obtained through a
 * {@link com.foreach.across.core.concurrent.DistributedLockRepository}.
 *
 * @author Arne Vandamme
 */
public interface DistributedLock
{
	/**
	 * @return Id of the owner wanting this lock.
	 */
	String getOwnerId();

	/**
	 * @return Id of the lock that the owner is trying to obtain.
	 */
	String getLockId();

	/**
	 * Will try to obtain the lock and wait indefinitely to do so.
	 * In case of an interrupt, a {@link com.foreach.across.core.concurrent.DistributedLockWaitException}
	 * will be thrown.
	 */
	void lock();

	/**
	 * Will try to obtain the lock a single time and return immediately.
	 *
	 * @return {@code true} if the lock was acquired and {@code false} otherwise
	 */
	boolean tryLock();

	/**
	 * Will try to obtain the lock and wait for the specified amount of time to do so.
	 * In case of an interrupt, a {@link com.foreach.across.core.concurrent.DistributedLockWaitException}
	 * will be thrown.
	 *
	 * @param time the maximum time to wait for the lock
	 * @param unit the time unit of the {@code time} argument
	 * @return {@code true} if the lock was acquired and {@code false} if the waiting time elapsed before the lock was acquired
	 */
	boolean tryLock( long time, TimeUnit unit );

	/**
	 * Will release the lock.
	 */
	void unlock();
}
