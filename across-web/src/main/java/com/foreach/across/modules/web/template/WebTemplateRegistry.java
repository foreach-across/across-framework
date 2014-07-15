package com.foreach.across.modules.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of all known WebTemplate instances with their unique name.
 */
public class WebTemplateRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger( WebTemplateRegistry.class );

	private String defaultTemplateName;
	private Map<String, WebTemplateProcessor> templateProcessors = new HashMap<>();

	/**
	 * Register a WebTemplateProcessor instance.
	 *
	 * @param webTemplateProcessor Named WebTemplateProcessor instance.
	 */
	public void register( NamedWebTemplateProcessor webTemplateProcessor ) {
		register( webTemplateProcessor.getName(), webTemplateProcessor );
	}

	public void unregister( NamedWebTemplateProcessor webTemplateProcessor ) {
		unregister( webTemplateProcessor.getName() );
	}

	/**
	 * Register a WebTemplateProcessor instance under a unique name.
	 *
	 * @param name                 Unique name of the instance.
	 * @param webTemplateProcessor WebTemplateProcessor instance.
	 */
	public void register( String name, WebTemplateProcessor webTemplateProcessor ) {
		LOG.debug( "Registering WebTemplateProcessor with name {}: {}", name, webTemplateProcessor );
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

	public boolean hasDefaultTemplate() {
		return defaultTemplateName != null;
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
		return Collections.unmodifiableMap( templateProcessors );
	}
}
