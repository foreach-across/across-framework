package com.foreach.across.modules.user.dto;

import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.business.User;
import org.springframework.beans.BeanUtils;

import java.util.Set;
import java.util.TreeSet;

public class UserDto
{
	private long id;
	private String username;
	private String email;

	private String password;

	private Set<Role> roles = new TreeSet<>();

	public UserDto() {
	}

	public UserDto( User user ) {
		setFromUser( user );
	}

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername( String username ) {
		this.username = username;
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

	public boolean isNewUser() {
		return getId() == 0;
	}

	public void setFromUser( User user ) {
		BeanUtils.copyProperties( user, this, "password" );
	}
}
