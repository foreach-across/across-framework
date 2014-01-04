package com.foreach.across.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * Base class to configure installer beans that will be registered in the repository,
 * and will only be executed once.
 */
public abstract class AcrossInstaller
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	@Autowired
	private AcrossContext context;

	@Autowired
	private AcrossInstallerRepository installerRepository;

	private AcrossModule module;

	void setModule( AcrossModule module ) {
		this.module = module;
	}

	public AcrossModule getModule() {
		return module;
	}

	public void execute() {
		if ( !isInstalled() ) {
			if ( context.isAllowInstallers() ) {
				if ( !context.isOnlyRegisterInstallers() ) {
					LOG.info( "Installing {}", getClass() );
					install();
				}
				else {
					// Skipping installer execution
					LOG.info( "Setting {} as installed without executing install()", getClass() );
				}

				installerRepository.setInstalled( this );
			}
			else {
				// Skip installer
				LOG.info( "Skipping installer {}", getClass() );
			}
		}
		else {
			LOG.debug( "{} was already installed", getClass() );
		}
	}

	/**
	 * @return True if this installer is registered as installed in the repository.
	 */
	public boolean isInstalled() {
		return installerRepository.isInstalled( this );
	}

	/**
	 * @return Descriptive text for the installer.
	 */
	public abstract String getDescription();

	/**
	 * Execute installation code.
	 */
	protected abstract void install();
}
