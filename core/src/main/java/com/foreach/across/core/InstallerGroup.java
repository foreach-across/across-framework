package com.foreach.across.core;

import java.lang.annotation.*;

/**
 * Used to specify the label for a group of installers.  Can be used to filter out installers on
 * Only one default group exists: "schema" (InstallerGroup.SCHEMA constant) used for AcrossLiquibaseInstaller instances.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InstallerGroup
{
	public static String SCHEMA = "schema";

	String value() default "";
}
