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
package com.foreach.across.core.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ResolvableMessage
{
	private final String[] codes;
	private final List<Object> parameters = new ArrayList<>( 5 );
	private final List<String> parameterNames = new ArrayList<>( 5 );

	private String defaultValue;

	public ResolvableMessage withDefaultValue( String defaultValue ) {
		this.defaultValue = defaultValue;
		return this;
	}

	public ResolvableMessage withParameter( String name, Object value ) {
		parameters.add( value );
		parameterNames.add( name );
		return this;
	}

	public static ResolvableMessage messageCode( String... codes ) {
		return new ResolvableMessage( codes );
	}
}
