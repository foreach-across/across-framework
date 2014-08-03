package com.foreach.across.test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

import java.util.Set;

public abstract class AbstractInlineModule extends AcrossModule
{
	private final String name;

	protected AbstractInlineModule( String name, Class... annotatedClasses ) {
		this.name = name;
		addApplicationContextConfigurer( new AnnotatedClassConfigurer( annotatedClasses ) );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return "inline test module";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
	}
}
