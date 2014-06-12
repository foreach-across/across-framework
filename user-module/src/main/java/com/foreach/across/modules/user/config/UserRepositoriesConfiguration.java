package com.foreach.across.modules.user.config;

import com.foreach.across.modules.user.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRepositoriesConfiguration
{
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
