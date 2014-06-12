package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.TestDatabaseConfig;
import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.PermissionGroup;
import com.foreach.across.modules.user.business.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRoleRepository.Config.class)
@DirtiesContext
public class TestRoleRepository
{
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Test
	public void notExistingRole() {
		Role existing = roleRepository.getRole( "badrole" );

		assertNull( existing );
	}

	@Test
	public void saveAndGetRole() {
		PermissionGroup groupOne = new PermissionGroup();
		groupOne.setName( "group-one" );
		permissionRepository.save( groupOne );

		Permission permissionOne = new Permission( "admin permission" );
		permissionOne.setGroup( groupOne );
		permissionRepository.save( permissionOne );

		Permission permissionTwo = new Permission( "other permission" );
		permissionTwo.setGroup( groupOne );
		permissionRepository.save( permissionTwo );

		Role admin = new Role( "admin", "Administrator role" );
		admin.getPermissions().add( permissionOne );
		admin.getPermissions().add( permissionTwo );

		roleRepository.save( admin );

		assertTrue( admin.getId() > 0 );

		Role existing = roleRepository.getRole( "admin" );
		assertEquals( admin, existing );
		assertEquals( admin.getDescription(), existing.getDescription() );
		assertEquals( admin.getId(), existing.getId() );
		assertEquals( admin.getPermissions(), existing.getPermissions() );

		Collection<Role> roles = roleRepository.getRoles();
		assertEquals( 1, roles.size() );
		assertTrue( roles.contains( admin ) );

		roleRepository.delete( existing );

		existing = roleRepository.getRole( "admin" );
		assertNull( existing );
	}

	@Test
	public void updateRole() {
		PermissionGroup groupTwo = new PermissionGroup();
		groupTwo.setName( "group-two" );
		permissionRepository.save( groupTwo );

		Permission permissionOne = new Permission( "some permission" );
		permissionOne.setGroup( groupTwo );
		permissionRepository.save( permissionOne );

		Role role = new Role( "user" );
		roleRepository.save( role );

		assertTrue( role.getId() > 0 );

		Role existing = roleRepository.getRole( "user" );
		assertEquals( role, existing );
		assertTrue( role.getPermissions().isEmpty() );

		Set<Permission> permissions = new HashSet<>();
		permissions.add( permissionOne );

		existing.setPermissions( permissions );
		roleRepository.save( existing );

		existing = roleRepository.getRole( "user" );

		assertEquals( role, existing );
		assertEquals( 1, existing.getPermissions().size() );
		assertTrue( existing.getPermissions().contains( permissionOne ) );
	}

	@Configuration
	@Import(TestDatabaseConfig.class)
	static class Config
	{
		@Bean
		public RoleRepository roleRepository() {
			return new RoleRepositoryImpl();
		}

		@Bean
		public PermissionRepository permissionRepository() {
			return new PermissionRepositoryImpl();
		}
	}
}
