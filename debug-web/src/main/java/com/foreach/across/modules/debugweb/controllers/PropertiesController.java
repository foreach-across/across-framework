package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.table.Table;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Properties;

@DebugWebController(path = "/properties/")
public class PropertiesController {

	@Autowired
	private ConfigurableEnvironment environment;

	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addItem( "/properties/environment", "System environment variables" );
		event.addItem( "/properties/application", "Application properties" );
	}

	@RequestMapping("environment")
	public DebugPageView listEnvironmentProperties( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_PROPERTIES );

		view.addObject( "systemProperties", Table.fromMap( "System properties", System.getProperties() ) );
		view.addObject( "environmentVariables", Table.fromMap( "Environment variables", System.getenv() ) );

		return view;
	}

	@RequestMapping("application")
	public DebugPageView listApplicationProperties( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_APPLICATION_PROPERTIES );

		view.addObject( "applicationProperties", Table.fromMap( "Application properties", getEnvironmentProperties() ) );

		return view;
	}

	/**
	 * Retrieves all properties from the environment, excluding any that reference the system.
	 * The system properties are listed within the other view and are retrieved through some System calls.
	 *
	 * @return The properties registered to the environment (excluding system properties)
	 */
	private Properties getEnvironmentProperties() {
		Properties allProperties = new Properties();
		MutablePropertySources propertySources = environment.getPropertySources();
		for ( PropertySource<?> propertySource : propertySources ) {
			if ( propertySource.getSource() instanceof Map &&
					// We exclude system properties and system environment variables, as they have their own page
					!propertySource.getName().equalsIgnoreCase( "systemproperties" ) &&
					!propertySource.getName().equalsIgnoreCase( "systemenvironment" ) ) {
				allProperties.putAll( ( Map ) propertySource.getSource() );
			} else {
				// not sure we can output this, so let's skip it
			}
		}
		return allProperties;
	}
}
