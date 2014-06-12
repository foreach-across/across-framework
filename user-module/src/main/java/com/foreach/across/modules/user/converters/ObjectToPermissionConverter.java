package com.foreach.across.modules.user.converters;

import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.services.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

public class ObjectToPermissionConverter implements Converter<Object, Permission>
{
	private final ConversionService conversionService;
	private final PermissionService permissionService;

	public ObjectToPermissionConverter( ConversionService conversionService, PermissionService permissionService ) {
		this.conversionService = conversionService;
		this.permissionService = permissionService;
	}

	@Override
	public Permission convert( Object source ) {
		if ( source instanceof Permission ) {
			return (Permission) source;
		}

		String permissionName = conversionService.convert( source, String.class );

		if ( !StringUtils.isBlank( permissionName ) ) {
			return permissionService.getPermission( permissionName );
		}

		return null;
	}
}
