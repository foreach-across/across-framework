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
package com.foreach.across.test.support.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.support.AcrossContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Collections;

/**
 * Configuration class that will extend a configured {@link AcrossContextBuilder} by adding all
 * {@link com.foreach.across.config.AcrossContextConfigurer} beans from the
 * {@link org.springframework.context.ApplicationContext} it resides in.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
@Configuration
public class AcrossContextBuilderConfigurerRegistrar
{
	@Autowired(required = false)
	private Collection<AcrossContextConfigurer> configurers = Collections.emptyList();

	@Autowired
	public void registerConfigurers( AcrossContextBuilder contextBuilder ) {
		contextBuilder.configurer( configurers.toArray( new AcrossContextConfigurer[0] ) );
	}
}
