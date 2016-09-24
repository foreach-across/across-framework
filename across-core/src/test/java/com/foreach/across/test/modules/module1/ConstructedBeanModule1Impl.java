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

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.test.modules.TestModuleEventListener;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This will be a proxy because of the Interceptor aspect.
 */
@AcrossEventHandler
public class ConstructedBeanModule1Impl extends TestModuleEventListener implements ConstructedBeanModule1
{
	private String text, otherText;

	@Autowired
	private ScannedBeanModule1 scannedBeanModule1;

	@Autowired(required = false)
	private ScannedBeanModule2 scannedBeanModule2;

	@Autowired
	private List<SomeInterface> someInterfaces;

	public ConstructedBeanModule1Impl( String text ) {
		this.text = text;
	}

	public List<SomeInterface> getSomeInterfaces() {
		return someInterfaces;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getOtherText() {
		return otherText;
	}

	public ScannedBeanModule1 getScannedBeanModule1() {
		return scannedBeanModule1;
	}

	public ScannedBeanModule2 getScannedBeanModule2() {
		return scannedBeanModule2;
	}

	@PostRefresh
	private void refresh() {
		text = "i have been refreshed";
	}

	@PostRefresh
	public void otherRefresh() {
		otherText = "i have also been refreshed";
	}
}
