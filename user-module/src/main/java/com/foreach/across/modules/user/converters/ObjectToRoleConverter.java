package com.foreach.across.modules.user.converters;

import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.services.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

public class ObjectToRoleConverter implements Converter<Object, Role>
{
	private final ConversionService conversionService;
	private final RoleService roleService;

	public ObjectToRoleConverter( ConversionService conversionService, RoleService roleService ) {
		this.conversionService = conversionService;
		this.roleService = roleService;
	}

	@Override
	public Role convert( Object source ) {
		if ( source instanceof Role ) {
			return (Role) source;
		}

		String roleName = conversionService.convert( source, String.class );

		if ( !StringUtils.isBlank( roleName ) ) {
			return roleService.getRole( roleName );
		}

		return null;
	}
}
