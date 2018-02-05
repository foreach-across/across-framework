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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor()
public class ResolvableMessage
{
	@Getter
	private final String[] codes;
	private String defaultValue;
	@Getter
	private Map<String, Object> parameters;// = new LinkedHashMap<>();

	public ResolvableMessage withDefaultValue( String defaultValue ) {
		ResolvableMessage clone = new ResolvableMessage( this.codes );
		clone.parameters = this.parameters != null ? new LinkedHashMap<>( this.parameters ) : null;
		clone.defaultValue = defaultValue;
		return clone;
	}

	public ResolvableMessage withParameter( String name, Object value ) {
		ResolvableMessage clone = new ResolvableMessage( this.codes );
		clone.defaultValue = this.defaultValue;
		clone.parameters = this.parameters != null ? new LinkedHashMap<>( this.parameters ) : new LinkedHashMap<>();
		clone.parameters.put( name, value );
		return clone;
	}

	public static ResolvableMessage messageCode( String... codes ) {
		return new ResolvableMessage( codes );
	}
}
