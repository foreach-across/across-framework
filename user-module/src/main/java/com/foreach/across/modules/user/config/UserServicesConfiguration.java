package com.foreach.across.modules.user.config;

import com.foreach.across.modules.user.converters.ObjectToPermissionConverter;
import com.foreach.across.modules.user.converters.ObjectToRoleConverter;
import com.foreach.across.modules.user.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;

import javax.annotation.PostConstruct;

@Configuration
public class UserServicesConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( UserServicesConfiguration.class );

	@Autowired(required = false)
	private FormattingConversionService conversionService;

	@PostConstruct
	void registerConverters() {
		if ( conversionService != null ) {
			LOG.debug( "FormattingConversionService found - auto-registering user converters " );

			conversionService.addConverter( new ObjectToRoleConverter( conversionService, roleService() ) );
			conversionService.addConverter( new ObjectToPermissionConverter( conversionService, permissionService() ) );
		}
		else {
			LOG.debug( "No FormattingConversionService found - unable to auto-register user converters" );
		}
	}

	@Bean
	public PermissionService permissionService() {
		return new PermissionServiceImpl();
	}

	@Bean
	public RoleService roleService() {
		return new RoleServiceImpl();
	}

	@Bean
	public UserService userService() {
		return new UserServiceImpl();
	}
}
