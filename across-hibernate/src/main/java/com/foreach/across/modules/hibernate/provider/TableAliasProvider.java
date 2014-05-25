package com.foreach.across.modules.hibernate.provider;

import com.foreach.across.core.database.SchemaObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TableAliasProvider extends HibernatePackageProviderAdapter
{
	private Map<String, String> tableAlias = new HashMap<>();

	public TableAliasProvider() {
	}

	public TableAliasProvider( Collection<SchemaObject> tables ) {
		for ( SchemaObject table : tables ) {
			if ( !StringUtils.equals( table.getOriginalName(), table.getCurrentName() ) ) {
				tableAlias.put( table.getOriginalName(), table.getCurrentName() );
			}
		}
	}

	public TableAliasProvider( Map<String, String> tableAlias ) {
		this.tableAlias = tableAlias;
	}

	public void addAlias( String original, String name ) {
		tableAlias.put( original, name );
	}

	@Override
	public Map<String, String> getTableAliases() {
		return tableAlias;
	}
}
