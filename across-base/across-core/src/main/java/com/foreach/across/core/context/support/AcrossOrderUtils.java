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
package com.foreach.across.core.context.support;

import com.foreach.across.core.OrderedInModule;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.context.ExposedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for resolving order annotations.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public abstract class AcrossOrderUtils
{
	private static class OrderResolver extends AnnotationAwareOrderComparator
	{
		@Override
		protected Integer findOrder( Object obj ) {
			return super.findOrder( obj );
		}
	}

	private static final OrderResolver ORDER_RESOLVER = new OrderResolver();

	/**
	 * Resolve the order value on the given object.
	 * Will inspect annotations as well as the {@link org.springframework.core.Ordered} interface.
	 *
	 * @param obj to check for order annotations
	 * @return null if no order specified.
	 */
	public static Integer findOrder( Object obj ) {
		return ORDER_RESOLVER.findOrder( obj );
	}

	/**
	 * Resolve the order in module value on the given object.
	 * Will inspect annotations as well as the {@link com.foreach.across.core.OrderedInModule} interface.
	 *
	 * @param obj to check for order annotations
	 * @return null if no order specified.
	 */
	public static Integer findOrderInModule( Object obj ) {
		Integer order = ( obj instanceof OrderedInModule ? ( (OrderedInModule) obj ).getOrderInModule() : null );

		if ( order != null ) {
			return order;
		}

		if ( obj instanceof Class ) {
			OrderInModule ann = AnnotationUtils.findAnnotation( (Class) obj, OrderInModule.class );
			if ( ann != null ) {
				return ann.value();
			}
		}
		else if ( obj instanceof Method ) {
			OrderInModule ann = AnnotationUtils.findAnnotation( (Method) obj, OrderInModule.class );
			if ( ann != null ) {
				return ann.value();
			}
		}
		else if ( obj instanceof AnnotatedElement ) {
			OrderInModule ann = AnnotationUtils.getAnnotation( (AnnotatedElement) obj, OrderInModule.class );
			if ( ann != null ) {
				return ann.value();
			}
		}
		else if ( obj != null ) {
			order = findOrderInModule( obj.getClass() );
			if ( order == null && obj instanceof DecoratingProxy ) {
				order = findOrderInModule( ( (DecoratingProxy) obj ).getDecoratedClass() );
			}
		}

		return order;
	}

	public static AcrossOrderSpecifier createOrderSpecifier( BeanDefinition beanDefinition, Integer moduleIndex ) {
		if ( beanDefinition == null ) {
			return null;
		}
		if ( beanDefinition instanceof ExposedBeanDefinition ) {
			ExposedBeanDefinition exposed = (ExposedBeanDefinition) beanDefinition;

			BeanDefinition originating = exposed.getOriginatingBeanDefinition();
			if ( originating != null ) {
				return createOrderSpecifier( originating, exposed.getModuleIndex() );
			}

			return AcrossOrderSpecifier.forSources( Collections.singletonList( exposed.getTargetType() ) ).moduleIndex( exposed.getModuleIndex() ).build();
		}

		if ( beanDefinition instanceof RootBeanDefinition ) {
			RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;

			List<Object> sources = new ArrayList<Object>( 2 );
			Method factoryMethod = rootBeanDefinition.getResolvedFactoryMethod();
			if ( factoryMethod != null ) {
				sources.add( factoryMethod );
			}
			Class<?> targetType = rootBeanDefinition.getTargetType();
			if ( targetType != null ) {
				sources.add( targetType );
			}

			return AcrossOrderSpecifier.forSources( sources ).moduleIndex( moduleIndex ).build();
		}

		return AcrossOrderSpecifier.builder().moduleIndex( moduleIndex ).build();
	}
}
