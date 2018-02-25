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

package test.modules.module2;

import test.modules.TestModuleEventListener;
import test.modules.module1.ConstructedBeanModule1;
import test.modules.module1.ScannedBeanModule1;
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
