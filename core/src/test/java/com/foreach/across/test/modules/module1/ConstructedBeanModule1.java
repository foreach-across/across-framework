package com.foreach.across.test.modules.module1;

import com.foreach.across.test.modules.TestModuleEventListener;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class ConstructedBeanModule1 extends TestModuleEventListener
{
	private final String text;

	@Autowired
	private ScannedBeanModule1 scannedBeanModule1;

	@Autowired(required = false)
	private ScannedBeanModule2 scannedBeanModule2;

	public ConstructedBeanModule1( String text ) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public ScannedBeanModule1 getScannedBeanModule1() {
		return scannedBeanModule1;
	}

	public ScannedBeanModule2 getScannedBeanModule2() {
		return scannedBeanModule2;
	}
}
