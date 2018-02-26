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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Optional;

/**
 * {@link AcrossContextConfigurer} bean that will add dynamic modules to an {@link AcrossContext}.
 * It will do so by scanning a base package ({@link #setBasePackage(String)} for child packages
 * application, infrastructure and postprocessor.  For each of these found, a module with the respective role
 * will be added.  The name of the module will be determined by the base module name ({@link #setBaseModuleName(String)}).
 * <p>
 * If there is a {@link ModuleDependencyResolver} found in the bean factory, it will be used to resolve the corresponding modules.
 * This way an implemented {@link AcrossModule} descriptor will automatically be picked up.
 * <p>
 * Can also be configured from a single class instance that will be used for determining the base package and base
 * module name.  See also {@link AcrossDynamicModulesConfiguration} for a {@link Configuration} that will use the
 * importing class as base.
 *
 * @author Arne Vandamme
 * @see AcrossDynamicModulesConfiguration
 * @since 1.1.2
 */
@Slf4j
public class AcrossDynamicModulesConfigurer implements AcrossContextConfigurer
{
	private boolean metadataReaderFactoryConfigured = false;
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory( resourcePatternResolver );

	private String basePackage, baseModuleName;
	private ClassPathScanningChildPackageProvider packageProvider;

	public AcrossDynamicModulesConfigurer() {
	}

	public AcrossDynamicModulesConfigurer( Class<?> applicationClass ) {
		setApplicationClass( applicationClass );
	}

	public AcrossDynamicModulesConfigurer( String basePackage, String baseModuleName ) {
		setBasePackage( basePackage );
		setBaseModuleName( baseModuleName );
	}

	/**
	 * The base package that should be scanned for modules.
	 * Can also be set through a single {@link #setApplicationClass(Class)}.
	 *
	 * @param basePackage package name
	 */
	public void setBasePackage( String basePackage ) {
		this.basePackage = basePackage;
	}

	/**
	 * The base module name that should be used.  If dynamic modules are added, their names will be
	 * prefixed with the base module name.  Resulting in either BASEApplicationModule, BASEInfrastructureModule or
	 * BASEPostProcessorModule.
	 *
	 * @param baseModuleName module name prefix
	 */
	public void setBaseModuleName( String baseModuleName ) {
		this.baseModuleName = baseModuleName;
	}

	/**
	 * Set a single class from that is in the base package, and determine a base module name from the class name.
	 * If the simple class name ends with <em>Application</em> (eg. MyCurrentApplication), the part before <em>Application</em>
	 * will be used as base module name (eg. MyCurrent); else the simple class name will be used.
	 *
	 * @param clazz instance
	 */
	public void setApplicationClass( Class<?> clazz ) {
		basePackage = packageName( clazz );
		baseModuleName = baseModuleName( clazz );
	}

	protected String getBasePackage() {
		return basePackage;
	}

	/**
	 * Set the {@link ResourcePatternResolver} that should be used for classpath scanning.
	 * Defaults to a {@link PathMatchingResourcePatternResolver}.
	 *
	 * @param resourcePatternResolver resolver
	 */
	public void setResourcePatternResolver( ResourcePatternResolver resourcePatternResolver ) {
		this.resourcePatternResolver = resourcePatternResolver;
	}

	/**
	 * Set the {@link MetadataReaderFactory} that should be used by the classpath scanner.
	 * Defaults to a new instance of {@link CachingMetadataReaderFactory}.
	 *
	 * @param metadataReaderFactory to use
	 */
	public void setMetadataReaderFactory( MetadataReaderFactory metadataReaderFactory ) {
		metadataReaderFactoryConfigured = true;
		this.metadataReaderFactory = metadataReaderFactory;
	}

	@Override
	public synchronized void configure( AcrossContext context ) {
		if ( basePackage == null || baseModuleName == null ) {
			throw new AcrossException(
					"Unable to add dynamic modules as no basePackage and no baseModuleName have been configured" );
		}

		packageProvider = new ClassPathScanningChildPackageProvider( resourcePatternResolver, metadataReaderFactory );

		String[] children = packageProvider.findChildren( basePackage );

		// always add the application module
		configureApplicationModule( context, basePackage + ".application", baseModuleName );

		if ( hasPackage( children, "infrastructure" ) ) {
			configureInfrastructureModule( context, basePackage + ".infrastructure", baseModuleName );
		}
		if ( hasPackage( children, "postprocessor" ) ) {
			configurePostProcessorModule( context, basePackage + ".postprocessor", baseModuleName );
		}

		if ( !metadataReaderFactoryConfigured && metadataReaderFactory instanceof CachingMetadataReaderFactory ) {
			( (CachingMetadataReaderFactory) metadataReaderFactory ).clearCache();
		}
	}

	private String packageName( Class<?> clazz ) {
		return clazz.getPackage() != null ? clazz.getPackage().getName() : "";
	}

	private boolean hasPackage( String[] packages, String name ) {
		String suffix = "." + name;
		for ( String pkg : packages ) {
			if ( StringUtils.endsWith( pkg, suffix ) ) {
				return true;
			}
		}
		return false;
	}

	private void configureApplicationModule( AcrossContext context, String moduleBasePackage, String baseModuleName ) {
		String applicationModuleName = baseModuleName + "ApplicationModule";
		String resourcesKey = StringUtils.uncapitalize( baseModuleName );

		if ( context.getModule( applicationModuleName ) == null ) {
			Optional<AcrossModule> module = resolveModule( context, applicationModuleName );

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
			Optional<AcrossModule> module = resolveModule( context, infrastructureModule );

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
			Optional<AcrossModule> module = resolveModule( context, postprocessorModule );

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

	private Optional<AcrossModule> resolveModule( AcrossContext context, String moduleName ) {
		ModuleDependencyResolver moduleDependencyResolver = context.getModuleDependencyResolver();

		if ( moduleDependencyResolver != null ) {
			return moduleDependencyResolver.resolveModule( moduleName, true );
		}

		return Optional.empty();
	}

	private String baseModuleName( Class<?> importingClass ) {
		String moduleName = importingClass.getSimpleName();
		if ( StringUtils.endsWith( moduleName, "Application" ) ) {
			return StringUtils.substringBeforeLast( moduleName, "Application" );
		}

		return moduleName;
	}
}
