package com.foreach.across.core;

import java.lang.annotation.*;

/**
 * All beans having this annotation will get their properties refreshed once the
 * entire AcrossContext has been bootstrapped.  Alternative way for module components
 * to detect beans exposed by modules that have been loaded at a later bootstrap stage.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Refreshable
{
}
