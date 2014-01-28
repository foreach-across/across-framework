package com.foreach.across.core.filters;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Beans or definitions with any of the given annotations will be copied.
 */
public class AnnotationBeanFilter implements BeanFilter
{
	private boolean matchIfBeanFactoryApplies = false;
	private Class<? extends Annotation>[] annotations;

	public AnnotationBeanFilter( Class<? extends Annotation>... annotations ) {
		this.annotations = annotations;
	}

	public AnnotationBeanFilter( boolean matchIfBeanFactoryApplies, Class<? extends Annotation>... annotations ) {
		this.matchIfBeanFactoryApplies = matchIfBeanFactoryApplies;
		this.annotations = annotations;
	}

	public Class<? extends Annotation>[] getAnnotations() {
		return annotations;
	}

	public void setAnnotations( Class<? extends Annotation>[] annotations ) {
		this.annotations = annotations;
	}

	public boolean isMatchIfBeanFactoryApplies() {
		return matchIfBeanFactoryApplies;
	}

	/**
	 * @param matchIfBeanFactoryApplies True if the bean should be returned if the bean factory matches.
	 */
	public void setMatchIfBeanFactoryApplies( boolean matchIfBeanFactoryApplies ) {
		this.matchIfBeanFactoryApplies = matchIfBeanFactoryApplies;
	}

	public boolean apply( ConfigurableListableBeanFactory beanFactory, Object bean, BeanDefinition definition ) {
		if ( bean != null ) {
			Class beanClass = ClassUtils.getUserClass( AopUtils.getTargetClass( bean ) );
			for ( Class<? extends Annotation> annotation : annotations ) {
				if ( AnnotationUtils.getAnnotation( beanClass, annotation ) != null ) {
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

				try {
					Class factoryClass = ClassUtils.getUserClass( Class.forName( metadata.getDeclaringClassName() ) );

					Object factory = beanFactory.getSingleton( definition.getFactoryBeanName() );

					if ( factory != null ) {
						factoryClass = ClassUtils.getUserClass( AopUtils.getTargetClass( factory ) );
					}

					if ( isMatchIfBeanFactoryApplies() ) {
						// If the bean factory has the annotation, then it should apply as well
						for ( Class<? extends Annotation> annotation : annotations ) {
							if ( AnnotationUtils.getAnnotation( factoryClass, annotation ) != null ) {
								return true;
							}
						}
					}

					if ( bean == null ) {
						// If the target of the method has the annotation, it applies - in case of a bean
						// this has already been tested
						Method method = ReflectionUtils.findMethod( factoryClass, metadata.getMethodName() );

						for ( Class<? extends Annotation> annotation : annotations ) {
							if ( AnnotationUtils.getAnnotation( method.getReturnType(), annotation ) != null ) {
								return true;
							}
						}
					}
				}
				catch ( Exception e ) { /* Ignore any exceptions */ }
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
