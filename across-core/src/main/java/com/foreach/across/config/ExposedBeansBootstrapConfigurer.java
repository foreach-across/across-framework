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

import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Simple {@link AcrossBootstrapConfigurer} that can be used to expose additional classes from modules.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor
public final class ExposedBeansBootstrapConfigurer implements AcrossBootstrapConfigurer
{
	private final Class<?>[] types;

	public ExposedBeansBootstrapConfigurer( @NonNull Collection<Class<?>> types ) {
		this.types = types.toArray( new Class<?>[0] );
	}

	@Override
	public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
		moduleConfiguration.expose( types );
	}
}
