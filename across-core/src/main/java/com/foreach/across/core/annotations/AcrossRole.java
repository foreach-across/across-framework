package com.foreach.across.core.annotations;

import com.foreach.across.core.context.AcrossDependsCondition;
import com.foreach.across.core.context.AcrossModuleRole;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AcrossRole annotations impact the bootstrapping order of AcrossModules.
 * Modules annotated with an INFRASTRUCTURE role will always boot as early as possible,
 * without explicitly declaring other module dependencies.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AcrossRole
{
	AcrossModuleRole value();
}
