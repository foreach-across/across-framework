package com.foreach.across.core;

import com.foreach.across.core.events.AcrossBootstrapFinishedEvent;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

public class AcrossContext
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContext.class );

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private ApplicationEventPublisher publisher;

	private boolean allowInstallers;
	private boolean skipSchemaInstallers;
	private boolean onlyRegisterInstallers;

	private LinkedList<AcrossModule> modules = new LinkedList<AcrossModule>();

	@Autowired
	List<ApplicationListener<AcrossBootstrapFinishedEvent>> listeners;

	private boolean isBootstrapped = false;

	public void addModule( AcrossModule module ) {
		if ( !modules.contains( module ) ) {
			modules.add( module );

			if ( isBootstrapped ) {
				LOG.info( "Added module to bootstrapped context - installing & bootstrapping module" );

				module.install();
				module.bootstrap();
			}
		}
	}

	public Collection<AcrossModule> getModules() {
		return modules;
	}

	public boolean isAllowInstallers() {
		return allowInstallers;
	}

	public void setAllowInstallers( boolean allowInstallers ) {
		this.allowInstallers = allowInstallers;
	}

	public boolean isSkipSchemaInstallers() {
		return skipSchemaInstallers;
	}

	public void setSkipSchemaInstallers( boolean skipSchemaInstallers ) {
		this.skipSchemaInstallers = skipSchemaInstallers;
	}

	public boolean isOnlyRegisterInstallers() {
		return onlyRegisterInstallers;
	}

	public void setOnlyRegisterInstallers( boolean onlyRegisterInstallers ) {
		this.onlyRegisterInstallers = onlyRegisterInstallers;
	}

	@PostConstruct
	public void bootstrap() throws Exception {
		if ( isBootstrapped ) {
			return;
		}

		isBootstrapped = true;

		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

		// Run base schema installer
		if ( applicationContext.getBeanNamesForType( AcrossCoreSchemaInstaller.class ).length == 0 ) {
			AcrossCoreSchemaInstaller installer =
					(AcrossCoreSchemaInstaller) beanFactory.autowire( AcrossCoreSchemaInstaller.class,
					                                                  AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
					                                                  false );
			beanFactory.initializeBean( installer, AcrossCoreSchemaInstaller.class.getName() );
		}

		// Create the installer repository
		AcrossInstallerRepository installerRepository = beanFactory.createBean( AcrossInstallerRepository.class );
		beanFactory.registerSingleton( AcrossInstallerRepository.class.getName(), installerRepository );

		// Activate the core module
		AcrossCoreModule acrossCoreModule = beanFactory.createBean( AcrossCoreModule.class );
		beanFactory.registerSingleton( acrossCoreModule.getName(), acrossCoreModule );

		modules.addFirst( acrossCoreModule );

		// Install all modules in order
		LOG.debug( "Installing and bootstrapping {} Across modules", modules.size() );

		for ( AcrossModule module : modules ) {
			module.install();
			module.bootstrap();
		}

		// Publish bootstrap finished event
		AcrossBootstrapFinishedEvent e =
				new AcrossBootstrapFinishedEvent( this, this, new ArrayList<AcrossModule>( modules ) );
		publisher.publishEvent( e );
	}

	@PreDestroy
	public void shutdown() {
		if ( isBootstrapped ) {
			// Shutdown all modules in reverse order
			List<AcrossModule> reverseList = new LinkedList<AcrossModule>( modules );
			Collections.reverse( reverseList );

			for ( AcrossModule module : reverseList ) {
				module.shutdown();
			}

			isBootstrapped = false;
		}
	}
}
