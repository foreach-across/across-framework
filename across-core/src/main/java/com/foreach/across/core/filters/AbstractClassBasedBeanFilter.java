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

package com.foreach.across.core.filters;

import com.foreach.across.core.util.ClassLoadingUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public abstract class AbstractClassBasedBeanFilter<T> implements BeanFilter
{
	private T[] allowedItems;

	@SafeVarargs
	protected AbstractClassBasedBeanFilter( T... allowedItems ) {
		this.allowedItems = allowedItems;
	}

	protected T[] getAllowedItems() {
		return allowedItems;
	}

	@SuppressWarnings( "unchecked" )
	protected void setAllowedItems( T... allowedItems ) {
		this.allowedItems = allowedItems;
	}

	@SuppressWarnings({ "findbugs:DE_MIGHT_IGNORE", "squid:S00108" })
	public boolean apply( ConfigurableListableBeanFactory beanFactory,
	                      String beanName,
	                      Object bean,
	                      BeanDefinition definition ) {
		if ( bean != null ) {
			Class targetClass = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( bean ) );

			for ( T allowed : allowedItems ) {
				if ( matches( targetClass, allowed ) ) {
					return true;
				}
			}

			if ( bean instanceof FactoryBean ) {
				// in case of a factory bean, check it as well
				targetClass = ( (FactoryBean) bean ).getObjectType();

				for ( T allowed : allowedItems ) {
					if ( matches( targetClass, allowed ) ) {
						return true;
					}
				}
			}
		}
		else if ( definition != null ) {
			if ( definition.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) definition.getSource();

				try {
					Method method = ReflectionUtils.findMethod(
							ClassUtils.getUserClass( ClassLoadingUtils.loadClass( metadata.getDeclaringClassName() ) ),
							metadata.getMethodName() );

					for ( T allowed : allowedItems ) {
						if ( matches( method.getReturnType(), allowed ) ) {
							return true;
						}
					}
				}
				catch ( Exception ignore ) { /* Ignore any exceptions */ }
			}
			else {
				for ( T allowed : allowedItems ) {
					if ( matches( definition.getBeanClassName(), allowed ) ) {
						return true;
					}
				}
			}
		}

		return false;
	}

	protected abstract boolean matches( Class beanClass, T expected );

	protected abstract boolean matches( String beanClassName, T expected );
}
