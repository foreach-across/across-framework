package com.foreach.across.core.concurrent;

import java.util.concurrent.locks.Lock;

/**
 * @author Arne Vandamme
 */
public interface DistributedLockRepository
{
	Lock getLock( String id );

	Lock getSharedLock( String owner, String id );

	boolean acquire( DistributedLock lock );

	boolean release( DistributedLock lock );
}
