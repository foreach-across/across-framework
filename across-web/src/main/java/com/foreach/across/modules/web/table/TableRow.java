package com.foreach.across.modules.web.table;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class TableRow
{
	private Collection<Object> fields = new LinkedList<Object>();

	public TableRow( Object... fields ) {
		addFields( fields );
	}

	public void addField( Object field ) {
		this.fields.add( field );
	}

	public void addFields( Object... fields ) {
		this.fields.addAll( Arrays.asList( fields ) );
	}

	public Collection<Object> getFields() {
		return fields;
	}
}
