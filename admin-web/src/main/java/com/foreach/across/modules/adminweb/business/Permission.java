package com.foreach.across.modules.adminweb.business;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

/**
 * A single permission that can be checked against.
 */
@Entity
@Table(name = "permission")
public class Permission
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_permission_id")
	@TableGenerator(name = "seq_permission_id", table = "sequences",
	                pkColumnName = "seq_name", valueColumnName = "seq_number", pkColumnValue = "seq_permission_id",
	                allocationSize = 5)
	private long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column
	private String description;

	public Permission() {
	}

	public Permission( String name ) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Permission that = (Permission) o;

		if ( !StringUtils.equalsIgnoreCase( getName(), that.getName() ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getName() != null ? getName().hashCode() : 0;
	}

	@Override
	public String toString() {
		return "Permission{" +
				"name='" + getName() + '\'' +
				'}';
	}
}
