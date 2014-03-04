package com.foreach.across.test.modules.hibernate.hibernate2;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User
{
	@Id
	private int id;

	@Column
	private String name;

	public User() {
	}

	public User( int id, String name ) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		User user = (User) o;

		if ( getId() != user.getId() ) {
			return false;
		}
		if ( getName() != null ? !getName().equals( user.getName() ) : user.getName() != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + ( getName() != null ? getName().hashCode() : 0 );
		return result;
	}
}
