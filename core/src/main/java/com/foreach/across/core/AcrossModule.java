package com.foreach.across.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedList;

public abstract class AcrossModule
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	@Autowired
	private ApplicationContext applicationContext;

	private final Collection<AcrossInstaller> installers = new LinkedList<AcrossInstaller>();

	public Collection<AcrossInstaller> getInstallers() {
		return installers;
	}

	@PostConstruct
	private void install() throws Exception {
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

		for ( Class installerClass : installerClasses() ) {

			if ( !AcrossInstaller.class.isAssignableFrom( installerClass ) ) {
				LOG.error( "Installer {} does not extend from {}", installerClass, AcrossInstaller.class );

				throw new RuntimeException( "Installer " + installerClass + " must be of type AcrossInstaller" );
			}
			else {
				// Create installer bean
				AcrossInstaller installer = (AcrossInstaller) beanFactory.autowire( installerClass,
				                                                                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
				                                                                    false );
				installer.setModule( this );
				beanFactory.initializeBean( installer, installer.getClass().toString() );

				installer.execute();

				installers.add( installer );
			}
		}

		bootstrap();
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	public abstract String getName();

	/**
	 * @return Array containing the installer classes in the order of which they should be run.
	 */
	protected abstract Class[] installerClasses();

	/**
	 * Called after the module has been constructed.
	 * This is after any @PostConstruct methods have been called for a module.
	 */
	public void bootstrap() {
	}
}
