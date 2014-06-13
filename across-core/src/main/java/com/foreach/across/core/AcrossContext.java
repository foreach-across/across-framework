package com.foreach.across.core;

import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.PropertySourcesConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;

/**
 * Main class representing a set of Across modules.
 * This class takes care of managing the global Spring ApplicationContext related to all modules,
 * and will make sure that different modules are handled in the order in which they are registered.
 */
public class AcrossContext extends AcrossApplicationContextHolder implements DisposableBean
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContext.class );

	public static final String BEAN = "acrossContext";
	public static final String DATASOURCE = "acrossDataSource";

	private DataSource dataSource;

	private boolean allowInstallers;
	private boolean onlyRegisterInstallers;
	private String[] skipInstallerGroups = new String[0];

	private Map<ApplicationContextConfigurer, ConfigurerScope> applicationContextConfigurers =
			new LinkedHashMap<ApplicationContextConfigurer, ConfigurerScope>();

	private LinkedList<AcrossModule> modules = new LinkedList<>();

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
	}

	public ApplicationContext getParentApplicationContext() {
		return parentApplicationContext;
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
		if ( modules.contains( module ) ) {
			throw new RuntimeException(
					"Not allowed to add the same module instance to a single AcrossContext: " + module );
		}

		if ( module.getContext() != null ) {
			throw new RuntimeException( "Module is already attached to another AcrossContext: " + module );
		}

		if ( isBootstrapped ) {
			throw new RuntimeException(
					"Adding a module to an already bootstrapped AcrossContext is currently not supported." );
		}

		modules.add( module );
		module.setContext( this );
	}

	/**
	 * Gets the module with the given name (or fully qualified class name) if present on the context.
	 * Only the first module matching the name will be returned.
	 *
	 * @param name Name or fully qualified class name of the module.
	 * @return AcrossModule or null if not present.
	 */
	public AcrossModule getModule( String name ) {
		for ( AcrossModule module : modules ) {
			if ( StringUtils.equals( module.getName(), name ) || StringUtils.equals( module.getClass().getName(),
			                                                                         name ) ) {
				return module;
			}
		}

		return null;
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
		this.skipInstallerGroups = skipInstallerGroups.clone();
	}

	public Map<ApplicationContextConfigurer, ConfigurerScope> getApplicationContextConfigurers() {
		return applicationContextConfigurers;
	}

	/**
	 * <p>Add an ApplicationContextConfigurer to the AcrossContext.  Depending on the scope the configurer
	 * will be applied to the root ApplicationContext only or to every ApplicationContext of every module registered.</p>
	 * <p>The latter is required for configurers providing BeanFactoryPostProcessor beans like property sources.</p>
	 *
	 * @param configurer Configurer instance.
	 * @param scope      Scope to which this configurer should be applied.
	 * @see com.foreach.across.core.context.configurer.ConfigurerScope
	 */
	public void addApplicationContextConfigurer( ApplicationContextConfigurer configurer, ConfigurerScope scope ) {
		applicationContextConfigurers.put( configurer, scope );
	}

	/**
	 * Add PropertySources to the context.
	 *
	 * @param propertySources A PropertySources instance.
	 */
	@Override
	public void addPropertySources( PropertySources propertySources ) {
		// Only added to the context as they are merged in the environment of the module anyway
		addApplicationContextConfigurer( new PropertySourcesConfigurer( propertySources ),
		                                 ConfigurerScope.CONTEXT_ONLY );
	}

	/**
	 * Shortcut to add PropertySources to the context.
	 *
	 * @param propertySources One or more PropertySource instances.
	 */
	@Override
	public void addPropertySources( PropertySource<?>... propertySources ) {
		// Only added to the context as they are merged in the environment of the module anyway
		addApplicationContextConfigurer( new PropertySourcesConfigurer( propertySources ),
		                                 ConfigurerScope.CONTEXT_ONLY );
	}

	/**
	 * Shortcut method to publish an event synchronously on the AcrossContext event bus.
	 * For more fine-grained functionality like asynchronous publishing, use the AcrossEventPublisher.
	 *
	 * @param event Event instance that will be published.
	 */
	public void publishEvent( AcrossEvent event ) {
		AcrossContextUtils.getBeanOfType( this, AcrossEventPublisher.class ).publish( event );
	}

	@PostConstruct
	public void bootstrap() {
		if ( !isBootstrapped ) {
			isBootstrapped = true;

			new AcrossBootstrapper( this ).bootstrap();
		}
	}

	public void shutdown() {
		if ( isBootstrapped ) {
			LOG.info( "Shutdown signal received - destroying ApplicationContext instances" );

			// Shutdown all modules in reverse order - note that it is quite possible to beans might have been destroyed
			// already by Spring in the meantime
			List<AcrossModuleInfo> reverseList =
					new ArrayList<>( AcrossContextUtils.getBeanOfType( this, AcrossContextInfo.class ).getModules() );
			Collections.reverse( reverseList );

			for ( AcrossModuleInfo moduleInfo : reverseList ) {
				if ( moduleInfo.isBootstrapped() ) {
					AcrossModule module = moduleInfo.getModule();
					AbstractApplicationContext applicationContext = AcrossContextUtils.getApplicationContext( module );

					if ( applicationContext != null ) {
						LOG.debug( "Destroying ApplicationContext for module {}", module.getName() );

						module.shutdown();
						applicationContext.destroy();
					}
				}
			}

			// Destroy the root ApplicationContext
			AcrossContextUtils.getApplicationContext( this ).destroy();

			LOG.debug( "Destroyed root ApplicationContext" );

			isBootstrapped = false;
		}
	}

	public void destroy() {
		shutdown();
	}
}
