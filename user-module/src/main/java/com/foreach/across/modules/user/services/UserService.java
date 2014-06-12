package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.dto.UserDto;

import java.util.Collection;

public interface UserService
{
	Collection<User> getUsers();

	User getUserById( long id );

	User getUserByUsername( String username );

	UserDto createUserDto( User user );

	void save( UserDto user );
}
