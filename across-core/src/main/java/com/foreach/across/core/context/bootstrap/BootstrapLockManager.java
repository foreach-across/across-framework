package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.concurrent.DistributedLock;
import com.foreach.across.core.concurrent.DistributedLockRepository;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
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

		if ( !lock.isLockedByMe() ) {
			LOG.debug( "Acquiring Across bootstrap lock, owner id: {}", lock.getOwnerId() );

			long lockStartTime = System.currentTimeMillis();
			lock.lock();

			LOG.info( "Across bootstrap lock acquired by {} in {} ms", lock.getOwnerId(),
			          System.currentTimeMillis() - lockStartTime );
		}
	}

	public void ensureUnlocked() {
		if ( installerLock != null ) {
			LOG.info( "Releasing Across bootstrap lock - owner {}", installerLock.getOwnerId() );
			installerLock.unlock();
			installerLock = null;
		}
	}

	private DistributedLock loadLock() {
		if ( installerLock == null ) {
			installerLock = AcrossContextUtils
					.getBeanRegistry( contextInfo )
					.getBeanOfType( DistributedLockRepository.class )
					.createLock( BOOTSTRAP_LOCK );
		}

		return installerLock;
	}
}
