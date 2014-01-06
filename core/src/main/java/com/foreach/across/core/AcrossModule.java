package com.foreach.across.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;

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

	public void install() throws RuntimeException {
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

		// Load all beans of the module
		GenericApplicationContext ctx = new GenericApplicationContext( applicationContext );
		ClassPathBeanDefinitionScanner scanner =
				new ClassPathBeanDefinitionScanner( ctx.getDefaultListableBeanFactory() );
		scanner.setEnvironment( applicationContext.getEnvironment() );

		scanner.scan( getComponentScanPackages() );

		ctx.refresh();
	}

	/**
	 * @return Array of packages that should be scanned for components.
	 */
	protected String[] getComponentScanPackages() {
		return new String[] { getClass().getPackage().getName() };
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	public abstract String getName();

	/**
	 * @return Description of the content of this module.
	 */
	public abstract String getDescription();

	/**
	 * @return Array containing the installer classes in the order of which they should be run.
	 */
	protected abstract Class[] installerClasses();

	/**
	 * Called after all modules have been installed and - depending on the registration order in the context -
	 * the other modules have been bootstrapped.
	 */
	public void bootstrap() {
	}

	/**
	 * Called in case of a context shutdown.  Modules registered after this one in the context will have
	 * been shutdown already.
	 */
	public void shutdown() {
	}
}
