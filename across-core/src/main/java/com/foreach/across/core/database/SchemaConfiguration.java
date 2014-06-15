package com.foreach.across.core.database;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class encapsulating general database properties to be used when creating/updating the schema.
 * With specific functionality for table names and renaming tables.
 */
public class SchemaConfiguration
{
	private Collection<SchemaObject> tables;
	private Map<String, String> properties = new HashMap<>();

	public SchemaConfiguration( Collection<SchemaObject> tables ) {
		this.tables = Collections.unmodifiableCollection( tables );
	}

	public Collection<SchemaObject> getTables() {
		return tables;
	}

	public void renameTable( String original, String name ) {
		boolean found = false;
		for ( SchemaObject databaseObject : tables ) {
			if ( StringUtils.equals( original, databaseObject.getOriginalName() ) ) {
				databaseObject.setCurrentName( name );
				found = true;
			}
		}

		if ( !found ) {
			throw new RuntimeException( "Could not find any defined table with name " + original );
		}
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperty( String name, String value ) {
		properties.put( name, value );
	}

	public String getProperty( String name ) {
		return properties.get( name );
	}
}
