package com.foreach.across.modules.adminweb.config;

import com.foreach.across.modules.adminweb.repositories.PermissionRepository;
import com.foreach.across.modules.adminweb.repositories.PermissionRepositoryImpl;
import com.foreach.across.modules.adminweb.repositories.RoleRepository;
import com.foreach.across.modules.adminweb.repositories.RoleRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminServicesConfig
{
	@Bean
	public PermissionRepository permissionRepository() {
		return new PermissionRepositoryImpl();
	}

	@Bean
	public RoleRepository roleRepository() {
		return new RoleRepositoryImpl();
	}
}
