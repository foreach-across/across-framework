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
	 * Queries if this lock is held by anyone.
	 * <p/>
	 * <strong>Note:</strong> in a distributed context, calls to check if a lock is taken
	 * can be relatively expensive and almost as expensive as trying to acquire the lock.
	 *
	 * @return true if the lock is held by anyone
	 */
	boolean isLocked();

	/**
	 * Queries if this lock is held by the owner calling the method.
	 * Not that the concept of the *owner* depends on the DistributedLock
	 * implementation.  Some implementations might take the current thread
	 * into account while others might not.
	 * <p/>
	 * <strong>Note:</strong> in a distributed context, calls to check if a lock is taken
	 * can be relatively expensive and almost as expensive as trying to acquire the lock.
	 *
	 * @return true if the lock is held by the owner calling the method
	 * @see com.foreach.across.core.concurrent.ThreadBasedDistributedLock
	 * @see com.foreach.across.core.concurrent.SharedDistributedLock
	 */
	boolean isLockedByMe();

	/**
	 * Will release the lock.
	 */
	void unlock();

	/**
	 * Set the callback to be executed in case this lock gets stolen.
	 *
	 * @param callback Callback instance for this lock
	 * @see com.foreach.across.core.concurrent.DistributedLock.LockStolenCallback
	 */
	void setStolenCallback( LockStolenCallback callback );

	/**
	 * @return The callback attached to this lock.
	 */
	LockStolenCallback getStolenCallback();

	/**
	 * A simple callback interface that will be executed when a DistributedLock is reported stolen.
	 */
	interface LockStolenCallback
	{
		/**
		 * The callback provides the original DistributedLock instance that could possibly be
		 * used to retake the lock (implementation dependent).
		 *
		 * @param lockId If of the lock that has been stolen.
		 * @param ownerId Id of the owner the lock has been stolen from.
		 * @param lock Instance of the lock that has been stolen.
		 */
		void stolen( String lockId, String ownerId, DistributedLock lock );
	}
}
