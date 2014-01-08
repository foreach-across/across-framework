package com.foreach.across.core;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
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

	private AnnotationConfigApplicationContext ctx;

	public AcrossContext() {
		ctx = new AnnotationConfigApplicationContext();

		modules.addFirst( new AcrossCoreModule() );
	}

	public AcrossContext( ApplicationContext parentContext ) {
		ctx = new AnnotationConfigApplicationContext();
		ctx.setParent( parentContext );

		if ( parentContext.getEnvironment() instanceof ConfigurableEnvironment ) {
			ctx.setEnvironment( (ConfigurableEnvironment) parentContext.getEnvironment() );
		}

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

	public ApplicationContext getApplicationContext() {
		return ctx;
	}

	public void setBeanFactoryPostProcessors( BeanFactoryPostProcessor... postProcessors ) {
		this.beanFactoryPostProcessors = postProcessors;
	}

	public BeanFactoryPostProcessor[] getBeanFactoryPostProcessors() {
		return beanFactoryPostProcessors;
	}

	@PostConstruct
	public void bootstrap() throws Exception {
		if ( !isBootstrapped ) {
			isBootstrapped = true;

			new AcrossBootstrap().bootstrap( this );
		}
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
