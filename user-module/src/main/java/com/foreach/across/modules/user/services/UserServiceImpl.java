package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService
{
	@Autowired
	private UserRepository userRepository;

	@Override
	public Collection<User> getUsers() {
		return userRepository.getUsers();
	}

	@Override
	public User getUserById( long id ) {
		return userRepository.getUserById( id );
	}

	@Override
	public User getUserByUsername( String username ) {
		return userRepository.getUserByUsername( username );
	}

	@Override
	public void save( User user ) {
		userRepository.save( user );
	}

	@Override
	public void delete( User user ) {
		userRepository.delete( user );
	}
}
