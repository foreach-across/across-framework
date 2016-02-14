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
import com.foreach.across.core.DynamicAcrossModuleFactory;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ClassPathScanningChildPackageProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

/**
 * When imported on a configuration class, this implementation will attempt to dynamically add generated modules.
 * The name and package of the importing class will be used as a basis.  This config will scan for child packages
 * application, infrastructure and postprocessor.  For each of these found, a module with the respective role
 * will be added.  The name of the module will be based on the name of the importing class.
 *
 * @author Arne Vandamme
 */
@Configuration
public class DynamicApplicationModulesConfiguration implements AcrossContextConfigurer, ImportAware
{
	private static final Logger LOG = LoggerFactory.getLogger( DynamicApplicationModulesConfiguration.class );

	private AnnotationMetadata importMetadata;

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		this.importMetadata = importMetadata;
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

		if ( context.getModule( applicationModuleName ) == null ) {
			String resourcesKey = StringUtils.uncapitalize( baseModuleName );

			LOG.info( "Adding package based application module {}, resources: {}, base package: {}",
			          applicationModuleName, resourcesKey, moduleBasePackage );

			DynamicAcrossModuleFactory factory = new DynamicAcrossModuleFactory()
					.setModuleRole( AcrossModuleRole.APPLICATION )
					.setModuleName( applicationModuleName )
					.setResourcesKey( resourcesKey )
					.setBasePackage( moduleBasePackage );

			try {
				context.addModule( factory.getObject() );
			}
			catch ( Exception e ) {
				LOG.error( "Unable to add package based application module", e );
			}
		}
	}

	private void configureInfrastructureModule( AcrossContext context,
	                                            String moduleBasePackage,
	                                            String baseModuleName ) {
		String applicationModuleName = baseModuleName + "InfrastructureModule";

		if ( context.getModule( applicationModuleName ) == null ) {
			String resourcesKey = StringUtils.uncapitalize( baseModuleName + "Infrastructure" );

			LOG.info( "Adding package based infrastructure module {}, resources: {}, base package: {}",
			          applicationModuleName, resourcesKey, moduleBasePackage );

			DynamicAcrossModuleFactory factory = new DynamicAcrossModuleFactory()
					.setModuleRole( AcrossModuleRole.INFRASTRUCTURE )
					.setModuleName( applicationModuleName )
					.setResourcesKey( resourcesKey )
					.setBasePackage( moduleBasePackage );

			try {
				context.addModule( factory.getObject() );
			}
			catch ( Exception e ) {
				LOG.error( "Unable to add package based infrastructure module", e );
			}
		}
	}

	private void configurePostProcessorModule( AcrossContext context,
	                                           String moduleBasePackage,
	                                           String baseModuleName ) {
		String applicationModuleName = baseModuleName + "PostProcessorModule";

		if ( context.getModule( applicationModuleName ) == null ) {
			String resourcesKey = StringUtils.uncapitalize( baseModuleName + "PostProcessor" );

			LOG.info( "Adding package based postprocessor module {}, resources: {}, base package: {}",
			          applicationModuleName, resourcesKey, moduleBasePackage );

			DynamicAcrossModuleFactory factory = new DynamicAcrossModuleFactory()
					.setModuleRole( AcrossModuleRole.POSTPROCESSOR )
					.setModuleName( applicationModuleName )
					.setResourcesKey( resourcesKey )
					.setBasePackage( moduleBasePackage );

			try {
				context.addModule( factory.getObject() );
			}
			catch ( Exception e ) {
				LOG.error( "Unable to add package based postprocessor module", e );
			}
		}
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
