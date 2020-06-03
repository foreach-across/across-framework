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
package com.foreach.across.core.context.support;

import com.foreach.across.core.OrderedInModule;
import com.foreach.across.core.context.AcrossOrderSpecifierComparator;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * A specifier that contains the different Across order related attributes.
 * A global {@link org.springframework.core.annotation.Order} will take precedence when specified,
 * if not the module index will apply and within a same module index the module order will be used.
 *
 * @author Arne Vandamme
 * @see AcrossOrderSpecifierComparator
 * @since 3.0.0
 */
@Builder
public class AcrossOrderSpecifier
{
	@Getter
	private final Integer order;

	@Getter
	private final Integer orderInModule;

	@Getter
	private final Integer moduleIndex;

	/**
	 * Retrieve the actual order value for the instance.
	 * If the instance implements {@link org.springframework.core.Ordered} or {@link org.springframework.core.PriorityOrdered},
	 * the runtime value will be used.  Else the fixed {@link #getOrder()} will be returned.
	 *
	 * @param instance to check
	 * @return order value
	 */
	public Integer getOrder( Object instance ) {
		return getOrder( instance, null );
	}

	/**
	 * Retrieve the actual order value for the instance.
	 * If the instance implements {@link org.springframework.core.Ordered} or {@link org.springframework.core.PriorityOrdered},
	 * the runtime value will be used.  Else the fixed {@link #getOrder()} will be returned.
	 *
	 * @param instance     to check
	 * @param defaultValue to return if no order specified
	 * @return order value
	 */
	public Integer getOrder( Object instance, Integer defaultValue ) {
		if ( instance instanceof Ordered ) {
			return ( (Ordered) instance ).getOrder();
		}
		return order != null ? order : defaultValue;
	}

	/**
	 * Retrieve the actual module order value for the instance.
	 * If the instance implements {@link com.foreach.across.core.OrderedInModule}, the return value of {@link OrderedInModule#getOrderInModule()}
	 * will be used.  Else the fixed {@link #getOrderInModule()} will be returned.
	 *
	 * @param instance to check
	 * @return order in module value
	 */
	public Integer getOrderInModule( Object instance ) {
		return getOrderInModule( instance, null );
	}

	/**
	 * Retrieve the actual module order value for the instance.
	 * If the instance implements {@link com.foreach.across.core.OrderedInModule}, the return value of {@link OrderedInModule#getOrderInModule()}
	 * will be used.  Else the fixed {@link #getOrderInModule()} will be returned.
	 *
	 * @param instance     to check
	 * @param defaultValue to return if no order in module specified
	 * @return order in module value
	 */
	public Integer getOrderInModule( Object instance, Integer defaultValue ) {
		if ( instance instanceof OrderedInModule ) {
			return ( (OrderedInModule) instance ).getOrderInModule();
		}
		return orderInModule != null ? orderInModule : defaultValue;
	}

	/**
	 * Get the module index configured or return the default value.
	 *
	 * @param defaultValue to return if no module index set
	 * @return module index
	 */
	public Integer getModuleIndex( Integer defaultValue ) {
		return moduleIndex != null ? moduleIndex : defaultValue;
	}

	/**
	 * Calculate a single priority value based on the combination of order, module index and module order.
	 * Not the same as fallback sorting using the different parameters, but - when positive ordering values are used - can
	 * obtain the same result.
	 * <p/>
	 * Use this value for situations where you cannot modify the comparator (eg. event handling).
	 *
	 * @return priority value
	 */
	public int toPriority() {
		return toPriority( null );
	}

	/**
	 * Calculate a single priority value based on the combination of order, module index and module order.
	 * Not the same as fallback sorting using the different parameters, but - when positive ordering values are used - can
	 * obtain the same result.
	 * <p/>
	 * Use this value for situations where you cannot modify the comparator (eg. event handling).
	 *
	 * @param instance for which to calculate the priority
	 * @return priority value
	 */
	public int toPriority( Object instance ) {
		Integer order = getOrder( instance );
		Integer orderInModule = getOrderInModule( instance );
		Integer moduleIndex = 1000000000 + ( this.moduleIndex != null ? this.moduleIndex : 0 ) * 2000000;

		if ( order != null ) {
			return order;
		}

		if ( orderInModule != null ) {
			return snapToBoundaries( moduleIndex.longValue() + orderInModule, moduleIndex - 1000000, moduleIndex + 999999 );
		}

		return moduleIndex;
	}

	private int snapToBoundaries( Long value, int minValue, int maxValue ) {
		if ( value <= minValue ) {
			return minValue;
		}
		if ( value >= maxValue ) {
			return maxValue;
		}
		return value.intValue();
	}

	/**
	 * Will attempt to resolve the order and order in module values from the annotations present on the sources.
	 * The first source will take precedence over later sources.
	 *
	 * @param orderSources to check
	 * @return specifier builder with possible values for order and orderInModule
	 */
	public static AcrossOrderSpecifierBuilder forSources( List<Object> orderSources ) {
		AcrossOrderSpecifierBuilder builder = builder();
		for ( Object orderSource : orderSources ) {
			Integer order = AcrossOrderUtils.findOrder( orderSource );
			if ( order != null ) {
				return builder.order( order );
			}
			Integer orderInModule = AcrossOrderUtils.findOrderInModule( orderSource );
			if ( orderInModule != null ) {
				return builder.orderInModule( orderInModule );
			}
		}
		return builder;
	}
}
