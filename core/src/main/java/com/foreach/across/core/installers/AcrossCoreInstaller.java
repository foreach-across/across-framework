package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossInstaller;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component("AcrossCoreInstaller")
@DependsOn("AcrossCoreSchemaInstaller")
public class AcrossCoreInstaller extends AcrossInstaller
{
	protected void install() {
		// Nothing to do
	}
}
