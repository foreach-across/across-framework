package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.TestDatabaseConfig;
import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.PermissionGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestPermissionRepository.Config.class)
@DirtiesContext
public class TestPermissionRepository
{
	@Autowired
	private PermissionRepository permissionRepository;

	@Test
	public void notExistingPermission() {
		Permission existing = permissionRepository.getPermission( "djsklfjdskds" );

		assertNull( existing );
	}

	@Test
	public void notExistingPermissionGroup() {
		PermissionGroup existing = permissionRepository.getPermissionGroup( "djsklfjdskds" );

		assertNull( existing );
	}

	@Test
	public void saveAndGetPermissionGroup() {
		PermissionGroup group = new PermissionGroup();
		group.setName( "test-group" );
		group.setTitle( "Test permission group" );
		group.setDescription( "Contains some permissions" );

		permissionRepository.save( group );

		assertTrue( group.getId() > 0 );

		PermissionGroup existing = permissionRepository.getPermissionGroup( "test-group" );
		assertEquals( group, existing );
		assertEquals( group.getId(), existing.getId() );
		assertEquals( "Test permission group", existing.getTitle() );
		assertEquals( "Contains some permissions", existing.getDescription() );

		permissionRepository.delete( existing );

		existing = permissionRepository.getPermissionGroup( "test-group" );
		assertNull( existing );
	}

	@Test
	public void saveAndGetPermission() {
		PermissionGroup userGroup = new PermissionGroup();
		userGroup.setName( "test-users" );
		userGroup.setTitle( "Test users" );

		permissionRepository.save( userGroup );

		Permission manageUsers = new Permission( "manage users" );
		manageUsers.setGroup( userGroup );
		permissionRepository.save( manageUsers );

		assertTrue( manageUsers.getId() > 0 );

		Permission existing = permissionRepository.getPermission( "manage users" );
		assertEquals( manageUsers, existing );
		assertEquals( manageUsers.getId(), existing.getId() );

		permissionRepository.delete( existing );

		existing = permissionRepository.getPermission( "manage users" );
		assertNull( existing );
	}

	@Configuration
	@Import(TestDatabaseConfig.class)
	static class Config
	{
		@Bean
		public PermissionRepository permissionRepository() {
			return new PermissionRepositoryImpl();
		}
	}
}
