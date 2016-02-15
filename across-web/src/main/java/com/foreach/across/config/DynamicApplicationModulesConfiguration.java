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
package com.foreach.across.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.DynamicAcrossModuleFactory;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ClassPathScanningChildPackageProvider;
import com.foreach.across.core.context.ModuleDependencyResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Optional;

/**
 * When imported on a configuration class, this implementation will attempt to dynamically add generated modules.
 * The name and package of the importing class will be used as a basis.  This config will scan for child packages
 * application, infrastructure and postprocessor.  For each of these found, a module with the respective role
 * will be added.  The name of the module will be based on the name of the importing class.
 *
 * @author Arne Vandamme
 */
@Configuration
public class DynamicApplicationModulesConfiguration implements AcrossContextConfigurer, BeanFactoryAware, ImportAware
{
	private static final Logger LOG = LoggerFactory.getLogger( DynamicApplicationModulesConfiguration.class );

	private AnnotationMetadata importMetadata;

	private BeanFactory beanFactory;

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		this.importMetadata = importMetadata;
	}

	@Override
	public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void configure( AcrossContext context ) {
		Class<?> importingClass = importingClass();

		String basePackage = importingClass.getPackage().getName();
		String baseModuleName = baseModuleName( importingClass );

		ClassPathScanningChildPackageProvider packageProvider = new ClassPathScanningChildPackageProvider();
		String[] children = packageProvider.findChildren( basePackage );

		if ( ArrayUtils.contains( children, "application" ) ) {
			configureApplicationModule( context, basePackage + ".application", baseModuleName );
		}
		if ( ArrayUtils.contains( children, "infrastructure" ) ) {
			configureInfrastructureModule( context, basePackage + ".infrastructure", baseModuleName );
		}
		if ( ArrayUtils.contains( children, "postprocessor" ) ) {
			configurePostProcessorModule( context, basePackage + ".postprocessor", baseModuleName );
		}
	}

	private void configureApplicationModule( AcrossContext context, String moduleBasePackage, String baseModuleName ) {
		String applicationModuleName = baseModuleName + "ApplicationModule";
		String resourcesKey = StringUtils.uncapitalize( baseModuleName );

		if ( context.getModule( applicationModuleName ) == null ) {
			Optional<AcrossModule> module = resolveModule( applicationModuleName );

			context.addModule( module.orElseGet( () -> dynamicModule(
					AcrossModuleRole.APPLICATION,
					applicationModuleName,
					resourcesKey,
					moduleBasePackage
			) ) );
		}
	}

	private void configureInfrastructureModule( AcrossContext context,
	                                            String moduleBasePackage,
	                                            String baseModuleName ) {
		String infrastructureModule = baseModuleName + "InfrastructureModule";
		String resourcesKey = StringUtils.uncapitalize( baseModuleName + "Infrastructure" );

		if ( context.getModule( infrastructureModule ) == null ) {
			Optional<AcrossModule> module = resolveModule( infrastructureModule );

			context.addModule( module.orElseGet( () -> dynamicModule(
					AcrossModuleRole.INFRASTRUCTURE,
					infrastructureModule,
					resourcesKey,
					moduleBasePackage
			) ) );
		}
	}

	private void configurePostProcessorModule( AcrossContext context,
	                                           String moduleBasePackage,
	                                           String baseModuleName ) {
		String postprocessorModule = baseModuleName + "PostProcessorModule";
		String resourcesKey = StringUtils.uncapitalize( baseModuleName + "PostProcessor" );

		if ( context.getModule( postprocessorModule ) == null ) {
			Optional<AcrossModule> module = resolveModule( postprocessorModule );

			context.addModule( module.orElseGet( () -> dynamicModule(
					AcrossModuleRole.POSTPROCESSOR,
					postprocessorModule,
					resourcesKey,
					moduleBasePackage
			) ) );
		}
	}

	private AcrossModule dynamicModule( AcrossModuleRole moduleRole,
	                                    String moduleName,
	                                    String resourcesKey,
	                                    String moduleBasePackage ) {
		LOG.info( "Adding package based {} module {}, resources: {}, base package: {}",
		          moduleRole.name(), moduleName, resourcesKey, moduleBasePackage );

		DynamicAcrossModuleFactory factory = new DynamicAcrossModuleFactory()
				.setModuleRole( moduleRole )
				.setModuleName( moduleName )
				.setResourcesKey( resourcesKey )
				.setBasePackage( moduleBasePackage );

		try {
			return factory.getObject();
		}
		catch ( Exception e ) {
			throw new AcrossException( "Unable to create package based module", e );
		}
	}

	private Optional<AcrossModule> resolveModule( String moduleName ) {
		try {
			return beanFactory.getBean( ModuleDependencyResolver.class ).resolveModule( moduleName, true );
		}
		catch ( NoSuchBeanDefinitionException ignore ) {
		}

		return Optional.empty();
	}

	private Class<?> importingClass() {
		try {
			return Class.forName( importMetadata.getClassName() );
		}
		catch ( ClassNotFoundException cnfe ) {
			throw new AcrossException( "Unable to configure dynamic application modules" );
		}
	}

	private String baseModuleName( Class<?> importingClass ) {
		String moduleName = importingClass.getSimpleName();
		if ( StringUtils.endsWith( moduleName, "Application" ) ) {
			return StringUtils.substringBeforeLast( moduleName, "Application" );
		}

		return moduleName;
	}
}
