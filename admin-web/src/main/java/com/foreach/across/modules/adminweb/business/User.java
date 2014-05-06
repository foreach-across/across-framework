package com.foreach.across.modules.adminweb.business;

import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.security.Principal;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "user")
public class User implements Principal
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_user_id")
	@TableGenerator(name = "seq_user_id", table = "sequences",
	                pkColumnName = "seq_name", valueColumnName = "seq_number", pkColumnValue = "seq_user_id",
	                allocationSize = 5)
	private long id;

	@Column(nullable = false, name = "username")
	private String userName;

	@Column
	private String email;

	@Column
	private String password;

	@ManyToMany(fetch = FetchType.EAGER)
	@BatchSize(size = 50)
	@JoinTable(
			name = "user_role",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new TreeSet<>();

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName( String userName ) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles( Set<Role> roles ) {
		this.roles = roles;
	}

	public String getName() {
		return getUserName();
	}

	public boolean hasRole( String name ) {
		return hasRole( new Role( name ) );
	}

	public boolean hasRole( Role role ) {
		return getRoles().contains( role );
	}

	public boolean hasPermission( String name ) {
		return hasPermission( new Permission( name ) );
	}

	public boolean hasPermission( Permission permission ) {
		for ( Role role : getRoles() ) {
			if ( role.hasPermission( permission ) ) {
				return true;
			}
		}

		return false;
	}
}
