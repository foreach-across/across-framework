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

import com.foreach.across.boot.AcrossAutoConfigurationImportSelector;
import com.foreach.across.boot.ExtendModuleAutoConfiguration;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation that merges {@link EnableAcrossContext} together with Spring Boot configuration classes
 * for web applications.  Supports most attributes that {@link EnableAcrossContext} does, but allows
 * for minimal configuration of an application.  Optionally adds support for dynamic modules based on the
 * {@link #enableDynamicModules()} value.
 * <p/>
 * NOTE: Even though this class defines {@link EnableAutoConfiguration}, it will explicitly disable auto configuration
 * in the {@link ApplicationModuleImportSelector}.  The annotation is added for better IDE support.
 *
 * @author Arne Vandamme
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableAcrossContext
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({ ApplicationModuleImportSelector.class, AcrossAutoConfigurationImportSelector.class, ExtendModuleAutoConfiguration.class,
          ApplicationAutoConfigurationPackage.class })
public @interface AcrossApplication
{
	/**
	 * Display name that should be used for the context.  If empty, the property value of <strong>across.displayName</strong>
	 * will be used instead.  If no property value is specified, the simple class name of the class that holds the annotation will be used.
	 */
	String displayName() default "";

	/**
	 * If enabled, this will register a {@link AcrossDynamicModulesConfiguration} and will scan for dynamic modules
	 * based on the package of the importing class.
	 */
	boolean enableDynamicModules() default true;

	/**
	 * If enabled, Spring Boot auto-configuration will be attempted.
	 */
	boolean autoConfiguration() default true;

	/**
	 * Array of {@link AcrossModule} names that should be configured if auto configuration is enabled.
	 * These will be added to the {@link AcrossContext} before any configured module beans and before
	 * the {@link AcrossContextConfigurer} instances are called.
	 */
	String[] modules() default {};

	/**
	 * Array of {@link AcrossModule} names that should never be resolved through scanning, no matter the
	 * values of {@link #scanForRequiredModules()} and {@link #scanForOptionalModules()}.
	 */
	String[] excludeFromScanning() default {};

	/**
	 * If auto configuration is enabled, should required modules be scanned for?  All modules configured using the
	 * {@link #modules()} attribute are considered to be required.
	 */
	boolean scanForRequiredModules() default true;

	/**
	 * If auto configuration is enabled, should optional modules be scanned for?
	 */
	boolean scanForOptionalModules() default false;

	/**
	 * Set of packages that should be scanned for modules. The standard modules packages as well as the
	 * package of the importing class will be used as well. If you do not want this, you should add
	 * <strong>.</strong> (dot) as a package name. In that case only explicitly defined module packages
	 * will be used.
	 */
	String[] modulePackages() default {};

	/**
	 * Type-safe alternative to {@link #modulePackages()} for specifying the packages to scan for modules.
	 * The package of each class specified will be scanned.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.</p>
	 * <p>The default packages will always apply as well, if you do not want this you should define
	 * <strong>.</strong> (dot) in {@link #modulePackages()}</p>.
	 */
	Class<?>[] modulePackageClasses() default {};

	/**
	 * Set of packages that should be scanned for {@link com.foreach.across.core.annotations.ModuleConfiguration}
	 * classes.  If empty the sub-packages <strong>config</strong> and <strong>extensions</strong> of the importing
	 * class will be used.
	 */
	String[] moduleConfigurationPackages() default {};

	/**
	 * Type-safe alternative to {@link #moduleConfigurationPackages()} for specifying the packages to scan for
	 * {@link com.foreach.across.core.annotations.ModuleConfiguration} classes.
	 * The package of each class specified will be scanned.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.</p>
	 */
	Class<?>[] moduleConfigurationPackageClasses() default {};

	/**
	 * Array of AutoConfiguration classes that should not be enabled.
	 * The provided classes will be excluded in addition to the exclusions defined in the META-INF/across.configuration file.
	 *
	 * @see com.foreach.across.boot.AcrossApplicationAutoConfiguration
	 */
	Class<?>[] excludeAutoConfigurations() default {};
}
