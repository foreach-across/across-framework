package com.foreach.across.modules.hibernate.strategy;

import org.hibernate.cfg.ImprovedNamingStrategy;

import java.util.Map;

public class TableAliasNamingStrategy extends ImprovedNamingStrategy
{
	private final Map<String, String> aliasMap;

	public TableAliasNamingStrategy( Map<String, String> aliasMap ) {
		this.aliasMap = aliasMap;
	}

	@Override
	public String classToTableName( String className ) {
		return alias( super.classToTableName( className ) );
	}

	@Override
	public String tableName( String tableName ) {
		return alias( super.tableName( tableName ) );
	}

	private String alias( String tableName ) {
		if ( aliasMap.containsKey( tableName ) ) {
			return aliasMap.get( tableName );
		}

		return tableName;
	}
}
