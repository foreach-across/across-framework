package com.foreach.across.core.annotations;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier that can be used to wire the AcrossModule or AcrossModuleInfo instance.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Module
{
	/**
	 * Unique name of the module in the context.
	 */
	String value();
}
