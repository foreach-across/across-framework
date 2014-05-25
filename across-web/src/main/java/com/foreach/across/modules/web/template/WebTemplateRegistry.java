package com.foreach.across.modules.web.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of all known WebTemplate instances with their unique name.
 */
public class WebTemplateRegistry
{
	private String defaultTemplateName;
	private Map<String, WebTemplateProcessor> templateProcessors = new HashMap<>();

	/**
	 * Register a WebTemplate instance under a unique name.
	 *
	 * @param name                 Unique name of the instance.
	 * @param webTemplateProcessor WebTemplate instance.
	 */
	public void register( String name, WebTemplateProcessor webTemplateProcessor ) {
		templateProcessors.put( name, webTemplateProcessor );
	}

	public void unregister( String name ) {
		templateProcessors.remove( name );
	}

	/**
	 * @return Name of the default template that will be applied if no template is specified.
	 */
	public String getDefaultTemplateName() {
		return defaultTemplateName;
	}

	public void setDefaultTemplateName( String defaultTemplateName ) {
		this.defaultTemplateName = defaultTemplateName;
	}

	public WebTemplateProcessor get( String name ) {
		return templateProcessors.get( name );
	}

	public void clear() {
		templateProcessors.clear();
	}

	public Map<String, WebTemplateProcessor> getTemplateProcessors() {
		return new HashMap<>( templateProcessors );
	}
}
