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

package com.foreach.across.test.modules.module2;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.test.modules.TestContextEventListener;
import com.foreach.across.test.modules.module1.ScannedBeanModule1;
import com.foreach.across.test.modules.module1.TestModule1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AcrossEventHandler
public class ScannedBeanModule2 extends TestContextEventListener
{
	public static final AtomicInteger CONSTRUCTION_COUNTER = new AtomicInteger();

	@Autowired(required = false)
	private TestModule1 referenceToModule1;

	@Autowired(required = false)
	private ScannedBeanModule1 referenceToBeanFromModule1;

	@Autowired
	private AcrossModule defaultModule;

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossModule currentModule;

	public TestModule1 getReferenceToModule1() {
		return referenceToModule1;
	}

	public ScannedBeanModule1 getReferenceToBeanFromModule1() {
		return referenceToBeanFromModule1;
	}

	public ScannedBeanModule2() {
		CONSTRUCTION_COUNTER.incrementAndGet();
	}

	@PostConstruct
	public void postConstruct() {
		CONSTRUCTION_COUNTER.incrementAndGet();
	}

	public AcrossModule getCurrentModule() {
		return currentModule;
	}

	public AcrossModule getDefaultModule() {
		return defaultModule;
	}
}
