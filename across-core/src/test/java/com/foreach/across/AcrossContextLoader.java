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
package com.foreach.across;

import com.foreach.across.core.context.AcrossApplicationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.test.context.support.AbstractGenericContextLoader;
import org.springframework.test.context.support.AnnotationConfigContextLoaderUtils;
import org.springframework.util.ObjectUtils;

/**
 * Custom test context loader for Across applications.
 *
 * @author Arne Vandamme
 * @since 4.0.0
 */
public class AcrossContextLoader extends AbstractContextLoader
{
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private static final Log logger = LogFactory.getLog( AbstractContextLoader.class );

	@Override
	public final ConfigurableApplicationContext loadContext( MergedContextConfiguration mergedConfig ) throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug( String.format( "Loading ApplicationContext for merged context configuration [%s].",
			                             mergedConfig ) );
		}

		validateMergedContextConfiguration( mergedConfig );

		AcrossApplicationContext context = new AcrossApplicationContext();

		ApplicationContext parent = mergedConfig.getParentApplicationContext();
		if ( parent != null ) {
			context.setParent( parent );
		}
		//prepareContext(context);
		prepareContext( context, mergedConfig );
		//customizeBeanFactory(context.getDefaultListableBeanFactory());
		loadBeanDefinitions( context, mergedConfig );
		AnnotationConfigUtils.registerAnnotationConfigProcessors( context );
		//customizeContext(context);
		customizeContext( context, mergedConfig );
		context.refresh();
		context.registerShutdownHook();
		return context;
	}

	@Override
	public ApplicationContext loadContext( String... locations ) throws Exception {
		return null;
	}

	// SmartContextLoader

	/**
	 * Process <em>annotated classes</em> in the supplied {@link ContextConfigurationAttributes}.
	 * <p>If the <em>annotated classes</em> are {@code null} or empty and
	 * {@link #isGenerateDefaultLocations()} returns {@code true}, this
	 * {@code SmartContextLoader} will attempt to {@link
	 * #detectDefaultConfigurationClasses detect default configuration classes}.
	 * If defaults are detected they will be
	 * {@link ContextConfigurationAttributes#setClasses(Class[]) set} in the
	 * supplied configuration attributes. Otherwise, properties in the supplied
	 * configuration attributes will not be modified.
	 *
	 * @param configAttributes the context configuration attributes to process
	 * @see org.springframework.test.context.SmartContextLoader#processContextConfiguration(ContextConfigurationAttributes)
	 * @see #isGenerateDefaultLocations()
	 * @see #detectDefaultConfigurationClasses(Class)
	 */
	@Override
	public void processContextConfiguration( ContextConfigurationAttributes configAttributes ) {
		if ( !configAttributes.hasClasses() && isGenerateDefaultLocations() ) {
			configAttributes.setClasses( detectDefaultConfigurationClasses( configAttributes.getDeclaringClass() ) );
		}
	}

	// AnnotationConfigContextLoader

	/**
	 * Detect the default configuration classes for the supplied test class.
	 * <p>The default implementation simply delegates to
	 * {@link AnnotationConfigContextLoaderUtils#detectDefaultConfigurationClasses(Class)}.
	 *
	 * @param declaringClass the test class that declared {@code @ContextConfiguration}
	 * @return an array of default configuration classes, potentially empty but
	 * never {@code null}
	 * @see AnnotationConfigContextLoaderUtils
	 */
	protected Class<?>[] detectDefaultConfigurationClasses( Class<?> declaringClass ) {
		return AnnotationConfigContextLoaderUtils.detectDefaultConfigurationClasses( declaringClass );
	}

	// AbstractContextLoader

	/**
	 * {@code AnnotationConfigContextLoader} should be used as a
	 * {@link org.springframework.test.context.SmartContextLoader SmartContextLoader},
	 * not as a legacy {@link org.springframework.test.context.ContextLoader ContextLoader}.
	 * Consequently, this method is not supported.
	 *
	 * @throws UnsupportedOperationException in this implementation
	 * @see AbstractContextLoader#modifyLocations
	 */
	@Override
	protected String[] modifyLocations( Class<?> clazz, String... locations ) {
		throw new UnsupportedOperationException(
				"AnnotationConfigContextLoader does not support the modifyLocations(Class, String...) method" );
	}

	/**
	 * {@code AnnotationConfigContextLoader} should be used as a
	 * {@link org.springframework.test.context.SmartContextLoader SmartContextLoader},
	 * not as a legacy {@link org.springframework.test.context.ContextLoader ContextLoader}.
	 * Consequently, this method is not supported.
	 *
	 * @throws UnsupportedOperationException in this implementation
	 * @see AbstractContextLoader#generateDefaultLocations
	 */
	@Override
	protected String[] generateDefaultLocations( Class<?> clazz ) {
		throw new UnsupportedOperationException(
				"AnnotationConfigContextLoader does not support the generateDefaultLocations(Class) method" );
	}

	/**
	 * {@code AnnotationConfigContextLoader} should be used as a
	 * {@link org.springframework.test.context.SmartContextLoader SmartContextLoader},
	 * not as a legacy {@link org.springframework.test.context.ContextLoader ContextLoader}.
	 * Consequently, this method is not supported.
	 *
	 * @throws UnsupportedOperationException in this implementation
	 * @see AbstractContextLoader#getResourceSuffix
	 */
	@Override
	protected String getResourceSuffix() {
		throw new UnsupportedOperationException(
				"AnnotationConfigContextLoader does not support the getResourceSuffix() method" );
	}

	// AbstractGenericContextLoader

	/**
	 * Ensure that the supplied {@link MergedContextConfiguration} does not
	 * contain {@link MergedContextConfiguration#getLocations() locations}.
	 *
	 * @see AbstractGenericContextLoader#validateMergedContextConfiguration
	 * @since 4.0.4
	 */
	protected void validateMergedContextConfiguration( MergedContextConfiguration mergedConfig ) {
		if ( mergedConfig.hasLocations() ) {
			String msg = String.format( "Test class [%s] has been configured with @ContextConfiguration's 'locations' " +
					                            "(or 'value') attribute %s, but %s does not support resource locations.",
			                            mergedConfig.getTestClass().getName(), ObjectUtils.nullSafeToString( mergedConfig.getLocations() ),
			                            getClass().getSimpleName() );
			logger.error( msg );
			throw new IllegalStateException( msg );
		}
	}

	/**
	 * Register classes in the supplied {@link GenericApplicationContext context}
	 * from the classes in the supplied {@link MergedContextConfiguration}.
	 * <p>Each class must represent an <em>annotated class</em>. An
	 * {@link AnnotatedBeanDefinitionReader} is used to register the appropriate
	 * bean definitions.
	 * <p>Note that this method does not call {@link #createBeanDefinitionReader}
	 * since {@code AnnotatedBeanDefinitionReader} is not an instance of
	 * {@link BeanDefinitionReader}.
	 *
	 * @param context      the context in which the annotated classes should be registered
	 * @param mergedConfig the merged configuration from which the classes should be retrieved
	 * @see AbstractGenericContextLoader#loadBeanDefinitions
	 */
	protected void loadBeanDefinitions( GenericApplicationContext context, MergedContextConfiguration mergedConfig ) {
		Class<?>[] annotatedClasses = mergedConfig.getClasses();
		if ( logger.isDebugEnabled() ) {
			logger.debug( "Registering annotated classes: " + ObjectUtils.nullSafeToString( annotatedClasses ) );
		}
		new AnnotatedBeanDefinitionReader( context ).register( annotatedClasses );
	}

	/**
	 * {@code AnnotationConfigContextLoader} should be used as a
	 * {@link org.springframework.test.context.SmartContextLoader SmartContextLoader},
	 * not as a legacy {@link org.springframework.test.context.ContextLoader ContextLoader}.
	 * Consequently, this method is not supported.
	 *
	 * @throws UnsupportedOperationException in this implementation
	 * @see #loadBeanDefinitions
	 * @see AbstractGenericContextLoader#createBeanDefinitionReader
	 */
	protected BeanDefinitionReader createBeanDefinitionReader( GenericApplicationContext context ) {
		throw new UnsupportedOperationException(
				"AnnotationConfigContextLoader does not support the createBeanDefinitionReader(GenericApplicationContext) method" );
	}

}
