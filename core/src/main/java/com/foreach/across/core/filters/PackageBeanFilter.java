package com.foreach.across.core.filters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Applies to all classes that are in one of the given packages.
 */
public class PackageBeanFilter implements BeanFilter
{
	private final String[] allowedPackages;

	public PackageBeanFilter( String... packageNames ) {
		this.allowedPackages = packageNames;
	}

	public boolean apply( Object bean, BeanDefinition definition ) {

		if ( bean != null ) {
			String packageName = bean.getClass().getName();

			for ( String allowed : allowedPackages ) {
				if ( StringUtils.startsWith( packageName, allowed ) ) {
					return true;
				}
			}
		}
		else if ( definition != null ) {
			if ( definition.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) definition.getSource();

				try {
					Method method = ReflectionUtils.findMethod( Class.forName( metadata.getDeclaringClassName() ),
					                                            metadata.getMethodName() );

					for ( String allowed : allowedPackages ) {
						if ( StringUtils.startsWith( method.getReturnType().getName(), allowed ) ) {
							return true;
						}
					}
				}
				catch ( Exception e ) { /* Ignore any exceptions */ }
			}
			else {
				for ( String allowed : allowedPackages ) {
					if ( StringUtils.startsWith( definition.getBeanClassName(), allowed ) ) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
