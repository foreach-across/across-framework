package com.foreach.across.core;

import com.foreach.across.core.installers.AcrossCoreInstaller;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

//@Component(AcrossCoreModule.NAME)
@DependsOn("AcrossCoreSchemaInstaller")
public class AcrossCoreModule extends AcrossModule
{
	public static final String NAME = "Across";

	@Autowired
	private ApplicationContext applicationContext;

	public String getName() {
		return NAME;
	}

	@Override
	protected Class[] installerClasses() {
		return new Class[] { AcrossCoreInstaller.class };
	}
}
