package com.foreach.across.modules.user.converters;

import com.foreach.across.modules.user.business.User;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

public class ObjectToUserConverter implements Converter<Object, User>
{
	private final ConversionService conversionService;

	public ObjectToUserConverter( ConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	@Override
	public User convert( Object source ) {
		return null;
	}
}
