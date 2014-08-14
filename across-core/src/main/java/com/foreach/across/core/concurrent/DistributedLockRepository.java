package com.foreach.across.core.concurrent;

/**
 * Central repository for creating distributed locks.
 *
 * @author Arne Vandamme
 */
public interface DistributedLockRepository
{
	/**
	 * Creates a new DistributedLock with a unique owner id that is safe to use in distributed and
	 * multi-threaded scenarios.  This lock instance does not require manual management of the owner id
	 * or threads trying to acquire/release the lock.
	 * <p/>
	 * The generated owner id is based on a UUID and guaranteed to be unique.
	 *
	 * @param lockId Unique id of the lock.
	 * @return DistributedLock instance.
	 * @see com.foreach.across.core.concurrent.ThreadBasedDistributedLock
	 */
	DistributedLock createLock( String lockId );

	/**
	 * Creates a new DistributedLock with a unique owner id that is safe to use in distributed and
	 * multi-threaded scenarios.  This lock instance does not require manual management of the threads
	 * trying to acquire/release the lock within a same vm.
	 * <p/>
	 * For descriptive purposes the owner name passed in will be used as prefix for the owner id.
	 * <p/>
	 * <strong>Note:</strong> in distributed contexts it is imperative (unless explicitly required) that
	 * the ownerName be different in different applications or vms, as collisions can occur with the
	 * generated owner ids otherwise.
	 *
	 * @param ownerName Name of the owner, will be used as part of the unique owner id.
	 * @param lockId    Unique id of the lock.
	 * @return DistributedLock instance.
	 * @see com.foreach.across.core.concurrent.ThreadBasedDistributedLock
	 */
	DistributedLock createLock( String ownerName, String lockId );

	/**
	 * Creates a new shared distributed lock with the specified owner id.  This lock instance
	 * will require manual management across separate threads within the same vm.
	 *
	 * @param ownerId Unique id of the owner for this lock.
	 * @param lockId  Unique id of the lock.
	 * @return DistributedLock instance.
	 * @see com.foreach.across.core.concurrent.SharedDistributedLock
	 */
	DistributedLock createSharedLock( String ownerId, String lockId );
}
