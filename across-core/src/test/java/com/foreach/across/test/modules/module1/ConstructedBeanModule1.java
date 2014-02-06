package com.foreach.across.test.modules.module1;

import com.foreach.across.test.modules.TestEvent;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;

import java.util.List;

public interface ConstructedBeanModule1
{
	List<SomeInterface> getSomeInterfaces();

	String getText();

	ScannedBeanModule1 getScannedBeanModule1();

	ScannedBeanModule2 getScannedBeanModule2();

	List<TestEvent> getEventsReceived();
}
