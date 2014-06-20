package com.foreach.across.modules.user.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Installs database schema for user authorization.", version = 3)
public class UserSchemaInstaller extends AcrossLiquibaseInstaller
{
	public UserSchemaInstaller( SchemaConfiguration schemaConfiguration ) {
		super( schemaConfiguration );
	}
}
