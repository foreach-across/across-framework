package com.foreach.across.modules.web.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.bootstrap.AnnotationConfigBootstrapApplicationContextFactory;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

/**
 * Creates WebApplicationContext versions of the standard ApplicationContext.
 */
public class WebBootstrapApplicationContextFactory extends AnnotationConfigBootstrapApplicationContextFactory
{
	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ApplicationContext parentApplicationContext ) {
		if ( parentApplicationContext == null || parentApplicationContext instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentApplicationContext;
			AcrossSpringWebApplicationContext applicationContext = new AcrossSpringWebApplicationContext();

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

		return super.createApplicationContext( across, parentApplicationContext );
	}

	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ModuleBootstrapConfig moduleBootstrapConfig,
	                                                            AcrossApplicationContext parentContext ) {
		if ( parentContext.getApplicationContext() instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentContext.getApplicationContext();
			AcrossSpringWebApplicationContext child = new AcrossSpringWebApplicationContext();

			child.setServletContext( parentWebContext.getServletContext() );
			child.setParent( parentContext.getApplicationContext() );
			child.setEnvironment( parentContext.getApplicationContext().getEnvironment() );

			return child;
		}

		return super.createApplicationContext( across, moduleBootstrapConfig, parentContext );
	}

	@Override
	public void loadApplicationContext( AcrossContext across, AcrossApplicationContext context ) {
		AcrossSpringWebApplicationContext root = (AcrossSpringWebApplicationContext) context.getApplicationContext();
		Collection<ApplicationContextConfigurer> configurers = AcrossContextUtils.getConfigurersToApply( across );

		loadApplicationContext( root, configurers );
	}

	public void loadApplicationContext( AcrossContext across,
	                                    ModuleBootstrapConfig moduleBootstrapConfig,
	                                    AcrossApplicationContext context ) {
		AcrossSpringWebApplicationContext child = (AcrossSpringWebApplicationContext) context.getApplicationContext();

		loadApplicationContext( child, moduleBootstrapConfig.getApplicationContextConfigurers() );
	}

	private void loadApplicationContext( AcrossConfigurableApplicationContext context,
	                                     Collection<ApplicationContextConfigurer> configurers ) {
		for ( ApplicationContextConfigurer configurer : configurers ) {
			ProvidedBeansMap providedBeans = configurer.providedBeans();

			if ( providedBeans != null ) {
				context.provide( providedBeans );
			}

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
