package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.User;

import java.util.Collection;

public interface UserRepository
{
	Collection<User> getUsers();

	User getUserByUsername( String userName );

	//User getUserByEmail( String email );

	User getUserById( long id );

	void save( User user );

	void delete( User user );
}
