package com.foreach.across.modules.web.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.AnnotationConfigBootstrapApplicationContextFactory;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Collection;
import java.util.Map;

/**
 * Creates WebApplicationContext versions of the standard ApplicationContext.
 */
public class WebBootstrapApplicationContextFactory extends AnnotationConfigBootstrapApplicationContextFactory
{
	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ApplicationContext parentApplicationContext,
	                                                            Map<String, Object> singletons ) {
		if ( parentApplicationContext == null || parentApplicationContext instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentApplicationContext;
			AcrossSpringWebApplicationContext applicationContext = new AcrossSpringWebApplicationContext( singletons );

			if ( parentApplicationContext != null ) {
				applicationContext.setParent( parentApplicationContext );
				applicationContext.setServletContext( parentWebContext.getServletContext() );

				if ( parentApplicationContext.getEnvironment() instanceof ConfigurableEnvironment ) {
					applicationContext.setEnvironment(
							(ConfigurableEnvironment) parentApplicationContext.getEnvironment() );
				}
			}

			return applicationContext;
		}

		return super.createApplicationContext( across, parentApplicationContext, singletons );
	}

	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            AcrossModule module,
	                                                            AcrossApplicationContext parentContext,
	                                                            Map<String, Object> singletons ) {
		if ( parentContext.getApplicationContext() instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentContext.getApplicationContext();
			AcrossSpringWebApplicationContext child = new AcrossSpringWebApplicationContext( singletons );

			child.setServletContext( parentWebContext.getServletContext() );
			child.setParent( parentContext.getApplicationContext() );
			child.setEnvironment( parentContext.getApplicationContext().getEnvironment() );

			return child;
		}

		return super.createApplicationContext( across, module, parentContext, singletons );
	}

	/**
	 * Loads beans and definitions in the root ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param context Contains the root Spring ApplicationContext.
	 */
	@Override
	public void loadApplicationContext( AcrossContext across, AcrossApplicationContext context ) {
		AcrossSpringWebApplicationContext root = (AcrossSpringWebApplicationContext) context.getApplicationContext();
		Collection<ApplicationContextConfigurer> configurers = across.getApplicationContextConfigurers().keySet();

		loadApplicationContext( root, configurers );
	}

	/**
	 * Loads beans and definitions in the module ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param module  AcrossModule being loaded.
	 * @param context Contains the Spring ApplicationContext for the module.
	 */
	public void loadApplicationContext( AcrossContext across, AcrossModule module, AcrossApplicationContext context ) {
		AcrossSpringWebApplicationContext child = (AcrossSpringWebApplicationContext) context.getApplicationContext();
		Collection<ApplicationContextConfigurer> configurers =
				AcrossContextUtils.getConfigurersToApply( across, module );

		loadApplicationContext( child, configurers );
	}

	private void loadApplicationContext( AnnotationConfigWebApplicationContext context,
	                                     Collection<ApplicationContextConfigurer> configurers ) {
		for ( ApplicationContextConfigurer configurer : configurers ) {
			for ( BeanFactoryPostProcessor postProcessor : configurer.postProcessors() ) {
				context.addBeanFactoryPostProcessor( postProcessor );
			}

			if ( !ArrayUtils.isEmpty( configurer.annotatedClasses() ) ) {
				context.register( configurer.annotatedClasses() );
			}

			if ( !ArrayUtils.isEmpty( configurer.componentScanPackages() ) ) {
				context.scan( configurer.componentScanPackages() );
			}
		}

		context.refresh();
		context.start();
	}
}
