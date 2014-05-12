package com.foreach.across.modules.adminweb.repositories;

import com.foreach.across.modules.adminweb.business.User;

public interface UserRepository
{
	//User getUserByName( String userName );

	//User getUserByEmail( String email );

	User getUserById( long id );

	void save( User user );

	void delete( User user );
}
