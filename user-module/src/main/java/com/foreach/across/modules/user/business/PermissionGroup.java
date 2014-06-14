package com.foreach.across.modules.user.business;

import com.foreach.across.core.database.AcrossSchemaConfiguration;
import com.foreach.across.modules.user.config.UserSchemaConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = UserSchemaConfiguration.TABLE_PERMISSION_GROUP)
public class PermissionGroup
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_um_permission_group_id")
	@TableGenerator(name = "seq_um_permission_group_id", table = AcrossSchemaConfiguration.TABLE_SEQUENCES,
	                pkColumnName = AcrossSchemaConfiguration.SEQUENCE_NAME,
	                valueColumnName = AcrossSchemaConfiguration.SEQUENCE_VALUE,
	                pkColumnValue = "seq_um_permission_group_id",
	                allocationSize = 5)
	private long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column
	private String title;

	@Column
	private String description;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "group")
	@BatchSize(size = 50)
	private Set<Permission> permissions = new TreeSet<>();

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

	public String getTitle() {
		return title != null ? title : name;
	}

	public void setTitle( String title ) {
		this.title = title;
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

	public void setPermissions( Set<Permission> permissions ) {
		this.permissions = permissions;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PermissionGroup that = (PermissionGroup) o;

		if ( !StringUtils.equalsIgnoreCase( getName(), that.getName() ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "PermissionGroup{" +
				"name='" + name + '\'' +
				'}';
	}
}
