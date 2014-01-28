package com.foreach.across.test.modules.module1;

import com.foreach.across.core.AcrossModule;
import org.springframework.beans.factory.annotation.Value;

public class TestModule1 extends AcrossModule
{
	@Value("${module1.version}")
	private String version;

	public String getVersion() {
		return version;
	}

	@Override
	public String getName() {
		return "TestModule1";
	}

	@Override
	public String getDescription() {
		return "Module 1 for unit/integration tests";
	}
}
