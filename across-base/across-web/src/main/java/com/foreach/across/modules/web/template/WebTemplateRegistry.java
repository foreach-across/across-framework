/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
