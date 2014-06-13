package com.foreach.across.core.database;

public class SchemaObject
{
	private final String key;
	private final String originalName;
	private String currentName;

	public SchemaObject( String key, String originalName ) {
		this.key = key;
		this.originalName = originalName;
		this.currentName = originalName;
	}

	public String getKey() {
		return key;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getCurrentName() {
		return currentName;
	}

	public void setCurrentName( String currentName ) {
		this.currentName = currentName;
	}
}
