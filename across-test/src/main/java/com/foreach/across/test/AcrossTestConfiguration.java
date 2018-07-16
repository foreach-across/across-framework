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

package com.foreach.across.test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver;
import com.foreach.across.test.support.config.MockMvcConfiguration;
import com.foreach.across.test.support.config.ResetDatabaseConfigurer;
import com.foreach.across.test.support.config.TestDataSourceConfigurer;
import com.foreach.across.test.support.config.TestDataSourcePostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Custom annotation that provides most of the attributes that {@link EnableAcrossContext} does, but adds
 * some test-related configurations.  If no datasources are configured, a test datasource will be used.
 * See the javadoc for {@link TestDataSourceConfigurer} for more information.
 * <p>WARNING: Do not use this annotation when configuring a context with a real-life database.
 * Using this annotation will add configurers that will reset both the across datasource and optional
 * installer datasource before the context is bootstrapped.  All tables in the databases will be dropped.</p>
 * <p>When used in conjunction with {@link AcrossWebAppConfiguration}, this configuration will also
 * provide a {@link org.springframework.test.web.servlet.MockMvc} instance initialized with all filters
 * registered by Across modules. </p>
 * <p>Any class marked with this annotation is also marked with {@link Configuration} automatically.</p>
 *
 * @see AcrossWebAppConfiguration
 * @see TestDataSourceConfigurer
 * @see ResetDatabaseConfigurer
 * @see MockMvcConfiguration
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Configuration
@EnableAcrossContext
@ExposeForTest
@Import({ TestDataSourceConfigurer.class, TestDataSourcePostProcessor.class, ResetDatabaseConfigurer.class, MockMvcConfiguration.class })
public @interface AcrossTestConfiguration
{
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
	 * Configures auto configuration of the {@link AcrossContext}.  Auto configuration means {@link AcrossModule}
	 * beans from the parent {@link ApplicationContext} will be picked up and class path scanning for modules by
	 * names will be supported.  The configured context will have a {@link ClassPathScanningModuleDependencyResolver}
	 * set to resolve the module dependencies as well.
	 * <p>
	 * The ability to disable auto configuration is mainly kept for compatibility reasons.
	 */
	boolean autoConfigure() default true;

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
	 * Set of packages that should be scanned for modules.  If empty the standard modules packages as well as the
	 * package of the importing class will be used.
	 */
	String[] modulePackages() default {};

	/**
	 * Type-safe alternative to {@link #modulePackages()} for specifying the packages to scan for modules.
	 * The package of each class specified will be scanned.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.</p>
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
	 * Specify a collection of types, interfaces or annotations that should be exposed by all modules.
	 * Useful if you want to exposed components only for integration test purposes, especially for
	 * dynamic application modules which might not have anything exposed otherwise.
	 */
	@AliasFor(annotation = ExposeForTest.class, attribute = "value")
	Class<?>[] expose() default {};
}
