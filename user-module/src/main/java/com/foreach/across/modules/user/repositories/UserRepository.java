package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.User;

public interface UserRepository
{
	//User getUserByName( String userName );

	//User getUserByEmail( String email );

	User getUserById( long id );

	void save( User user );

	void delete( User user );
}
