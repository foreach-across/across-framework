package com.foreach.across.test.modules.module1;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.test.modules.module2.ConstructedBeanModule2;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Exposed
@Scope("prototype")
public class ScannedPrototypeBeanModule1
{
	@Autowired
	private ScannedBeanModule1 scannedBeanModule1;

	@Autowired
	private ConstructedBeanModule2 constructedBeanModule2;

	@Autowired
	private ScannedBeanModule2 scannedBeanModule2;

	public ScannedBeanModule1 getScannedBeanModule1() {
		return scannedBeanModule1;
	}

	public ConstructedBeanModule2 getConstructedBeanModule2() {
		return constructedBeanModule2;
	}

	public ScannedBeanModule2 getScannedBeanModule2() {
		return scannedBeanModule2;
	}
}