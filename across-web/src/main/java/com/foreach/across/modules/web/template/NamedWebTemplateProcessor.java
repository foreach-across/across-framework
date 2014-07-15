package com.foreach.across.modules.web.template;

/**
 * Extends the standard WebTemplateProcessor with the name.
 *
 * @see com.foreach.across.modules.web.template.NamedWebTemplateProcessorRegistrar
 */
public interface NamedWebTemplateProcessor extends WebTemplateProcessor
{
	/**
	 * @return The name under which the processor should be registered.
	 */
	String getName();
}
