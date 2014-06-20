package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.debugweb.util.ContextDebugInfo;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.table.Table;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Properties;

@DebugWebController
@RequestMapping("/properties")
public class PropertiesController
{
	@Autowired
	private AcrossContextInfo acrossContextInfo;

	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addItem( "/properties/environment", "System environment variables" );
		event.addItem( "/properties/application", "Application properties" );
	}

	@ModelAttribute
	public void registerJQuery( WebResourceRegistry resourceRegistry ) {
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "jquery",
		                             "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js",
		                             WebResource.EXTERNAL );
	}

	@RequestMapping("environment")
	public String listEnvironmentProperties( Model model ) {
		model.addAttribute( "systemProperties", Table.fromMap( "System properties", System.getProperties() ) );
		model.addAttribute( "environmentVariables", Table.fromMap( "Environment variables", System.getenv() ) );

		return DebugWeb.VIEW_PROPERTIES;
	}

	@RequestMapping("application")
	public String listApplicationProperties( Model model, @RequestParam(value = "contextName",
	                                                                    required = false) String contextName ) {
		Collection<ContextDebugInfo> debugInfo = ContextDebugInfo.create( acrossContextInfo );

		model.addAttribute( "contextList", debugInfo );

		ContextDebugInfo current = select( debugInfo, contextName );
		model.addAttribute( "currentContext", current );

		model.addAttribute( "applicationProperties", Table.fromMap( "Application properties",
		                                                            getPropertiesForEnvironment(
				                                                            current.getEnvironment() ) ) );

		return DebugWeb.VIEW_APPLICATION_PROPERTIES;
	}

	private ContextDebugInfo select( Collection<ContextDebugInfo> list, String contextName ) {
		for ( ContextDebugInfo debugInfo : list ) {
			if ( StringUtils.equals( debugInfo.getName(), contextName ) ) {
				return debugInfo;
			}
		}

		return list.iterator().next();
	}

	/**
	 * Retrieves all properties from the environment, excluding any that reference the system.
	 * The system properties are listed within the other view and are retrieved through some System calls.
	 *
	 * @return The properties registered to the environment (excluding system properties)
	 */
	private Properties getPropertiesForEnvironment( Environment environment ) {
		Properties allProperties = new Properties();

		// Only configurable environments and enumerable property sources are taken into account
		if ( environment instanceof ConfigurableEnvironment ) {
			MutablePropertySources propertySources = ( (ConfigurableEnvironment) environment ).getPropertySources();
			for ( PropertySource<?> propertySource : propertySources ) {
				if ( propertySource instanceof EnumerablePropertySource &&
						// We exclude system properties and system environment variables, as they have their own page
						!StringUtils.equalsIgnoreCase( "systemproperties", propertySource.getName() ) &&
						!StringUtils.equalsIgnoreCase( "systemenvironment", propertySource.getName() ) ) {
					EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;

					for ( String propertyName : enumerablePropertySource.getPropertyNames() ) {
						allProperties.put( propertyName, enumerablePropertySource.getProperty( propertyName ) );
					}
				}
				else {
					// not sure we can output this, so let's skip it
					// maybe provide feedback if we cannot list certain property sources
				}
			}
		}
		return allProperties;
	}
}
