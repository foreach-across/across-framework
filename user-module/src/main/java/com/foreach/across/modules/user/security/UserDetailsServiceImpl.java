package com.foreach.across.modules.user.security;

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
		User user = userService.getUserByUsername( username );

		if ( user == null ) {
			throw new UsernameNotFoundException( "No user found with username: " + username );
		}

		return user;
	}
}
