package com.foreach.across.test.filters;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Beans or definitions with any of the given annotations will be copied.
 */
public class AnnotationBeanFilter implements BeanFilter
{
	private final Class<? extends Annotation>[] annotations;

	public AnnotationBeanFilter( Class<? extends Annotation>... annotations ) {
		this.annotations = annotations;
	}

	public boolean apply( Object bean, BeanDefinition definition ) {
		if ( bean != null ) {
			for ( Class<? extends Annotation> annotation : annotations ) {
				if ( AnnotationUtils.getAnnotation( bean.getClass(), annotation ) != null ) {
					return true;
				}
			}
		}

		// Even though a bean might not match, perhaps the definition that created it does
		if ( definition != null ) {
			if ( definition.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) definition.getSource();

				// If method itself has the annotation it applies
				for ( Class<? extends Annotation> annotation : annotations ) {
					if ( metadata.isAnnotated( annotation.getName() ) ) {
						return true;
					}
				}

				if ( bean == null ) {
					// If the target of the method has the annotation, it applies - in case of a bean
					// this has already been tested
					try {
						Method method = ReflectionUtils.findMethod( Class.forName( metadata.getDeclaringClassName() ),
						                                            metadata.getMethodName() );

						for ( Class<? extends Annotation> annotation : annotations ) {
							if ( AnnotationUtils.getAnnotation( method.getReturnType(), annotation ) != null ) {
								return true;
							}
						}
					}
					catch ( Exception e ) { /* Ignore any exceptions */ }
				}
			}
			else if ( bean == null && definition.getBeanClassName() != null ) {
				try {
					Class beanClass = Class.forName( definition.getBeanClassName() );

					for ( Class<? extends Annotation> annotation : annotations ) {
						if ( AnnotationUtils.getAnnotation( beanClass, annotation ) != null ) {
							return true;
						}
					}
				}
				catch ( Exception e ) { /* Ignore any exceptions */ }
			}
		}

		return false;
	}
}
