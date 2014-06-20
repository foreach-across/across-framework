package com.foreach.across.modules.web.mvc;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ValidatorDelegate implements Validator
{
	private Validator implementation;

	public Validator getImplementation() {
		return implementation;
	}

	public void setImplementation( Validator implementation ) {
		this.implementation = implementation;
	}

	public boolean hasImplementation() {
		return implementation != null;
	}

	@Override
	public boolean supports( Class<?> clazz ) {
		return implementation.supports( clazz );
	}

	@Override
	public void validate( Object target, Errors errors ) {
		implementation.validate( target, errors );
	}
}
