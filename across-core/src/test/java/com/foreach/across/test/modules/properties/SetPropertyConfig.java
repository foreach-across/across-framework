package com.foreach.across.test.modules.properties;

import com.foreach.across.core.annotations.AcrossDepends;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@AcrossDepends(expression = "#{currentModule.getName() != 'moduleFour'}")
public class SetPropertyConfig
{
	@Value("${contextValue}")
	public String contextValue;

	@Value("${moduleSourceValue}")
	public String moduleSourceValue;

	@Value("${moduleDirectValue}")
	public String moduleDirectValue;

	@Value("${contextDirectValue}")
	public int contextDirectValue;

	@Value("${unresolvable:50}")
	public long unresolvable;

	@Autowired
	private Environment environment;

	public String getProperty( String propertyName ) {
		return environment.getProperty( propertyName );
	}

	public <T> T getProperty( String propertyName, Class<T> propertyClass ) {
		return environment.getProperty( propertyName, propertyClass );
	}
}
