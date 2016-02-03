package com.foreach.across.modules.web.config.multipart;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import com.foreach.across.modules.web.servlet.AcrossMultipartFilter;
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.*;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

/**
 * Creates a MultipartResolver for handling multipart requests.  Will use the
 * {@link org.springframework.web.multipart.support.MultipartFilter} if possible and fallback
 * to multipart resolving in the {@link org.springframework.web.servlet.DispatcherServlet}.
 * <p>
 * If Commons FileUpload is not present on the classpath, the standard servlet resolver will
 * be used.  This requires the web container to support multipart resolving.</p>
 *
 * @see org.springframework.web.multipart.support.StandardServletMultipartResolver
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 */
@Configuration
@ConditionalOnProperty(value = "acrossWebModule.multipart.auto-configure", matchIfMissing = true)
public class MultipartResolverConfiguration extends AcrossWebDynamicServletConfigurer
{
	public static final String FILTER_NAME = "multipartFilter";
	public static final String COMMONS_FILE_UPLOAD = "org.apache.commons.fileupload.FileUpload";

	private static final Logger LOG = LoggerFactory.getLogger( MultipartResolverConfiguration.class );

	@Autowired
	private AcrossWebModuleSettings settings;

	@Autowired
	private AcrossListableBeanFactory beanFactory;

	@Override
	protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
		String resolverBeanName = determineExistingResolverBeanName();

		if ( StringUtils.equals( DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME, resolverBeanName ) ) {
			LOG.warn(
					"Unable to switch to MultipartFilter as there is an existing MultipartResolver registered as {} bean.  " +
							"This resolver will be picked up by the DispatcherServlet, " +
							"multipart requests will only be handled correctly in the DispatcherServlet.",
					DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME );
		}
		else {
			if ( resolverBeanName == null ) {
				resolverBeanName = MultipartFilter.DEFAULT_MULTIPART_RESOLVER_BEAN_NAME;
				createMultipartResolver( servletContext, true, resolverBeanName );
			}

			LOG.debug( "Registering the MultipartFilter with resolver bean {} for default DispatcherServlet",
			           resolverBeanName );

			MultipartResolver resolver = beanFactory.getBean( resolverBeanName, MultipartResolver.class );
			MultipartFilter multipartFilter = new AcrossMultipartFilter( resolver );
			beanFactory.registerSingleton( FILTER_NAME, multipartFilter );

			FilterRegistration.Dynamic registration = servletContext.addFilter( FILTER_NAME,
			                                                                    multipartFilter );

			if ( registration == null ) {
				throw new IllegalStateException(
						"Duplicate Filter registration for '" + FILTER_NAME + "'. Check to ensure the Filter is only configured once." );
			}
			registration.setAsyncSupported( true );

			registration.addMappingForUrlPatterns( EnumSet.of(
					                                       DispatcherType.REQUEST,
					                                       DispatcherType.ERROR,
					                                       DispatcherType.ASYNC
			                                       ),
			                                       false,
			                                       "/*" );
		}
	}

	@Override
	protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		String resolverBeanName = determineExistingResolverBeanName();

		if ( resolverBeanName == null ) {
			resolverBeanName = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME;
			createMultipartResolver( servletContext, false, resolverBeanName );

			LOG.debug( "Exposing MultipartResolver bean {} for automatic detection by the DispatcherServlet.",
			           resolverBeanName );
			beanFactory.expose( resolverBeanName );
		}
		else if ( !StringUtils.equals( DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME, resolverBeanName ) ) {
			LOG.warn(
					"Unable to auto configure Multipart resolving as there is a multipart resolver bean registered as {}.  " +
							"This bean will not be picked up by the DispatcherServlet as it should be named {} for that.  " +
							"Please configure MultipartResolving manually.",
					resolverBeanName,
					DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME );
		}
	}

	private void createMultipartResolver( ServletContext servletContext,
	                                      boolean dynamicConfiguration,
	                                      String beanName ) throws ServletException {
		boolean useCommons = ClassUtils.isPresent( COMMONS_FILE_UPLOAD, beanFactory.getBeanClassLoader() );

		if ( !dynamicConfiguration && !useCommons ) {
			LOG.warn(
					"Creating a standard servlet MultipartResolver without Servlet 3.0 dynamic configuration.  " +
							"Please ensure the DispatcherServlet has a MultipartConfig section in the web.xml or as annotation. " );
		}

		MultipartConfigElement multipartConfig = determineMultipartConfig();

		if ( useCommons ) {
			createCommonsMultipartResolver( multipartConfig, servletContext, beanName );
		}
		else {
			createStandardServletMultipartResolver( beanName );

			if ( dynamicConfiguration ) {
				LOG.trace( "Exposing the MultipartConfigElement to the DispatcherServlet." );
				servletContext.setAttribute( AbstractAcrossServletInitializer.ATTRIBUTE_DYNAMIC_MULTIPART_CONFIG,
				                             multipartConfig );
			}
		}
	}

	private void createStandardServletMultipartResolver( String beanName ) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass( StandardServletMultipartResolver.class );

		beanFactory.registerBeanDefinition( beanName, beanDefinition );
	}

	private void createCommonsMultipartResolver( MultipartConfigElement multipartConfig,
	                                             ServletContext servletContext,
	                                             String beanName ) throws ServletException {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver( servletContext );
		multipartResolver.setMaxUploadSize( multipartConfig.getMaxFileSize() );
		multipartResolver.setMaxInMemorySize( multipartConfig.getFileSizeThreshold() );

		try {
			multipartResolver.setUploadTempDir( new PathResource( multipartConfig.getLocation() ) );
		}
		catch ( IOException ioe ) {
			throw new ServletException( "Illegal location for multipart uploads: " + multipartConfig.getLocation() );
		}

		beanFactory.registerSingleton( beanName, multipartResolver );
	}

	private MultipartConfigElement determineMultipartConfig() {
		MultipartConfigElement config = settings.getMultipart().getSettings();

		if ( config == null ) {
			Map<String, MultipartConfigElement> configs
					= BeanFactoryUtils.beansOfTypeIncludingAncestors( beanFactory, MultipartConfigElement.class );

			if ( !configs.isEmpty() && configs.size() > 1 ) {
				throw new IllegalStateException(
						"Found more than one MultipartConfigElement - unable to autoconfigure MultipartResolver.  " +
								"Please specify a MultipartConfigElement as a property on the AcrossWebModule to resolve this problem." );
			}

			if ( !configs.isEmpty() ) {
				LOG.trace( "Using MultipartConfigElement from the ApplicationContext" );
				config = configs.values().iterator().next();
			}
			else {
				LOG.trace( "Creating default MultipartConfigElement" );
				config = new MultipartConfiguration( System.getProperty( "java.io.tmpdir" ), -1L, -1L, 10 * 1024 );
			}
		}
		else {
			LOG.trace( "Using MultipartConfigElement that was set on the AcrossWebModule" );
		}

		return config;
	}

	private String determineExistingResolverBeanName() {
		Map<String, MultipartResolver> existingResolvers
				= BeanFactoryUtils.beansOfTypeIncludingAncestors( beanFactory, MultipartResolver.class );

		if ( !existingResolvers.isEmpty() ) {
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

		return null;
	}
}
