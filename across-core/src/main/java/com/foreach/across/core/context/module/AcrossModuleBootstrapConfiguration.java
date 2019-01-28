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

import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.module.AcrossModuleDependencySorter.DependencySpec;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.installers.InstallerReference;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.BeanDefinitionTransformerComposite;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the actual configuration that will be used to bootstrap a module.
 * Internal to the framework, clients usually use either {@link AcrossModuleConfiguration} or {@link MutableAcrossModuleConfiguration}.
 * Built from an {@link AcrossModuleDescriptor} using {@link #from(AcrossModuleDescriptor)}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Setter
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
@SuppressWarnings("WeakerAccess")
public class AcrossModuleBootstrapConfiguration implements MutableAcrossModuleConfiguration
{
	/**
	 * Original {@link AcrossModuleDescriptor} that this configuration was derived from.
	 */
	@NonNull
	private final AcrossModuleDescriptor moduleDescriptor;

	/**
	 * Extensions that have been applied.
	 */
	@Getter
	private final Collection<AcrossModuleConfiguration> extensions = new ArrayList<>();

	/**
	 * Collection of {@link ApplicationContextConfigurer} instances that should be loaded in the
	 * {@link org.springframework.context.ApplicationContext} of this module.
	 */
	private final Collection<ApplicationContextConfigurer> applicationContextConfigurers = new LinkedHashSet<>();

	/**
	 * Collection of package names that should be scanned
	 * for {@link com.foreach.across.core.annotations.Installer} annotated classes.
	 */
	private final Collection<String> installerScanPackages = new LinkedHashSet<>();

	private InstallerSettings installerSettings;

	/**
	 * Collection of {@link ApplicationContextConfigurer} that should be loaded in the
	 * installer {@link org.springframework.context.ApplicationContext} of this module.
	 */
	private final Collection<ApplicationContextConfigurer> installerContextConfigurers = new LinkedHashSet<>();

	/**
	 * Collection of package names that should be scanned for {@link com.foreach.across.core.annotations.ModuleConfiguration} annotated classes.
	 */
	private final Collection<String> moduleConfigurationScanPackages = new LinkedHashSet<>();

	/**
	 * Initial properties attached to this module.
	 */
	private final Map<String, Object> properties = new LinkedHashMap<>();

	/**
	 * List of installers that should be considered for execution during the bootstrapping of this module.
	 */
	private final Collection<InstallerReference> installers = new LinkedHashSet<>();

	private BeanFilter exposeFilter;

	private ExposedBeanDefinitionTransformer exposeTransformer;

	/**
	 *
	 */
	private final Set<String> excludedAnnotatedClasses = new LinkedHashSet<>();

	@Getter
	@Setter
	@NonNull
	private Set<String> configurationsToImport = new LinkedHashSet<>();

	/**
	 * Add a collection of context configurers to this module.
	 *
	 * @param configurers to add
	 */
	public AcrossModuleBootstrapConfiguration addApplicationContextConfigurers( ApplicationContextConfigurer... configurers ) {
		return addApplicationContextConfigurers( Arrays.asList( configurers ) );
	}

	/**
	 * Add a collection of context configurers to this module.
	 *
	 * @param configurers to add
	 */
	public AcrossModuleBootstrapConfiguration addApplicationContextConfigurers( @NonNull Collection<ApplicationContextConfigurer> configurers ) {
		applicationContextConfigurers.addAll( configurers );
		return this;
	}

	/**
	 * Clear all previously registered context configurers.
	 */
	public AcrossModuleBootstrapConfiguration clearApplicationContextConfigurers() {
		applicationContextConfigurers.clear();
		return this;
	}

	/**
	 * Add a collection of context configurers for the installers.
	 *
	 * @param configurers to add
	 */
	public AcrossModuleBootstrapConfiguration addInstallerContextConfigurers( ApplicationContextConfigurer... configurers ) {
		return addInstallerContextConfigurers( Arrays.asList( configurers ) );
	}

	/**
	 * Add a collection of context configurers for the installers.
	 *
	 * @param configurers to add
	 */
	public AcrossModuleBootstrapConfiguration addInstallerContextConfigurers( @NonNull Collection<ApplicationContextConfigurer> configurers ) {
		installerContextConfigurers.addAll( configurers );
		return this;
	}

	/**
	 * Clear all previously registered installer context configurers.
	 */
	public AcrossModuleBootstrapConfiguration clearInstallerContextConfigurers() {
		installerContextConfigurers.clear();
		return this;
	}

	/**
	 * Add additional packages that should be scanned for installers.
	 *
	 * @param installerScanPackages to add
	 */
	public AcrossModuleBootstrapConfiguration addInstallerScanPackages( String... installerScanPackages ) {
		return addInstallerScanPackages( Arrays.asList( installerScanPackages ) );
	}

	/**
	 * Add additional packages that should be scanned for installers.
	 *
	 * @param installerScanPackages to add
	 */
	public AcrossModuleBootstrapConfiguration addInstallerScanPackages( @NonNull Collection<String> installerScanPackages ) {
		this.installerScanPackages.addAll( installerScanPackages );
		return this;
	}

	/**
	 * Clear all previously registered installer scan packages.
	 */
	public AcrossModuleBootstrapConfiguration clearInstallerScanPackages() {
		installerScanPackages.clear();
		return this;
	}

	/**
	 * Add additional packages to scan for {@link com.foreach.across.core.annotations.ModuleConfiguration} classes (module extensions).
	 *
	 * @param moduleConfigurationScanPackages to add
	 */
	public AcrossModuleBootstrapConfiguration addModuleConfigurationScanPackages( String... moduleConfigurationScanPackages ) {
		return addModuleConfigurationScanPackages( Arrays.asList( moduleConfigurationScanPackages ) );
	}

	/**
	 * Add additional packages to scan for {@link com.foreach.across.core.annotations.ModuleConfiguration} classes (module extensions).
	 *
	 * @param moduleConfigurationScanPackages to add
	 */
	public AcrossModuleBootstrapConfiguration addModuleConfigurationScanPackages( @NonNull Collection<String> moduleConfigurationScanPackages ) {
		this.moduleConfigurationScanPackages.addAll( moduleConfigurationScanPackages );
		return this;
	}

	/**
	 * Clear all previously registered module configuration scan packages.
	 */
	public AcrossModuleBootstrapConfiguration clearModuleConfigurationScanPackages() {
		moduleConfigurationScanPackages.clear();
		return this;
	}

	/**
	 * Set property values for this module.
	 *
	 * @param properties to set
	 */
	public AcrossModuleBootstrapConfiguration addProperties( @NonNull Map<String, Object> properties ) {
		this.properties.putAll( properties );
		return this;
	}

	/**
	 * Add a single property value.
	 *
	 * @param key   property key
	 * @param value property value
	 */
	public AcrossModuleBootstrapConfiguration addProperty( @NonNull String key, @Nullable Object value ) {
		this.properties.put( key, value );
		return this;
	}

	/**
	 * Clear all previously registered properties.
	 */
	public AcrossModuleBootstrapConfiguration clearProperties() {
		properties.clear();
		return this;
	}

	/**
	 * Add installers that should be considered when starting this module.
	 *
	 * @param installers to add
	 */
	public AcrossModuleBootstrapConfiguration addInstallers( InstallerReference... installers ) {
		return addInstallers( Arrays.asList( installers ) );
	}

	/**
	 * Add installers that should be considered when starting this module.
	 *
	 * @param installers to add
	 */
	public AcrossModuleBootstrapConfiguration addInstallers( @NonNull Collection<InstallerReference> installers ) {
		this.installers.addAll( installers );
		return this;
	}

	/**
	 * Clear all previously registered installers.
	 */
	public AcrossModuleBootstrapConfiguration clearInstallers() {
		installers.clear();
		return this;
	}

	/**
	 * Installer settings for this module.
	 */
	public Optional<InstallerSettings> getInstallerSettings() {
		return Optional.ofNullable( installerSettings );
	}

	/**
	 * Add an additional expose filter but keeps any previously registered.
	 *
	 * @param beanFilter to add
	 */
	public AcrossModuleBootstrapConfiguration addExposeFilter( @NonNull BeanFilter beanFilter ) {
		if ( exposeFilter != null ) {
			exposeFilter = BeanFilter.composite( exposeFilter, beanFilter );
		}
		else {
			exposeFilter = beanFilter;
		}
		return this;
	}

	/**
	 * Add an additional expose transformer but keeps any previously registered.
	 *
	 * @param transformer to add
	 */
	public AcrossModuleBootstrapConfiguration addExposeTransformer( @NonNull ExposedBeanDefinitionTransformer transformer ) {
		if ( exposeTransformer != null ) {
			exposeTransformer = new BeanDefinitionTransformerComposite( exposeTransformer, transformer );
		}
		else {
			exposeTransformer = transformer;
		}
		return this;
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

	DependencySpec toDependencySpec() {
		List<AcrossModuleDescriptor> descriptors = new ArrayList<>( 1 + extensions.size() );
		descriptors.add( moduleDescriptor );
		descriptors.addAll( extensions.stream().map( AcrossModuleConfiguration::getModuleDescriptor ).collect( Collectors.toList() ) );

		Set<String> moduleNames = new LinkedHashSet<>();
		Set<String> requiredDependencies = new LinkedHashSet<>();
		Set<String> optionalDependencies = new LinkedHashSet<>();

		descriptors.forEach( descriptor -> {
			moduleNames.add( descriptor.getModuleName() );
			moduleNames.addAll( descriptor.getModuleNameAliases() );
			requiredDependencies.addAll( descriptor.getRequiredModules() );
			optionalDependencies.addAll( descriptor.getOptionalModules() );
		} );

		return DependencySpec.builder()
		                     .names( moduleNames )
		                     .requiredDependencies( requiredDependencies )
		                     .optionalDependencies( optionalDependencies )
		                     .role( moduleDescriptor.getModuleRole() )
		                     .orderInRole( moduleDescriptor.getOrderInModuleRole() )
		                     .build();
	}

	/**
	 * Add a module configuration extension, this configuration should be loaded in the same module,
	 * after the initial configuration and any previously registered extensions.
	 *
	 * @param configuration to extend the current configuration with
	 */
	@Override
	public AcrossModuleBootstrapConfiguration addExtension( @NonNull AcrossModuleConfiguration configuration ) {
		Assert.isTrue( configuration != this, "Should not extend a module configuration with itself" );
		extensions.add( configuration );
		return this;
	}

	/**
	 * Convert a module descriptor into an equivalent (modifiable) configuration.
	 *
	 * @param descriptor original module descriptor
	 * @return configuration
	 */
	static AcrossModuleBootstrapConfiguration from( @NonNull AcrossModuleDescriptor descriptor ) {
		return new AcrossModuleBootstrapConfiguration( descriptor )
				.addApplicationContextConfigurers( descriptor.getApplicationContextConfigurers() )
				.addInstallerContextConfigurers( descriptor.getInstallerContextConfigurers() )
				.addInstallerScanPackages( descriptor.getInstallerScanPackages() )
				.addModuleConfigurationScanPackages( descriptor.getModuleConfigurationScanPackages() )
				.setInstallerSettings( descriptor.getInstallerSettings().orElse( null ) )
				.setExposeFilter( descriptor.getExposeFilter().orElse( null ) )
				.setExposeTransformer( descriptor.getExposeTransformer().orElse( null ) )
				.addProperties( descriptor.getProperties() )
				.addInstallers( descriptor.getInstallers() );
	}
}