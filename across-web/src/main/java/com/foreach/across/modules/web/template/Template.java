package com.foreach.across.modules.web.template;

import java.lang.annotation.*;

/**
 * Specifies the name of the template to apply when rendering the view.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Template
{
	String value();
}
