/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.web.config.multipart;

import com.foreach.across.condition.ConditionalOnConfigurableServletContext;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import com.foreach.across.modules.web.servlet.AcrossMultipartFilter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.PathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.*;
import java.io.IOException;
import java.util.Collections;

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
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Configuration
@ConditionalOnClass({ Servlet.class, StandardServletMultipartResolver.class, MultipartConfigElement.class })
@ConditionalOnProperty(prefix = "spring.http.multipart", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(MultipartProperties.class)
public class MultipartResolverConfiguration
{
	public static final String FILTER_NAME = "multipartFilter";
	public static final String COMMONS_FILE_UPLOAD = "org.apache.commons.fileupload.FileUpload";

	private final MultipartProperties multipartProperties;
	private final AcrossListableBeanFactory beanFactory;

	@Bean
	@ConditionalOnMissingBean
	public MultipartConfigElement multipartConfigElement( @Value("${java.io.tmpdir}") String tempDirectory ) {
		if ( !StringUtils.hasText( multipartProperties.getLocation() ) && StringUtils.hasText( tempDirectory ) ) {
			multipartProperties.setLocation( tempDirectory );
		}
		return this.multipartProperties.createMultipartConfig();
	}

	@Bean
	@ConditionalOnMissingBean
	public MultipartResolver filterMultipartResolver( MultipartConfigElement multipartConfigElement, ServletContext servletContext ) {
		boolean useCommons = ClassUtils.isPresent( COMMONS_FILE_UPLOAD, beanFactory.getBeanClassLoader() );

		if ( useCommons ) {
			return createCommonsMultipartResolver( multipartConfigElement, servletContext );
		}
		else {
			return createStandardServletMultipartResolver();
		}
	}

	@Autowired
	public void registerMultipartConfig( ListableBeanFactory beanFactory ) {
		try {
			MultipartConfigElement multipartConfigElement = BeanFactoryUtils.beanOfType( beanFactory, MultipartConfigElement.class );
			ServletRegistrationBean registrationBean = beanFactory.getBean(
					DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME,
					ServletRegistrationBean.class
			);

			registrationBean.setMultipartConfig( multipartConfigElement );
		}
		catch ( BeansException be ) {
			LOG.debug( "Unable to register MultipartConfigElement on the default dispatcher servlet" );
		}
	}

	@Bean
	@ConditionalOnConfigurableServletContext
	@ConditionalOnBean(name = "filterMultipartResolver")
	public FilterRegistrationBean multipartFilterRegistration( ServletContext servletContext,
	                                                           MultipartConfigElement multipartConfigElement,
	                                                           @Qualifier("filterMultipartResolver") MultipartResolver multipartResolver ) {
		servletContext.setAttribute( AbstractAcrossServletInitializer.ATTRIBUTE_DYNAMIC_MULTIPART_CONFIG, multipartConfigElement );

		MultipartFilter multipartFilter = new AcrossMultipartFilter( multipartResolver );

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName( FILTER_NAME );
		registration.setFilter( multipartFilter );
		registration.setAsyncSupported( true );
		registration.setMatchAfter( false );
		registration.setUrlPatterns( Collections.singletonList( "/*" ) );
		registration.setDispatcherTypes( DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC );
		registration.setOrder( Ordered.HIGHEST_PRECEDENCE + 1 );

		return registration;
	}

	private MultipartResolver createStandardServletMultipartResolver() {
		StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
		multipartResolver.setResolveLazily( multipartProperties.isResolveLazily() );
		return multipartResolver;
	}

	@SneakyThrows
	private MultipartResolver createCommonsMultipartResolver( MultipartConfigElement multipartConfig, ServletContext servletContext ) {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver( servletContext );
		multipartResolver.setMaxUploadSize( multipartConfig.getMaxFileSize() );
		multipartResolver.setMaxInMemorySize( multipartConfig.getFileSizeThreshold() );
		multipartResolver.setResolveLazily( multipartProperties.isResolveLazily() );

		try {
			multipartResolver.setUploadTempDir( new PathResource( multipartConfig.getLocation() ) );
		}
		catch ( IOException ioe ) {
			throw new ServletException( "Illegal location for multipart uploads: " + multipartConfig.getLocation() );
		}

		return multipartResolver;
	}
}
