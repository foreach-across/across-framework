package com.foreach.across.core.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * @author Arne Vandamme
 */
public class DistributedLockRepositoryImpl implements DistributedLockRepository
{
	private final DistributedLockManager lockManager;

	public DistributedLockRepositoryImpl( DistributedLockManager lockManager ) {
		this.lockManager = lockManager;
	}

	@Override
	public DistributedLock createLock( String lockId ) {
		return createLock( UUID.randomUUID().toString(), lockId );
	}

	@Override
	public DistributedLock createLock( String ownerName, String lockId ) {
		Assert.isTrue( StringUtils.isNotBlank( lockId ), "lockId must not be empty" );
		Assert.isTrue( StringUtils.isNotBlank( ownerName ), "ownerName must not be empty" );

		return new ThreadBasedDistributedLock( lockManager, ownerName, lockId );
	}
}
