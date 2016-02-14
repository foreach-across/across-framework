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

package com.foreach.across.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.AbstractAcrossEntity;
import com.foreach.across.core.context.AcrossModuleEntity;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.core.context.configurer.PropertySourcesConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.stereotype.Service;

import java.util.*;

public abstract class AcrossModule extends AbstractAcrossEntity implements AcrossModuleEntity
{
	// The current module (owning the ApplicationContext) can always be referenced under this qualifier
	public static final String CURRENT_MODULE = "across.currentModule";

	private AcrossContext context;

	private BeanFilter exposeFilter = defaultExposeFilter();
	private ExposedBeanDefinitionTransformer exposeTransformer = null;
	private final Set<ApplicationContextConfigurer> applicationContextConfigurers = new LinkedHashSet<>();
	private final Set<ApplicationContextConfigurer> installerContextConfigurers = new LinkedHashSet<>();

	private final Set<String> runtimeDependencies = new HashSet<>();

	private boolean enabled = true;

	private InstallerSettings installerSettings;

	public AcrossModule() {
		registerDefaultApplicationContextConfigurers( applicationContextConfigurers );
		registerDefaultInstallerContextConfigurers( installerContextConfigurers );
	}

	public AcrossContext getContext() {
		return context;
	}

	protected void setContext( AcrossContext context ) {
		this.context = context;
	}

	/**
	 * @return True if this module will be bootstrapped if configured in an AcrossContext.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <p>A module can be attached to an AcrossContext but still not enabled.  In that case
	 * it will not be bootstrapped and other enabled modules depending on this one will cause
	 * the entire bootstrap to fail (dependency misconfiguration).</p>
	 * <p>A disabled module still influences the boostrap order of modules (as if it would be enabled),
	 * but will otherwise have no further impact.</p>
	 * <p>By default a module is enabled.</p>
	 */
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	/**
	 * @return The filter that beans should match to exposed to other modules in the AcrossContext.
	 */
	public BeanFilter getExposeFilter() {
		return exposeFilter;
	}

	/**
	 * Sets the filter that will be used after module bootstrap to copy beans to the parent context and make
	 * them available to other modules in the AcrossContext.
	 *
	 * @param exposeFilter The filter that beans should match to be exposed to other modules in the AcrossContext.
	 */
	public void setExposeFilter( BeanFilter exposeFilter ) {
		this.exposeFilter = exposeFilter;
	}

	/**
	 * Expose beans matching any of the classes.  If the class is an annotation, beans having the annotation
	 * will be matched, otherwise beans assignable to the target class will match.
	 *
	 * @param classOrAnnotations to match
	 */
	public void expose( Class<?>... classOrAnnotations ) {
		setExposeFilter( BeanFilter.composite(
				getExposeFilter(),
				BeanFilter.instances( classOrAnnotations ),
				BeanFilter.annotations( classOrAnnotations )
		) );
	}

	/**
	 * Exposed all beans with the given names.
	 *
	 * @param beanNames that need to be exposed
	 */
	public void expose( String... beanNames ) {
		setExposeFilter( BeanFilter.composite(
				getExposeFilter(),
				BeanFilter.beanNames( beanNames )
		) );
	}

	/**
	 * @return The transformer that will be applied to all exposed beans before copying them to the parent context.
	 */
	public ExposedBeanDefinitionTransformer getExposeTransformer() {
		return exposeTransformer;
	}

	/**
	 * Sets the transformer that will be applied to all exposed beans before actually copying them
	 * to the parent context.
	 *
	 * @param exposeTransformer The transformer that should be applies to all exposed beans.
	 */
	public void setExposeTransformer( ExposedBeanDefinitionTransformer exposeTransformer ) {
		this.exposeTransformer = exposeTransformer;
	}

	/**
	 * Gets the InstallerSettings attached to this module.  If none are attached (default),
	 * this method will return null.  No InstallerSettings mean the settings from the context
	 * will be used.
	 *
	 * @return InstallerSettings instance or null if none.
	 */
	public InstallerSettings getInstallerSettings() {
		return installerSettings;
	}

	/**
	 * Sets the InstallerSettings for this module.  Can be null (default) to ensure that the InstallerSettings
	 * from the context will be used.
	 *
	 * @param installerSettings InstallerSettings instance.
	 */
	public void setInstallerSettings( InstallerSettings installerSettings ) {
		this.installerSettings = installerSettings;
	}

	/**
	 * @return Array containing the installer classes in the order of which they should be run.
	 * @deprecated Signature is subject to change in the future, only class instances should be returned.
	 */
	@Deprecated
	public Object[] getInstallers() {
		return new Object[0];
	}

	/**
	 * The collection of packages that should be scanned for {@link com.foreach.across.core.annotations.Installer}
	 * annotated classes. Defaults to the "installers" package relative to the package of the implementing class.
	 * <p>
	 *
	 * @return Array of package names.
	 * @see #registerDefaultInstallerContextConfigurers(Set)
	 */
	public String[] getInstallerScanPackages() {
		return new String[] { getClass().getPackage().getName() + ".installers" };
	}

	/**
	 * @return Collection of dependencies added explicitly - outside of the module definition.
	 */
	public Collection<String> getRuntimeDependencies() {
		return Collections.unmodifiableCollection( runtimeDependencies );
	}

	/**
	 * Add an explicit required dependency for this module.
	 *
	 * @param moduleName Name of the other module this module should depend on.
	 */
	public void addRuntimeDependency( String moduleName ) {
		runtimeDependencies.add( moduleName );
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new ComponentScanConfigurer( getClass().getPackage().getName() + ".config" ) );
	}

	/**
	 * Register the default {@link ApplicationContextConfigurer} to be added to the global installer
	 * {@link org.springframework.context.ApplicationContext} for this module.
	 * This defaults to the "installers.config" package relative to the package of the implementing class.
	 *
	 * @param installerContextConfigurers Set of existing configurers to add to.
	 */
	protected void registerDefaultInstallerContextConfigurers( Set<ApplicationContextConfigurer> installerContextConfigurers ) {
		installerContextConfigurers.add(
				new ComponentScanConfigurer( getClass().getPackage().getName() + ".installers.config" )
		);
	}

	public Set<ApplicationContextConfigurer> getApplicationContextConfigurers() {
		return applicationContextConfigurers;
	}

	public Set<ApplicationContextConfigurer> getInstallerContextConfigurers() {
		return installerContextConfigurers;
	}

	/**
	 * <p>Add one or more annotated classes to the module ApplicationContext.</p>
	 *
	 * @param annotatedClasses Configuration classes.
	 */
	public void addApplicationContextConfigurer( Class... annotatedClasses ) {
		addApplicationContextConfigurer( new AnnotatedClassConfigurer( annotatedClasses ) );
	}

	/**
	 * <p>Add an ApplicationContextConfigurer to be loaded when the module bootstraps.</p>
	 *
	 * @param configurers Configurer instance.
	 */
	public void addApplicationContextConfigurer( ApplicationContextConfigurer... configurers ) {
		Collections.addAll( applicationContextConfigurers, configurers );
	}

	/**
	 * <p>Add one or more annotated classes to the module installer ApplicationContext.</p>
	 *
	 * @param annotatedClasses Configuration classes.
	 */
	public void addInstallerContextConfigurer( Class... annotatedClasses ) {
		addInstallerContextConfigurer( new AnnotatedClassConfigurer( annotatedClasses ) );
	}

	/**
	 * <p>Add an ApplicationContextConfigurer to configure the module installer ApplicationContext.</p>
	 *
	 * @param configurers Configurer instances.
	 */
	public void addInstallerContextConfigurer( ApplicationContextConfigurer... configurers ) {
		Collections.addAll( installerContextConfigurers, configurers );
	}

	/**
	 * Add PropertySources to the context.
	 *
	 * @param propertySources A PropertySources instance.
	 */
	@Override
	public void addPropertySources( PropertySources propertySources ) {
		addApplicationContextConfigurer( new PropertySourcesConfigurer( propertySources ) );
		addInstallerContextConfigurer( new PropertySourcesConfigurer( propertySources ) );
	}

	/**
	 * Shortcut to add PropertySources to the context.
	 *
	 * @param propertySources One or more PropertySource instances.
	 */
	public void addPropertySources( PropertySource<?>... propertySources ) {
		addApplicationContextConfigurer( new PropertySourcesConfigurer( propertySources ) );
		addInstallerContextConfigurer( new PropertySourcesConfigurer( propertySources ) );
	}

	/**
	 * The resources key is the key identifying the resources location for this module in the larger
	 * set of resources.  By default it is the same as the module name, but it can be overridden to provide
	 * a simpler (eg lowercase) string.
	 *
	 * @return Key (sub path) of the resources for this module.
	 */
	public String getResourcesKey() {
		return getName();
	}

	/**
	 * The collection of packages that should be scanned for module configurations.
	 * Defaults to the "config" and "extension" packages relative to the package of the implementing class.
	 *
	 * @return Array of package names.
	 */
	public String[] getModuleConfigurationScanPackages() {
		return new String[] {
				getClass().getPackage().getName() + ".config",
				getClass().getPackage().getName() + ".extensions"
		};
	}

	/**
	 * An AcrossModule name should not contain any whitespace and should only be ASCII characters.
	 *
	 * @return Name of this module.  Should be unique within a configured AcrossContext.
	 */
	public abstract String getName();

	/**
	 * @return Description of the content of this module.
	 */
	public String getDescription() {
		return "Across module - no description set";
	}

	/**
	 * <p>Called when a context is preparing to bootstrap, but before the actual bootstrap happens.
	 * This is the last chance for a module to modify itself or its siblings before the actual
	 * bootstrapping will occur.</p>
	 * <p>Only modules that will actually bootstrap will be available in the context configuration.
	 * Any disabled modules will not be present.</p>
	 *
	 * @param currentModule Bootstrap configuration of the current module.
	 * @param contextConfig Bootstrap configuration of the entire context.
	 */
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
	                                 AcrossBootstrapConfig contextConfig ) {
	}

	/**
	 * Called after all modules have been installed and - depending on the registration order in the context -
	 * previous modules have been bootstrapped already.
	 */
	public void bootstrap() {
	}

	/**
	 * Called in case of a context shutdown.  Modules registered after this one in the context will have
	 * been shutdown already.
	 */
	public void shutdown() {
	}

	/**
	 * By default all @Service and @Controller beans are exposed, along with any other beans
	 * annotated explicitly with @Exposed or created through an @Exposed BeanFactory.
	 */
	@SuppressWarnings("unchecked")
	public static BeanFilter defaultExposeFilter() {
		return new BeanFilterComposite( new AnnotationBeanFilter( Service.class ),
		                                new AnnotationBeanFilter( true, true, Exposed.class ) );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || !( o instanceof AcrossModule ) ) {
			return false;
		}
		AcrossModule that = (AcrossModule) o;
		return Objects.equals( getName(), that.getName() );
	}

	@Override
	public int hashCode() {
		return Objects.hash( getName() );
	}

	@Override
	public AcrossVersionInfo getVersionInfo() {
		return AcrossVersionInfo.load( getClass() );
	}
}
