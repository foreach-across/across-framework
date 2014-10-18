package com.foreach.across.modules.web.config;

import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;

@Configuration
@AcrossCondition("settings.autoConfigureMultipartResolver")
public class MultipartResolverConfiguration extends AcrossWebDynamicServletConfigurer
{
	public static final String COMMONS_FILE_UPLOAD = "org.apache.commons.fileupload.FileUpload";

	private static final Logger LOG = LoggerFactory.getLogger( MultipartResolverConfiguration.class );

	@Autowired
	private AcrossListableBeanFactory beanFactory;

	@Override
	protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
		registerResolverIfNecessary( servletContext, true );
	}

	@Override
	protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		registerResolverIfNecessary( servletContext, false );
	}

	private void registerResolverIfNecessary( ServletContext servletContext, boolean dynamicAllowed ) {
		Map<String, MultipartResolver> existingResolvers = BeanFactoryUtils.beansOfTypeIncludingAncestors( beanFactory,
		                                                                                                   MultipartResolver.class );

		if ( !existingResolvers.isEmpty() ) {
			// Determine the bean name of the multipart resolver
			String resolverBeanName = determineResolverBeanName( existingResolvers );
			LOG.debug( "Using existing multipart resolver with name {}", resolverBeanName );
		}
		else {
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

			if ( ClassUtils.isPresent( COMMONS_FILE_UPLOAD, beanFactory.getBeanClassLoader() ) ) {
				LOG.debug( "Creating and exposing a CommonsMultipartResolver" );
				beanDefinition.setBeanClass( CommonsMultipartResolver.class );
			}
			else if ( !dynamicAllowed ) {
				LOG.warn(
						"Unable to create a default MultipartResolver as Commons FileUpload is not present and Servlet 3.0 dynamic configuration is not allowed." +
								"  Please add Apache Commons FileUpload to the classpath to support auto configuration." );
			}
			else {
				LOG.debug( "Creating and exposing a StandardServletMultipartResolver" );
				beanDefinition.setBeanClass( StandardServletMultipartResolver.class );

				servletContext.setAttribute(
						AbstractAcrossServletInitializer.ATTRIBUTE_DYNAMIC_MULTIPART_CONFIG,
						new MultipartConfigElement( System.getProperty( "java.io.tmpdir" ) )
				);
			}

			beanFactory.registerBeanDefinition( DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME, beanDefinition );
			beanFactory.expose( DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME );
		}
	}

	private String determineResolverBeanName( Map<String, MultipartResolver> existingResolvers ) {
		MultipartResolver resolver = existingResolvers.get( DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME );

		String resolverBeanName = null;

		if ( resolver != null ) {
			resolverBeanName = MultipartFilter.DEFAULT_MULTIPART_RESOLVER_BEAN_NAME;
		}
		else {
			Assert.isTrue( existingResolvers.size() == 1,
			               "Unable to determine MultipartResolver as there is more than one" );
			for ( String beanName : existingResolvers.keySet() ) {
				resolverBeanName = beanName;
			}
		}

		Assert.notNull( resolverBeanName, "Could not determine bean name of existing MultipartResolver" );

		return resolverBeanName;
	}

}
