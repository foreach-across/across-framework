package com.foreach.across.core.annotations;

import org.springframework.core.Ordered;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that defines ordering inside the AcrossModule. The value is optional, and represents order value
 * as defined in the {@link org.springframework.core.Ordered} interface. Lower values have higher priority.
 * The default value is {@code Ordered.LOWEST_PRECEDENCE}, indicating
 * lowest priority (losing to any other specified order value).
 *
 * The regular @Order annotation and Ordered interface define ordering of beans across the entire context,
 * whereas this annotation is used to second level ordering.  If you need to define the order in module at runtime,
 * use the {@link com.foreach.across.core.OrderedInModule} interface instead.
 *
 * @author Arne Vandamme
 * @since 1.0.3
 * @see org.springframework.core.Ordered
 * @see com.foreach.across.core.OrderedInModule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD})
public @interface OrderInModule
{
	/**
	 * The order value. Default is {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE}.
	 * @see com.foreach.across.core.OrderedInModule#getOrderInModule()
	 */
	int value() default Ordered.LOWEST_PRECEDENCE;

}