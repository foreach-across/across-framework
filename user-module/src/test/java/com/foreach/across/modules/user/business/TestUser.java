package com.foreach.across.modules.user.business;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestUser
{
	@Test
	public void hasPermissions() {
		Role roleOne = new Role( "role one" );
		roleOne.getPermissions().add( new Permission( "perm three" ) );

		Role roleTwo = new Role( "role two" );
		roleTwo.getPermissions().addAll( Arrays.asList( new Permission( "perm one" ), new Permission( "perm two" ) ) );

		User user = new User();
		user.getRoles().addAll( Arrays.asList( roleOne, roleTwo ) );

		assertFalse( user.hasRole( "some role" ) );
		assertFalse( user.hasRole( new Role( "some role" ) ) );

		assertTrue( user.hasRole( "role one" ) );
		assertTrue( user.hasRole( new Role( "role two" ) ) );

		assertFalse( user.hasPermission( "perm" ) );
		assertTrue( user.hasPermission( "perm three" ) );
		assertTrue( user.hasPermission( "perm one" ) );
		assertTrue( user.hasPermission( "perm two" ) );
		assertTrue( user.hasPermission( new Permission( "perm three" ) ) );
		assertTrue( user.hasPermission( new Permission( "perm one" ) ) );
		assertTrue( user.hasPermission( new Permission( "perm two" ) ) );
	}
}
