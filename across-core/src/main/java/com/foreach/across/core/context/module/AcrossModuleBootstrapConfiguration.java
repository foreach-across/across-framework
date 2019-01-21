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
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.installers.InstallerReference;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import lombok.*;

import java.util.*;

/**
 * Represents the actual configuration that will be used to bootstrap a module.
 * Internal to the framework, clients usually use either {@link AcrossModuleConfiguration} or {@link MutableAcrossModuleConfiguration}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Setter
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
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
	 * Names of the modules that are required dependencies and must be present in the context.
	 */
	private final Collection<String> requiredModules = new LinkedHashSet<>();

	/**
	 * Names of the modules that are optional dependencies.
	 */
	private final Collection<String> optionalModules = new LinkedHashSet<>();

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
	 * Installer settings for this module.
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

	@Override
	public void extendWith( @NonNull AcrossModuleConfiguration configuration ) {
		extensions.add( configuration );
	}
}