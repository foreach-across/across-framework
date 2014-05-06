package com.foreach.across.modules.adminweb.config;

import com.foreach.across.modules.adminweb.repositories.*;
import com.foreach.across.modules.adminweb.services.PermissionService;
import com.foreach.across.modules.adminweb.services.PermissionServiceImpl;
import com.foreach.across.modules.adminweb.services.RoleService;
import com.foreach.across.modules.adminweb.services.RoleServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminServicesConfig
{
	@Bean
	public PermissionService permissionService() {
		return new PermissionServiceImpl();
	}

	@Bean
	public RoleService roleService() {
		return new RoleServiceImpl();
	}

	//@Bean
	//public UserService userService() {return new UserServiceImpl();}

	@Bean
	public PermissionRepository permissionRepository() {
		return new PermissionRepositoryImpl();
	}

	@Bean
	public RoleRepository roleRepository() {
		return new RoleRepositoryImpl();
	}

	@Bean
	public UserRepository userRepository() {
		return new UserRepositoryImpl();
	}
}
