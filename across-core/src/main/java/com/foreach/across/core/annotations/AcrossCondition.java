package com.foreach.across.core.annotations;

import com.foreach.across.core.annotations.conditions.AcrossConditionCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation that checks one or more Spring expression language statements to
 * see if they all return true before deciding a bean should be created.
 * </p>
 * <p>
 * The expression can access certain objects related to the context/module being bootstrapped:
 * <ul>
 * <li><strong>currentModule</strong>: will return the AcrossModule instance currently being bootstrapped</li>
 * </ul>
 * </p>
 * <p><strong>Note:</strong> Only usable on beans & configurations, not on AcrossModule classes.</p>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AcrossConditionCondition.class)
public @interface AcrossCondition
{
	/**
	 * One or more SpEL statements.  Expressions should return {@code true} if the
	 * condition passes or {@code false} if it fails.  If multiple expressions are
	 * configured, they should all return true.
	 */
	String[] value();
}
