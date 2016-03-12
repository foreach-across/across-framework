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

import com.foreach.across.test.support.config.MockAcrossServletContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.annotation.*;

/**
 * Custom annotation that extends the default {@link org.springframework.test.context.web.WebAppConfiguration}
 * with adding an initializer that ensures a {@link MockAcrossServletContext} is set
 * on the {@link org.springframework.web.context.WebApplicationContext}.
 * <p>
 * When used in conjunction with {@link AcrossTestConfiguration} this will provide an initialized
 * {@link org.springframework.test.web.servlet.MockMvc} bean that has all dynamically registered filters.
 * The combination of {@link com.foreach.across.test.AcrossWebAppConfiguration} and
 * {@link com.foreach.across.test.AcrossTestConfiguration} is a common setup for integration testing of Across modules.
 * <p>
 * This annotation has aliases for {@link org.springframework.test.context.ContextConfiguration#classes()} and
 * {@link org.springframework.test.context.ContextConfiguration#loader()}.  If you need more advanced customization
 * you should revert to the manual combination of the annotations and adding the
 * {@link MockAcrossServletContextInitializer}.
 *
 * @author Arne Vandamme
 * @see AcrossTestConfiguration
 * @see MockAcrossServletContextInitializer
 * @since 1.1.2
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@WebAppConfiguration
@ContextConfiguration(initializers = MockAcrossServletContextInitializer.class)
public @interface AcrossWebAppConfiguration
{
	/**
	 * The <em>annotated classes</em> to use for loading an
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
	 * <p>Check out the javadoc for
	 * {@link org.springframework.test.context.support.AnnotationConfigContextLoader#detectDefaultConfigurationClasses
	 * AnnotationConfigContextLoader.detectDefaultConfigurationClasses()} for details
	 * on how default configuration classes will be detected if no
	 * <em>annotated classes</em> are specified. See the documentation for
	 * {@link #loader} for further details regarding default loaders.
	 *
	 * @see org.springframework.context.annotation.Configuration
	 * @see org.springframework.test.context.support.AnnotationConfigContextLoader
	 * @since 3.1
	 */
	Class<?>[] classes() default {};

	/**
	 * The type of {@link SmartContextLoader} (or {@link ContextLoader}) to use
	 * for loading an {@link org.springframework.context.ApplicationContext
	 * ApplicationContext}.
	 * <p>If not specified, the loader will be inherited from the first superclass
	 * that is annotated with {@code @ContextConfiguration} and specifies an
	 * explicit loader. If no class in the hierarchy specifies an explicit
	 * loader, a default loader will be used instead.
	 * <p>The default concrete implementation chosen at runtime will be either
	 * {@link org.springframework.test.context.support.DelegatingSmartContextLoader
	 * DelegatingSmartContextLoader} or
	 * {@link org.springframework.test.context.web.WebDelegatingSmartContextLoader
	 * WebDelegatingSmartContextLoader} depending on the absence or presence of
	 * {@link org.springframework.test.context.web.WebAppConfiguration
	 * &#064;WebAppConfiguration}. For further details on the default behavior
	 * of various concrete {@code SmartContextLoaders}, check out the Javadoc for
	 * {@link org.springframework.test.context.support.AbstractContextLoader AbstractContextLoader},
	 * {@link org.springframework.test.context.support.GenericXmlContextLoader GenericXmlContextLoader},
	 * {@link org.springframework.test.context.support.GenericGroovyXmlContextLoader GenericGroovyXmlContextLoader},
	 * {@link org.springframework.test.context.support.AnnotationConfigContextLoader AnnotationConfigContextLoader},
	 * {@link org.springframework.test.context.web.GenericXmlWebContextLoader GenericXmlWebContextLoader},
	 * {@link org.springframework.test.context.web.GenericGroovyXmlWebContextLoader GenericGroovyXmlWebContextLoader}, and
	 * {@link org.springframework.test.context.web.AnnotationConfigWebContextLoader AnnotationConfigWebContextLoader}.
	 *
	 * @since 2.5
	 */
	Class<? extends ContextLoader> loader() default ContextLoader.class;
}
