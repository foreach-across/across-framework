package com.foreach.across.core.context.configurer;

/**
 * Simple implementation for specifying annotated classes an ApplicationContext should register.
 */
public class AnnotatedClassConfigurer extends ApplicationContextConfigurerAdapter
{
	private Class[] annotatedClasses;

	public AnnotatedClassConfigurer( Class... annotatedClasses ) {
		this.annotatedClasses = annotatedClasses;
	}

	/**
	 * Returns a set of annotated classes to register as components in the ApplicationContext.
	 * These can be annotated with @Configuration.
	 *
	 * @return Array of annotated classes.
	 */
	@Override
	public Class[] annotatedClasses() {
		return annotatedClasses;
	}
}
