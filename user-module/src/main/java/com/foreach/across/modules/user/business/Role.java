package com.foreach.across.modules.user.business;

import com.foreach.across.core.database.AcrossSchemaConfiguration;
import com.foreach.across.modules.user.config.UserSchemaConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = UserSchemaConfiguration.TABLE_ROLE)
public class Role implements Comparable<Role>, Serializable
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_um_role_id")
	@TableGenerator(name = "seq_um_role_id", table = AcrossSchemaConfiguration.TABLE_SEQUENCES,
	                pkColumnName = AcrossSchemaConfiguration.SEQUENCE_NAME,
	                valueColumnName = AcrossSchemaConfiguration.SEQUENCE_VALUE, pkColumnValue = "seq_um_role_id",
	                allocationSize = 5)
	private long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column
	private String description;

	@ManyToMany(fetch = FetchType.EAGER)
	@BatchSize(size = 50)
	@JoinTable(
			name = UserSchemaConfiguration.TABLE_ROLE_PERMISSION,
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private Set<Permission> permissions = new TreeSet<Permission>();

	public Role() {
	}

	public Role( String name ) {
		setName( name );
	}

	public Role( String name, String description ) {
		setName( name );
		this.description = description;
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

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void addPermission( String... names ) {
		Permission[] permissions = new Permission[names.length];

		for ( int i = 0; i < names.length; i++ ) {
			permissions[i] = new Permission( names[i] );
		}

		addPermission( permissions );
	}

	public void addPermission( Permission... permissions ) {
		for ( Permission permission : permissions ) {
			getPermissions().add( permission );
		}
	}

	public void setPermissions( Set<Permission> permissions ) {
		this.permissions = permissions;
	}

	public boolean hasPermission( String name ) {
		return hasPermission( new Permission( name ) );
	}

	public boolean hasPermission( Permission permission ) {
		return getPermissions().contains( permission );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Role that = (Role) o;

		if ( !StringUtils.equalsIgnoreCase( getName(), that.getName() ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int compareTo( Role o ) {
		return getName().compareTo( o.getName() );
	}

	@Override
	public int hashCode() {
		return getName() != null ? getName().hashCode() : 0;
	}

	@Override
	public String toString() {
		return "Role{" +
				"name='" + getName() + '\'' +
				'}';
	}
}
