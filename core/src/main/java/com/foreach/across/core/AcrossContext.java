package com.foreach.across.core;

import com.foreach.across.core.events.AcrossEvent;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Main class representing a set of Across modules.
 * This class takes care of managing the global Spring ApplicationContext related to all modules,
 * and will make sure that different modules are handled in the order in which they are registered.
 */
public class AcrossContext
{
	public static final String DATASOURCE = "acrossDataSource";

	private boolean allowInstallers;
	private boolean onlyRegisterInstallers;

	private DataSource dataSource;

	private String[] skipInstallerGroups = new String[0];
	private BeanFactoryPostProcessor[] beanFactoryPostProcessors = new BeanFactoryPostProcessor[0];

	private LinkedList<AcrossModule> modules = new LinkedList<AcrossModule>();

	private boolean isBootstrapped = false;

	private MBassador<AcrossEvent> eventBus = new MBassador<AcrossEvent>( BusConfiguration.Default() );

	private ConfigurableApplicationContext applicationContext;

	public AcrossContext() {
		applicationContext = createApplicationContext( null );

		setDefaultModules();
	}

	public AcrossContext( ApplicationContext parentContext ) {
		applicationContext = createApplicationContext( parentContext );

		setDefaultModules();
	}

	protected void setDefaultModules() {
		modules.addFirst( new AcrossCoreModule() );
	}

	protected ConfigurableApplicationContext createApplicationContext( ApplicationContext parent ) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

		if ( parent != null ) {
			applicationContext.setParent( parent );

			if ( parent.getEnvironment() instanceof ConfigurableEnvironment ) {
				applicationContext.setEnvironment( (ConfigurableEnvironment) parent.getEnvironment() );
			}
		}

		return applicationContext;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	public Collection<AcrossModule> getModules() {
		return modules;
	}

	public void addModule( AcrossModule module ) {
		if ( !modules.contains( module ) ) {
			modules.add( module );

			if ( isBootstrapped ) {
				throw new RuntimeException(
						"Adding a module to an already bootstrapped AcrossContext is currently not supported." );
			}
		}
	}

	public boolean isAllowInstallers() {
		return allowInstallers;
	}

	public void setAllowInstallers( boolean allowInstallers ) {
		this.allowInstallers = allowInstallers;
	}

	public boolean isOnlyRegisterInstallers() {
		return onlyRegisterInstallers;
	}

	public void setOnlyRegisterInstallers( boolean onlyRegisterInstallers ) {
		this.onlyRegisterInstallers = onlyRegisterInstallers;
	}

	public String[] getSkipInstallerGroups() {
		return skipInstallerGroups;
	}

	public void setSkipInstallerGroups( String[] skipInstallerGroups ) {
		this.skipInstallerGroups = skipInstallerGroups;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setBeanFactoryPostProcessors( BeanFactoryPostProcessor... postProcessors ) {
		this.beanFactoryPostProcessors = postProcessors;
	}

	public BeanFactoryPostProcessor[] getBeanFactoryPostProcessors() {
		return beanFactoryPostProcessors;
	}

	/**
	 * Publishes an event in the Across ApplicationContext.  All AcrossContextEventListeners from the modules, and any
	 * listeners in the parent ApplicationContexts will receive this event.
	 *
	 * @param event Event instance that will be published.
	 */
	public void publishEvent( AcrossEvent event ) {
		applicationContext.publishEvent( event );
	}

	@PostConstruct
	public void bootstrap() throws Exception {
		if ( !isBootstrapped ) {
			isBootstrapped = true;

			new AcrossBootstrap( createBootstrapHandler() ).bootstrap( this );
		}
	}

	protected AcrossBootstrapApplicationContextHandler createBootstrapHandler() {
		return new AcrossBootstrapApplicationContextHandler();
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
