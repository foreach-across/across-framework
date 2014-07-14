package com.foreach.across.core.annotations;

import java.lang.annotation.*;

/**
 * Methods with this annotation will be executed if the bean they belong to is refreshed.
 * Note that for a refresh to happen the bean must be annotated with @Refreshable.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PostRefresh
{
}
