package com.foreach.across.modules.user.converters;

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.dto.UserDto;
import com.foreach.across.modules.user.services.UserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

public class ObjectToUserConverter implements Converter<Object, User>
{
	private final ConversionService conversionService;
	private final UserService userService;

	public ObjectToUserConverter( ConversionService conversionService, UserService userService ) {
		this.conversionService = conversionService;
		this.userService = userService;
	}

	@Override
	public User convert( Object source ) {

		if ( source instanceof User ) {
			return (User) source;
		}

		if ( source instanceof UserDto ) {
			UserDto dto = (UserDto) source;

			if ( !dto.isNewUser() ) {
				return userService.getUserById( dto.getId() );
			}
		}

		long userId = conversionService.convert( source, Long.class );

		if ( userId != 0 ) {
			return userService.getUserById( userId );
		}

		return null;
	}
}
