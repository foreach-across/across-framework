package com.foreach.across.core.filters;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public abstract class AbstractClassBasedBeanFilter<T> implements BeanFilter
{
	private T[] allowedItems;

	protected AbstractClassBasedBeanFilter( T... allowedItems ) {
		this.allowedItems = allowedItems;
	}

	protected T[] getAllowedItems() {
		return allowedItems;
	}

	protected void setAllowedItems( T... allowedItems ) {
		this.allowedItems = allowedItems;
	}

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
		}
		else if ( definition != null ) {
			if ( definition.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) definition.getSource();

				try {
					Method method = ReflectionUtils.findMethod(
							ClassUtils.getUserClass( Class.forName( metadata.getDeclaringClassName() ) ),
							metadata.getMethodName() );

					for ( T allowed : allowedItems ) {
						if ( matches( method.getReturnType(), allowed ) ) {
							return true;
						}
					}
				}
				catch ( Exception e ) { /* Ignore any exceptions */ }
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
