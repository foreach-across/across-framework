package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.business.User;

import java.util.Collection;

public interface UserService
{
	Collection<User> getUsers();

	User getUserById( long id );

	User getUserByUsername( String username );

	void save( User user );

	void delete( User user );
}
