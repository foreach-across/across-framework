package com.foreach.across.modules.adminweb.business;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestRole
{
	@Test
	public void equals() {
		Role left = new Role( "some role" );
		left.setDescription( "description left" );

		Role right = new Role( "some role" );

		assertEquals( left, right );
		assertNotEquals( left, new Role( "other role" ) );
	}

	@Test
	public void caseIsIngored() {
		Role left = new Role( "some role" );
		left.setDescription( "description left" );

		Role right = new Role( "SOME role" );

		assertEquals( left, right );
	}
}
