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
	 * multi-threaded scenarios.  This lock instance does not require manual management of the owner id
	 * or threads trying to acquire/release the lock.
	 * <p/>
	 * For descriptive purposes the owner id passed in will be used
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
}
