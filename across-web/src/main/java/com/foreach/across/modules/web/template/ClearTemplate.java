package com.foreach.across.modules.web.template;

import java.lang.annotation.*;

/**
 * Ensures that no template is applied to the view rendering.
 * This is the same as @ResponseBody but can be used to still return a different view.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ClearTemplate
{
}
