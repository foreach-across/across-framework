package com.foreach.across.core.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Simple implementation of {@link com.foreach.across.core.concurrent.DistributedLockRepository}.
 *
 * @author Arne Vandamme
 */
public class DistributedLockRepositoryImpl implements DistributedLockRepository
{
	private static final String JVM_ID = UUID.randomUUID().toString();

	private final DistributedLockManager lockManager;
	private final String defaultOwnerName;

	public DistributedLockRepositoryImpl( DistributedLockManager lockManager ) {
		this.lockManager = lockManager;
		this.defaultOwnerName = JVM_ID;
	}

	public DistributedLockRepositoryImpl( DistributedLockManager lockManager, String defaultOwnerName ) {
		this.lockManager = lockManager;
		this.defaultOwnerName = defaultOwnerName;
	}

	@Override
	public DistributedLock createLock( String lockId ) {
		return createLock( defaultOwnerName, lockId );
	}

	@Override
	public DistributedLock createLock( String ownerName, String lockId ) {
		Assert.isTrue( StringUtils.isNotBlank( lockId ), "lockId must not be empty" );
		Assert.isTrue( StringUtils.isNotBlank( ownerName ), "ownerName must not be empty" );

		return new ThreadBasedDistributedLock( lockManager, ownerName, lockId );
	}

	@Override
	public DistributedLock createSharedLock( String ownerId, String lockId ) {
		Assert.isTrue( StringUtils.isNotBlank( lockId ), "lockId must not be empty" );
		Assert.isTrue( StringUtils.isNotBlank( ownerId ), "ownerId must not be empty" );

		return new SharedDistributedLock( lockManager, ownerId, lockId );
	}
}
