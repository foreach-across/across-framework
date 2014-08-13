package com.foreach.across.core.concurrent;

/**
 * Internal manager that links
 */
public interface DistributedLockManager
{
	boolean acquire( DistributedLock lock );

	boolean release( DistributedLock lock );
}
