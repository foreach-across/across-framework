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
import test.modules.module2.ConstructedBeanModule2;
import test.modules.module2.ScannedBeanModule2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import test.modules.module2.ConstructedBeanModule2;
import test.modules.module2.ScannedBeanModule2;

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

	@Autowired(required = false)
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossModule currentModule;

	public ScannedBeanModule1 getScannedBeanModule1() {
		return scannedBeanModule1;
	}

	public ConstructedBeanModule2 getConstructedBeanModule2() {
		return constructedBeanModule2;
	}

	public ScannedBeanModule2 getScannedBeanModule2() {
		return scannedBeanModule2;
	}

	public AcrossModule getCurrentModule() {
		return currentModule;
	}
}
