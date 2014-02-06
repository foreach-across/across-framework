package com.foreach.across.test.modules.module2;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import org.springframework.beans.factory.annotation.Value;

@AcrossDepends(required = "TestModule1")
public class TestModule2 extends AcrossModule
{
	@Value("${module2.version}")
	private String version;

	public String getVersion() {
		return version;
	}

	@Override
	public String getName() {
		return "TestModule2";
	}

	@Override
	public String getDescription() {
		return "Module 2 for unit/integration tests";
	}
}
