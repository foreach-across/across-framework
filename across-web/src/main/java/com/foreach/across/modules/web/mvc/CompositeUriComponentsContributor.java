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

package com.foreach.across.modules.web.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.Assert;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Variation on the Spring CompositeUriComponentsContributor that allows updating the handler methods.
 *
 * @see org.springframework.web.method.support.CompositeUriComponentsContributor
 */
public class CompositeUriComponentsContributor implements UriComponentsContributor
{
	private final List<UriComponentsContributor> contributors = new ArrayList<UriComponentsContributor>();

	private final ConversionService conversionService;

	public CompositeUriComponentsContributor() {
		this( null );
	}

	public CompositeUriComponentsContributor( ConversionService conversionService ) {
		this.conversionService =
				( conversionService != null ) ? conversionService : new DefaultFormattingConversionService();
	}

	public void setContributors( Collection<?> contributors ) {
		Assert.notNull( contributors, "'uriComponentsContributors' must not be null" );

		for ( Object contributor : contributors ) {
			if ( contributor instanceof UriComponentsContributor ) {
				this.contributors.add( (UriComponentsContributor) contributor );
			}
		}
	}

	public boolean hasContributors() {
		return this.contributors.isEmpty();
	}

	public boolean supportsParameter( MethodParameter parameter ) {
		for ( UriComponentsContributor c : this.contributors ) {
			if ( c.supportsParameter( parameter ) ) {
				return true;
			}
		}
		return false;
	}

	public void contributeMethodArgument( MethodParameter parameter,
	                                      Object value,
	                                      UriComponentsBuilder builder,
	                                      Map<String, Object> uriVariables,
	                                      ConversionService conversionService ) {

		for ( UriComponentsContributor c : this.contributors ) {
			if ( c.supportsParameter( parameter ) ) {
				c.contributeMethodArgument( parameter, value, builder, uriVariables, conversionService );
				break;
			}
		}
	}

	/**
	 * An overloaded method that uses the ConversionService created at construction.
	 */
	public void contributeMethodArgument( MethodParameter parameter,
	                                      Object value,
	                                      UriComponentsBuilder builder,
	                                      Map<String, Object> uriVariables ) {

		this.contributeMethodArgument( parameter, value, builder, uriVariables, this.conversionService );
	}
}
