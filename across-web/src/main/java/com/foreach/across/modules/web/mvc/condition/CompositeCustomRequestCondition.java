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
package com.foreach.across.modules.web.mvc.condition;

import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Collection of {@link CustomRequestCondition} with different semantics than
 * the {@link org.springframework.web.servlet.mvc.condition.CompositeRequestCondition}.  When combining,
 * this implementation will merge the collections.  Condition types existing on both ends will be combined, else
 * it will be added to the resulting collection.
 * <p/>
 * Only a single condition of the same type is allowed in the collection.  An exception will be thrown if more
 * than one condition of the same type is added.
 * <p/>
 * This condition only matches if all of its members match.
 *
 * @author Marc Vanbrabant
 * @author Arne Vandamme
 * @since 2.0.0
 */
public final class CompositeCustomRequestCondition extends AbstractRequestCondition<CompositeCustomRequestCondition>
{
	private final Map<String, CustomRequestCondition> conditionMap = new TreeMap<>();

	/**
	 * Create a new empty condition.  Can be used with {@link #combine(CompositeCustomRequestCondition)}
	 * and will match any request.
	 */
	public CompositeCustomRequestCondition() {
		this( Collections.emptyList() );
	}

	public CompositeCustomRequestCondition( Collection<CustomRequestCondition> conditions ) {
		conditions
				.forEach( condition -> conditionMap.compute(
						ClassUtils.getUserClass( condition.getClass() ).getName(), ( key, value ) -> {
							if ( value != null ) {
								throw new IllegalArgumentException(
										"Only a single CustomRequestCondition of the same type is allowed" );
							}
							return condition;
						}
				) );
	}

	/**
	 * If one instance is empty, return the other.
	 * If both instances have conditions, combine the conditions of the same type, add all others.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CompositeCustomRequestCondition combine( CompositeCustomRequestCondition other ) {
		if ( isEmpty() && other.isEmpty() ) {
			return this;
		}
		else if ( other.isEmpty() ) {
			return this;
		}
		else if ( isEmpty() ) {
			return other;
		}
		else {
			CompositeCustomRequestCondition combined = new CompositeCustomRequestCondition( conditionMap.values() );
			other.conditionMap.forEach(
					( className, condition ) ->
							combined.conditionMap.compute( className, ( key, existing ) ->
									existing != null ? existing.combine( condition ) : condition
							)
			);

			return combined;
		}
	}

	@Override
	public CompositeCustomRequestCondition getMatchingCondition( HttpServletRequest request ) {
		if ( isEmpty() ) {
			return this;
		}

		List<CustomRequestCondition> matchingConditions = new ArrayList<>( conditionMap.size() );
		for ( CustomRequestCondition condition : conditionMap.values() ) {
			CustomRequestCondition matching = condition.getMatchingCondition( request );

			if ( matching == null ) {
				return null;
			}

			matchingConditions.add( matching );
		}

		return new CompositeCustomRequestCondition( matchingConditions );
	}

	/**
	 * If one instance is empty, the other "wins". If both instances have
	 * conditions, compare them in the order  in which they were provided.
	 * A condition with more members always has precedence over one with fewer,
	 * as it is considered to be a more specific match.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo( CompositeCustomRequestCondition other, HttpServletRequest request ) {
		if ( isEmpty() && other.isEmpty() ) {
			return 0;
		}
		else if ( isEmpty() ) {
			return 1;
		}
		else if ( other.isEmpty() ) {
			return -1;
		}
		else {
			int result = -Integer.compare( conditionMap.size(), other.conditionMap.size() );

			if ( result != 0 ) {
				return result;
			}

			Set<String> classNames = new HashSet<>( conditionMap.keySet() );
			classNames.addAll( other.conditionMap.keySet() );

			for ( String className : classNames ) {
				CustomRequestCondition myCondition = conditionMap.get( className );
				CustomRequestCondition otherCondition = other.conditionMap.get( className );

				if ( myCondition == null ) {
					return 1;
				}
				else if ( otherCondition == null ) {
					return -1;
				}
				else {
					result = myCondition.compareTo( otherCondition, request );
					if ( result != 0 ) {
						return result;
					}
				}
			}

			return 0;
		}
	}

	@Override
	public boolean isEmpty() {
		return conditionMap.isEmpty();
	}

	@Override
	protected Collection<CustomRequestCondition> getContent() {
		return new ArrayList<>( conditionMap.values() );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}
}
