package com.foreach.across.test.modules.module2;

import com.foreach.across.test.modules.TestModuleEventListener;
import com.foreach.across.test.modules.module1.ConstructedBeanModule1;
import com.foreach.across.test.modules.module1.ScannedBeanModule1;
import org.springframework.beans.factory.annotation.Autowired;

public class ConstructedBeanModule2 extends TestModuleEventListener
{
	private final String text;

	@Autowired
	private ScannedBeanModule1 scannedBeanModule1;

	@Autowired
	private ConstructedBeanModule1 constructedBeanModule1;

	@Autowired
	private ScannedBeanModule2 scannedBeanModule2;

	public ConstructedBeanModule2( String text ) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public ConstructedBeanModule1 getConstructedBeanModule1() {
		return constructedBeanModule1;
	}

	public ScannedBeanModule1 getScannedBeanModule1() {
		return scannedBeanModule1;
	}

	public ScannedBeanModule2 getScannedBeanModule2() {
		return scannedBeanModule2;
	}
}
