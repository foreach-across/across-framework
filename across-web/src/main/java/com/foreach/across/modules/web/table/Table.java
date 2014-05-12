package com.foreach.across.modules.web.table;

import java.util.*;

public class Table
{
	private String title;
	private TableHeader header;
	private Collection<TableRow> rows = new LinkedList<>();

	public Table() {
	}

	public Table( String title ) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public TableHeader getHeader() {
		return header;
	}

	public void setHeader( TableHeader header ) {
		this.header = header;
	}

	public Collection<TableRow> getRows() {
		return rows;
	}

	public void setRows( Collection<TableRow> rows ) {
		this.rows = rows;
	}

	public void addRow( TableRow row ) {
		rows.add( row );
	}

	public void addRow( Object... fields ) {
		rows.add( new TableRow( fields ) );
	}

	/**
	 * Converts a map into a table where the first column is the key
	 * and the second column the value.
	 *
	 * @param title Title for the table.
	 * @param data  Map to be converted.
	 * @return Table instance.
	 */
	public static Table fromMap( String title, Map data ) {
		Set<Object> sortedKeys = new LinkedHashSet<Object>( data.keySet() );

		Table table = new Table( title );

		for ( Object key : sortedKeys ) {
			table.addRow( key, data.get( key ) );
		}

		return table;
	}

	public boolean isEmpty() {
		return rows.isEmpty();
	}

	public int size() {
		return rows.size();
	}
}
