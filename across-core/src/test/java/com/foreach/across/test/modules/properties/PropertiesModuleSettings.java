package com.foreach.across.test.modules.properties;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;

/**
 * @author Arne Vandamme
 */
public class PropertiesModuleSettings extends AcrossModuleSettings
{
	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
		registry.register( "moduleSourceValue", String.class, "defaultModuleSourceValue" );
		registry.register( "moduleDirectValue", String.class, "defaultModuleDirectValue" );
		registry.register( "contextValue", String.class, "defaultContextValue" );
		registry.register( "defaultOnlyValue", String.class, "default" );
	}
}
