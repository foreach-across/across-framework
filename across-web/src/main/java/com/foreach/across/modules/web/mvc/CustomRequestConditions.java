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

import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;
import org.springframework.web.servlet.mvc.condition.CompositeRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class CustomRequestConditions extends AbstractRequestCondition<CustomRequestConditions>
{
	private final Set<CustomRequestConditionMatcher> classes;

	public CustomRequestConditions( Collection<CustomRequestConditionMatcher> instances ) {
		classes = new LinkedHashSet<>( instances );
	}

	@Override
	public CustomRequestConditions combine( CustomRequestConditions other ) {
		Set<CustomRequestConditionMatcher> set = new LinkedHashSet<>( this.classes );
		set.addAll( other.classes );
		return new CustomRequestConditions( set );
	}

	@Override
	public CustomRequestConditions getMatchingCondition( HttpServletRequest request ) {
		for ( CustomRequestConditionMatcher clazz : classes ) {
			if ( clazz.matches( request ) ) {
				return new CustomRequestConditions( Collections.singleton( clazz ) );
			}
		}
		return null;
	}

	@Override
	public int compareTo( CustomRequestConditions other, HttpServletRequest request ) {
		return ( other.classes.size() - this.classes.size() );
	}

	@Override
	protected Collection<?> getContent() {
		return classes;
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}
}
