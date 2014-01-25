package com.foreach.across.core;

import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossContextUtil;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;

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
public class AcrossContext extends AcrossApplicationContextHolder
{
	public static final String DATASOURCE = "acrossDataSource";

	private DataSource dataSource;

	private boolean allowInstallers;
	private boolean onlyRegisterInstallers;
	private String[] skipInstallerGroups = new String[0];
	private BeanFactoryPostProcessor[] beanFactoryPostProcessors = new BeanFactoryPostProcessor[0];

	private LinkedList<AcrossModule> modules = new LinkedList<AcrossModule>();

	private boolean isBootstrapped = false;

	private ApplicationContext parentApplicationContext;

	/**
	 * Constructs a new AcrossContext in its own ApplicationContext.
	 * Modules in this context will not be able to use any beans defined outside the AcrossContext.
	 */
	public AcrossContext() {
		this( null );
	}

	/**
	 * Constructs a new AcrossContext that is a child of the parent ApplicationContext passed in.
	 * Modules can access all beans from the parent context, and exposed beans will be copied to the parent
	 * context so non-Across beans can access them.
	 *
	 * @param parentContext Parent ApplicationContext.
	 */
	public AcrossContext( ApplicationContext parentContext ) {
		parentApplicationContext = parentContext;

		setDefaultModules();
	}

	public ApplicationContext getParentApplicationContext() {
		return parentApplicationContext;
	}

	protected void setDefaultModules() {
		modules.addFirst( new AcrossCoreModule() );
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

	public void setBeanFactoryPostProcessors( BeanFactoryPostProcessor... postProcessors ) {
		this.beanFactoryPostProcessors = postProcessors;
	}

	public BeanFactoryPostProcessor[] getBeanFactoryPostProcessors() {
		return beanFactoryPostProcessors;
	}

	/**
	 * Shortcut method to publish an event synchronously on the AcrossContext event bus.
	 * For more fine-grained functionality like asynchronous publishing, use the AcrossEventPublisher.
	 *
	 * @param event Event instance that will be published.
	 */
	public void publishEvent( AcrossEvent event ) {
		AcrossContextUtil.getBeanOfType( this, AcrossEventPublisher.class ).publish( event );
	}

	@PostConstruct
	public void bootstrap() throws Exception {
		if ( !isBootstrapped ) {
			isBootstrapped = true;

			new AcrossBootstrapper( this ).bootstrap();
		}
	}

	@PreDestroy
	public void shutdown() {
		if ( isBootstrapped ) {
			// Shutdown all modules in reverse order - note that it is quite possible to beans might have been destroyed
			// already by Spring in the meantime
			List<AcrossModule> reverseList = new LinkedList<AcrossModule>( modules );
			Collections.reverse( reverseList );

			for ( AcrossModule module : reverseList ) {
				module.shutdown();
			}

			isBootstrapped = false;
		}
	}
}
