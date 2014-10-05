package com.foreach.across.core.annotations;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

/**
 * Annotation to use on fields that need to be autowired with a
 * {@link com.foreach.across.core.registry.RefreshableRegistry} collection type instead
 * of the collection of beans from the ApplicationContext.
 *
 * @see com.foreach.across.core.registry.RefreshableRegistry
 * @see com.foreach.across.core.registry.IncrementalRefreshableRegistry
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired
public @interface RefreshableCollection
{
	/**
	 * @return True if the internal module beans should be picked up.
	 */
	boolean includeModuleInternals() default false;

	/**
	 * @return True if a {@link com.foreach.across.core.registry.IncrementalRefreshableRegistry} should be created.
	 */
	boolean incremental() default false;
}
