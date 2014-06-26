package com.foreach.across.modules.user.config.modules;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.user.controllers.RoleController;
import com.foreach.across.modules.user.controllers.UserController;
import com.foreach.across.modules.user.handlers.AdminWebEventsHandler;
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

	@Bean
	public UserController userController() {
		return new UserController();
	}

	@Bean
	public AdminWebEventsHandler adminWebEventsHandler() {
		return new AdminWebEventsHandler();
	}
}
