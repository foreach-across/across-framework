/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.test.modules.module1;

import com.foreach.across.test.modules.TestEvent;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;

import java.util.List;

public interface ConstructedBeanModule1
{
	List<SomeInterface> getSomeInterfaces();

	String getText();

	String getOtherText();

	ScannedBeanModule1 getScannedBeanModule1();

	ScannedBeanModule2 getScannedBeanModule2();

	List<TestEvent> getEventsReceived();

	void receiveEvent( TestEvent event );
}
