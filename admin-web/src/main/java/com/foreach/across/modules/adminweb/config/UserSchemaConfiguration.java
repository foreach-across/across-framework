package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.database.SchemaObject;

import java.util.Arrays;

public class UserSchemaConfiguration extends SchemaConfiguration
{
	public static final String TABLE_SEQUENCES = "um_sequences";
	public static final String TABLE_PERMISSION = "um_permission";
	public static final String TABLE_ROLE = "um_role";
	public static final String TABLE_ROLE_PERMISSION = "um_role_permission";
	public static final String TABLE_USER = "um_user";
	public static final String TABLE_USER_ROLE = "um_user_role";

	public UserSchemaConfiguration() {
		super( Arrays.asList( new SchemaObject( "table.permission", TABLE_PERMISSION ),
		                      new SchemaObject( "table.role", TABLE_ROLE ),
		                      new SchemaObject( "table.role_permission", TABLE_ROLE_PERMISSION ),
		                      new SchemaObject( "table.user", TABLE_USER ),
		                      new SchemaObject( "table.user_role", TABLE_USER_ROLE ) ) );
	}
}
