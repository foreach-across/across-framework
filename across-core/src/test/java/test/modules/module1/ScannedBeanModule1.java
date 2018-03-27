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

package test.modules.module1;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import test.modules.TestContextEventListener;
import test.modules.module2.ScannedBeanModule2;
import test.modules.module2.TestModule2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import test.modules.module2.ScannedBeanModule2;
import test.modules.module2.TestModule2;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Exposed
public class ScannedBeanModule1 extends TestContextEventListener
{
	public static final AtomicInteger CONSTRUCTION_COUNTER = new AtomicInteger();

	private boolean postConstructed = false;

	@Autowired(required = false)
	private TestModule2 referenceToModule2;

	@Autowired(required = false)
	private ScannedBeanModule2 referenceToBeanFromModule2;

	@Value("${module1.beanValue:}")
	private String beanValue;

	@Autowired
	private AcrossModule defaultModule;

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossModule currentModule;

	public TestModule2 getReferenceToModule2() {
		return referenceToModule2;
	}

	public ScannedBeanModule2 getReferenceToBeanFromModule2() {
		return referenceToBeanFromModule2;
	}

	public boolean isPostConstructed() {
		return postConstructed;
	}

	public String getBeanValue() {
		return beanValue;
	}

	public ScannedBeanModule1() {
		CONSTRUCTION_COUNTER.incrementAndGet();
	}

	@PostConstruct
	public void postConstruct() {
		postConstructed = true;
		CONSTRUCTION_COUNTER.incrementAndGet();
	}

	public AcrossModule getCurrentModule() {
		return currentModule;
	}

	public AcrossModule getDefaultModule() {
		return defaultModule;
	}
}
