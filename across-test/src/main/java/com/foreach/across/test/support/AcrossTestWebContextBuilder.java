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
package com.foreach.across.test.support;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.test.AcrossTestWebContext;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.Properties;

/**
 * Builder for creating an {@link AcrossTestWebContext}.
 * This builder allows easy configuration of properties and modules to add to an {@link AcrossTestWebContext}.
 * <p>
 * Once {@link #build()} has been called, the {@link AcrossTestWebContext} will be created and the internal
 * {@link com.foreach.across.core.AcrossContext} bootstrapped.
 *
 * @author Arne Vandamme
 * @since feb 2016
 */
public class AcrossTestWebContextBuilder extends AcrossTestContextBuilder
{
	@Override
	public AcrossTestWebContextBuilder configurer( AcrossContextConfigurer... configurer ) {
		return (AcrossTestWebContextBuilder) super.configurer( configurer );
	}

	@Override
	public AcrossTestWebContextBuilder property( String key, Object value ) {
		return (AcrossTestWebContextBuilder) super.property( key, value );
	}

	@Override
	public AcrossTestWebContextBuilder properties( Properties properties ) {
		return (AcrossTestWebContextBuilder) super.properties( properties );
	}

	@Override
	public AcrossTestWebContextBuilder properties( Map<String, Object> properties ) {
		return (AcrossTestWebContextBuilder) super.properties( properties );
	}

	@Override
	public AcrossTestWebContextBuilder properties( PropertySource propertySource ) {
		return (AcrossTestWebContextBuilder) super.properties( propertySource );
	}

	@Override
	public AcrossTestWebContext build() {
		return new AcrossTestWebContext( contextConfigurers() );
	}
}
