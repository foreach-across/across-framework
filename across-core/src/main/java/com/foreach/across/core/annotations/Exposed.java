package com.foreach.across.core.annotations;

import java.lang.annotation.*;

/**
 * Beans having this annotation will be exposed to the parent context.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Exposed
{
}
