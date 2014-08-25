package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.common.concurrent.locks.distributed.DistributedLock;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance that manages the distributed lock for an Across context bootstrap.
 * A bootstrap lock is only acquired when the first installer wants to execute,
 * and is released once bootstrap is finished (or fails).
 *
 * @author Arne Vandamme
 */
public class BootstrapLockManager
{
	public static final String BOOTSTRAP_LOCK = "across:bootstrap";

	private static final Logger LOG = LoggerFactory.getLogger( BootstrapLockManager.class );

	private DistributedLock installerLock;

	private final AcrossContextInfo contextInfo;

	public BootstrapLockManager( AcrossContextInfo contextInfo ) {
		this.contextInfo = contextInfo;
	}

	public void ensureLocked() {
		DistributedLock lock = loadLock();

		if ( !lock.isHeldByCurrentThread() ) {
			LOG.debug( "Acquiring Across bootstrap lock, owner id: {}", lock.getOwnerId() );

			long lockStartTime = System.currentTimeMillis();
			lock.lock();

			LOG.info( "Across bootstrap lock acquired by {} in {} ms", lock.getOwnerId(),
			          System.currentTimeMillis() - lockStartTime );
		}
	}

	public void ensureUnlocked() {
		if ( installerLock != null ) {
			try {
				LOG.info( "Releasing Across bootstrap lock - owner {}", installerLock.getOwnerId() );
				installerLock.unlock();
			}
			catch ( Exception e ) {
				LOG.warn( "Could not release the bootstrap lock: {}", e );
			}
			installerLock = null;
		}
	}

	private DistributedLock loadLock() {
		if ( installerLock == null ) {
			installerLock = AcrossContextUtils
					.getBeanRegistry( contextInfo )
					.getBeanOfType( DistributedLockRepository.class )
					.getLock( BOOTSTRAP_LOCK );
		}

		return installerLock;
	}
}
