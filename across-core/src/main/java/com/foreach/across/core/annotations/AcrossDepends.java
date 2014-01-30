package com.foreach.across.core.annotations;

import com.foreach.across.core.context.AcrossDependsCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Conditional annotation that can be put on a module, @Configuration class, @Bean method or any component.
 * The actual behaviour depends on the owning element.</p>
 * <p>When putting @AcrossDepends on an AcrossModule instance:
 * <ul>
 * <li>the dependencies specified will determine the bootstrap order of the module (after its dependencies)</li>
 * <li>optional dependencies are only used to optimize the bootstrap order, ensuring that any optional
 * modules are in fact bootstrapped before the current one</li>
 * <li>if any of the required dependencies are missing the AcrossContext will not be able to boot</li>
 * </ul>
 * In this case, using required and optional together is important for the best bootstrap order of the AcrossContext.
 * </p>
 * <p>When putting @AcrossDepends on a component, @Bean or @Configuration class:
 * <ul>
 * <li>if any of the <u>required</u> dependencies is <u>missing</u> the component or @Configuration will not be created</li>
 * <li>if any of the <u>optional</u> dependencies is <u>present</u> the component or @Configuration will be loaded</li>
 * </ul>
 * The latter is the implementation of the standard Spring @Conditional behavior.
 * </p>
 * <p>
 * A module is always specified either by the name it exposes (eg. AcrossWebModule) or its full class name
 * (eg. com.foreach.across.modules.web.AcrossWebModule).
 * </p>
 *
 * @see org.springframework.context.annotation.Conditional
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AcrossDependsCondition.class)
public @interface AcrossDepends
{
	/**
	 * Set of module identifiers that are required.
	 */
	String[] required() default { };

	/**
	 * Set of module identifiers that are optional.
	 */
	String[] optional() default { };
}
