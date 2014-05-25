package com.foreach.across.modules.web.template;

/**
 * Ensures that no template is applied to the view rendering.
 * This is the same as @ResponseBody but can be used to still return a different view.
 */
public @interface ClearTemplate
{
}
