package com.foreach.across.modules.user.business;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestPermission
{
	@Test
	public void equals() {
		Permission left = new Permission( "some permission" );
		left.setDescription( "description left" );

		Permission right = new Permission( "some permission" );

		assertEquals( left, right );
		assertNotEquals( left, new Permission( "other permission" ) );
	}

	@Test
	public void caseIsIgnored() {
		Permission left = new Permission( "some permission" );
		left.setDescription( "description left" );

		Permission right = new Permission( "SOME permission" );

		assertEquals( left, right );
	}
}
