package com.foreach.across.core.annotations;

import java.lang.annotation.*;

/**
 * Used to specify the parameter-less method in an @Installer annotated class.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InstallerMethod
{
}
