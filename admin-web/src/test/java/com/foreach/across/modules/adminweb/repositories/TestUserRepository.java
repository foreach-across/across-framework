package com.foreach.across.modules.adminweb.repositories;

import com.foreach.across.modules.adminweb.TestDatabaseConfig;
import com.foreach.across.modules.adminweb.business.User;
import com.foreach.across.modules.adminweb.services.PermissionService;
import com.foreach.across.modules.adminweb.services.PermissionServiceImpl;
import com.foreach.across.modules.adminweb.services.RoleService;
import com.foreach.across.modules.adminweb.services.RoleServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUserRepository.Config.class)
@DirtiesContext
public class TestUserRepository
{
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@Before
	public void createRolesAndPermissions() {
		permissionService.definePermission( "perm one", "" );
		permissionService.definePermission( "perm two", "" );
		permissionService.definePermission( "perm three", "" );

		roleService.defineRole( "role one", "", Arrays.asList( "perm one", "perm two") );
		roleService.defineRole( "role two", "", Arrays.asList( "perm two", "perm three" ) );
	}

	@Test
	public void userNotFound() {
		User user = userRepository.getUserById( -123 );

		assertNull( user );
	}

	@Test
	public void userWithoutPermissions() {
		User user = new User();
		user.setUserName( "fred" );
		user.setEmail( "fred@gmail.com" );
		user.setPassword( "temp1234" );

		userRepository.save( user );

		assertTrue( user.getId() > 0 );

		User existing = userRepository.getUserById( user.getId() );

		assertEquals( user.getId(), existing.getId() );
		assertEquals( user.getUserName(), existing.getUserName() );
		assertEquals( user.getEmail(), existing.getEmail() );
		assertEquals( user.getPassword(), existing.getPassword() );
	}

	@Test
	public void userWithRoles() {
		User user = new User();
		user.setUserName( "paul" );
		user.getRoles().add( roleService.getRole( "role one" ) );
		user.getRoles().add( roleService.getRole( "role two" ) );

		userRepository.save( user );

		User existing = userRepository.getUserById( user.getId() );

		assertEquals( user.getRoles(), existing.getRoles() );
	}

	@Configuration
	@Import(TestDatabaseConfig.class)
	static class Config
	{
		@Bean
		public RoleService roleService() {
			return new RoleServiceImpl();
		}

		@Bean
		public PermissionService permissionService() {
			return new PermissionServiceImpl();
		}

		@Bean
		public RoleRepository roleRepository() {
			return new RoleRepositoryImpl();
		}

		@Bean
		public PermissionRepository permissionRepository() {
			return new PermissionRepositoryImpl();
		}

		@Bean
		public UserRepository userRepository() {
			return new UserRepositoryImpl();
		}
	}
}
