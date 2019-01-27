/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.module;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossVersionInfo;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.module.AcrossModuleDependencySorter.DependencySpec;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.installers.InstallerReference;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import lombok.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the original descriptor of an Across module, as defined by an {@link com.foreach.across.core.AcrossModule} implementation.
 * A module extension will always have its own separate descriptor.
 * <p/>
 * If you are looking for the modifiable configuration of a module, see {@link AcrossModuleConfiguration} instead.
 *
 * @author Arne Vandamme
 * @see AcrossModuleConfiguration
 * @since 5.0.0
 */
@Builder(toBuilder = true)
@EqualsAndHashCode
@Getter
public class AcrossModuleDescriptor
{
	/**
	 * The (usually unique) name of the module.
	 */
	@NonNull
	private final String moduleName;

	/**
	 * An optional list of aliases that this module serves for.
	 * Can be used to take the identity of multiple other modules.
	 */
	@NonNull
	@Singular
	private final Collection<String> moduleNameAliases;

	/**
	 * The resources key for the module.
	 */
	@NonNull
	private final String resourcesKey;

	/**
	 * Is this module enabled (should bootstrap).
	 */
	@Builder.Default
	private final boolean enabled = true;

	private final BeanFilter exposeFilter;

	private final ExposedBeanDefinitionTransformer exposeTransformer;

	/**
	 * Collection of {@link ApplicationContextConfigurer} that should be loaded in the
	 * {@code org.springframework.context.ApplicationContext} of this module.
	 */
	@Singular
	private final Collection<ApplicationContextConfigurer> applicationContextConfigurers;

	/**
	 * Collection of package names that should be scanned
	 * for {@link com.foreach.across.core.annotations.Installer} annotated classes.
	 */
	@Singular
	private final Collection<String> installerScanPackages;

	private final InstallerSettings installerSettings;

	/**
	 * Collection of {@link ApplicationContextConfigurer} that should be loaded in the
	 * installer {@code org.springframework.context.ApplicationContext} of this module.
	 */
	@Singular
	private final Collection<ApplicationContextConfigurer> installerContextConfigurers;

	/**
	 * Collection of package names that should be scanned for {@link com.foreach.across.core.annotations.ModuleConfiguration} annotated classes.
	 */
	@Singular
	private final Collection<String> moduleConfigurationScanPackages;

	/**
	 * Initial properties attached to this module.
	 */
	@Singular
	private final Map<String, Object> properties;

	/**
	 * Version information for this descriptor.
	 */
	@Builder.Default
	private final AcrossVersionInfo versionInfo = AcrossVersionInfo.UNKNOWN;

	/**
	 * Names of the modules that are required dependencies and must be present in the context.
	 */
	@Singular
	private final Collection<String> requiredModules;

	/**
	 * Names of the modules that are optional dependencies.
	 */
	@Singular
	private final Collection<String> optionalModules;

	/**
	 * Names of the modules that this descriptor extends, only the first module present will be extended.
	 */
	@Singular
	private final Collection<String> extensionTargets;

	/**
	 * List of installers that should be considered for execution during the bootstrapping of this module.
	 */
	@Singular
	private final Collection<InstallerReference> installers;

	@Builder.Default
	private final AcrossModuleRole moduleRole = AcrossModuleRole.APPLICATION;

	@Builder.Default
	private final int orderInModuleRole = 0;

	/**
	 * Initial installer settings for this module.
	 */
	public Optional<InstallerSettings> getInstallerSettings() {
		return Optional.ofNullable( installerSettings );
	}

	/**
	 * Custom filter that should be used to determine which beans should be exposed.
	 */
	public Optional<BeanFilter> getExposeFilter() {
		return Optional.ofNullable( exposeFilter );
	}

	/**
	 * Transformer that should be applied to all beans that are exposed.
	 */
	public Optional<ExposedBeanDefinitionTransformer> getExposeTransformer() {
		return Optional.ofNullable( exposeTransformer );
	}

	/**
	 * Is this a regular Across module, or an extension for another module?
	 * If {@code true} then {@link #getExtensionTargets()} will not be empty.
	 *
	 * @return true if one or more extension targets are registered
	 */
	public boolean isExtensionModule() {
		return !extensionTargets.isEmpty();
	}

	DependencySpec toDependencySpec() {
		return DependencySpec.builder()
		                     .name( getModuleName() )
		                     .names( getModuleNameAliases() )
		                     .requiredDependencies( getRequiredModules() )
		                     .optionalDependencies( getOptionalModules() )
		                     .role( getModuleRole() )
		                     .orderInRole( getOrderInModuleRole() )
		                     .build();
	}

	/**
	 * Converts an {@link AcrossModule} into a readonly descriptor.
	 *
	 * @param acrossModule to convert
	 * @return descriptor for the same rules
	 */
	@org.springframework.lang.NonNull
	public static AcrossModuleDescriptor from( @org.springframework.lang.NonNull @NonNull AcrossModule acrossModule ) {
		acrossModule.getInstallers();

		List<String> requiredModules = new ArrayList<>();
		List<String> optionalModules = new ArrayList<>();

		Annotation dependencies = AnnotationUtils.getAnnotation( acrossModule.getClass(), AcrossDepends.class );

		if ( dependencies != null ) {
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes( dependencies );
			requiredModules.addAll( Arrays.asList( (String[]) attributes.get( "required" ) ) );
			optionalModules.addAll( Arrays.asList( (String[]) attributes.get( "optional" ) ) );
		}

		requiredModules.addAll( acrossModule.getRuntimeDependencies() );

		AcrossModuleRole moduleRole = AcrossModuleRole.APPLICATION;
		int orderInRole = 0;
		Annotation roleAnnotation = AnnotationUtils.getAnnotation( acrossModule.getClass(), AcrossRole.class );

		if ( roleAnnotation != null ) {
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes( roleAnnotation );
			moduleRole = (AcrossModuleRole) attributes.get( "value" );
			orderInRole = (int) attributes.get( "order" );
		}

		if ( acrossModule instanceof Ordered ) {
			orderInRole = ( (Ordered) acrossModule ).getOrder();
		}

		HashMap<String, Object> properties = new HashMap<>();
		acrossModule.getProperties().forEach( ( k, v ) -> properties.put( (String) k, v ) );

		return AcrossModuleDescriptor.builder()
		                             .moduleName( acrossModule.getName() )
		                             .resourcesKey( acrossModule.getResourcesKey() )
		                             .applicationContextConfigurers( acrossModule.getApplicationContextConfigurers() )
		                             .installerContextConfigurers( acrossModule.getInstallerContextConfigurers() )
		                             .installerScanPackages( Arrays.asList( acrossModule.getInstallerScanPackages() ) )
		                             .installers(
				                             Arrays.stream( acrossModule.getInstallers() )
				                                   .map( InstallerReference::from )
				                                   .collect( Collectors.toList() )
		                             )
		                             .moduleConfigurationScanPackages( Arrays.asList( acrossModule.getModuleConfigurationScanPackages() ) )
		                             .requiredModules( requiredModules )
		                             .optionalModules( optionalModules )
		                             .properties( properties )
		                             .installerSettings( acrossModule.getInstallerSettings() )
		                             .exposeFilter( acrossModule.getExposeFilter() )
		                             .exposeTransformer( acrossModule.getExposeTransformer() )
		                             .versionInfo( acrossModule.getVersionInfo() )
		                             .moduleRole( moduleRole )
		                             .orderInModuleRole( orderInRole )
		                             .extensionTargets( Arrays.asList( acrossModule.getExtensionTargets() ) )
		                             .enabled( acrossModule.isEnabled() )
		                             .build();
	}
}
