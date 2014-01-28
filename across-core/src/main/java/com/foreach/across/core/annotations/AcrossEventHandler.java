package com.foreach.across.core.annotations;

import java.lang.annotation.*;

/**
 * All beans having this annotation will automatically be registered in the AcrossEventPublisher.
 * Note: this annotation is not inherited.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AcrossEventHandler
{
}
