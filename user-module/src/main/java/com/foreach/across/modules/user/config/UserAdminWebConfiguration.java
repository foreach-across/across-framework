package com.foreach.across.modules.user.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.user.controllers.RoleController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AcrossDepends(required = "AdminWebModule")
public class UserAdminWebConfiguration
{
	@Bean
	public RoleController roleController() {
		return new RoleController();
	}
}
