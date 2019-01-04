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
package com.foreach.across;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;

/**
 * Custom {@link SpringApplication} extension that will immediately use an {@link AcrossListableBeanFactory}
 * as the bean factory for the root {@link org.springframework.context.ApplicationContext}. This ensures it is no
 * longer required to introduce an artificial parent bean factory in the hierarchy, as the root factory
 * will support exposed beans.
 * <p/>
 * {@code AcrossApplicationRunner} should be a drop-in replacement for {@link SpringApplication}, which can
 * reduce some overhead of an Across application.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@SuppressWarnings( "unused" )
public class AcrossApplicationRunner extends SpringApplication
{
	public AcrossApplicationRunner( Class<?>... primarySources ) {
		super( primarySources );
	}

	public AcrossApplicationRunner( ResourceLoader resourceLoader, Class<?>... primarySources ) {
		super( resourceLoader, primarySources );
	}

	@Override
	@SneakyThrows
	protected ConfigurableApplicationContext createApplicationContext() {
		ConfigurableApplicationContext context = super.createApplicationContext();
		Class<? extends ConfigurableApplicationContext> actualClass = context.getClass();

		// create a new instance using the custom bean factory instead
		Constructor<? extends ConfigurableApplicationContext> constructor = actualClass.getConstructor( DefaultListableBeanFactory.class );
		Assert.notNull( constructor, "no constructor for DefaultListableBeanFactory" );

		return constructor.newInstance( new AcrossListableBeanFactory() );
	}

	/**
	 * Static helper that can be used to run an {@link AcrossApplicationRunner} from the
	 * specified source using default settings.
	 *
	 * @param primarySource the primary source to load
	 * @param args          the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ConfigurableApplicationContext run( Class<?> primarySource, String... args ) {
		return run( new Class<?>[] { primarySource }, args );
	}

	/**
	 * Static helper that can be used to run an {@link AcrossApplicationRunner} from the
	 * specified sources using default settings and user supplied arguments.
	 *
	 * @param primarySources the primary sources to load
	 * @param args           the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}
	 */
	public static ConfigurableApplicationContext run( Class<?>[] primarySources, String[] args ) {
		return new AcrossApplicationRunner( primarySources ).run( args );
	}
}
