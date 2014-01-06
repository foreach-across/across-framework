package com.foreach.across.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class to configure installer beans that will be registered in the repository,
 * and will only be executed once.
 */
public abstract class AcrossInstaller
{
	private final static Logger INSTALL_LOG = LoggerFactory.getLogger( AcrossInstaller.class );

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
			if ( context.isAllowInstallers() || ( context.isSkipSchemaInstallers() && this instanceof AcrossLiquibaseInstaller ) ) {
				if ( !context.isOnlyRegisterInstallers() ) {
					INSTALL_LOG.info( "Installing {}", getClass() );
					install();
				}
				else {
					// Skipping installer execution
					INSTALL_LOG.info( "Setting {} as installed without executing install()", getClass() );
				}

				installerRepository.setInstalled( this );
			}
			else {
				// Skip installer
				INSTALL_LOG.info( "Skipping installer {}", getClass() );
			}
		}
		else {
			INSTALL_LOG.debug( "{} was already installed", getClass() );
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
