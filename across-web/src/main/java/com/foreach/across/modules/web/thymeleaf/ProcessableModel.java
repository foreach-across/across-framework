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
package com.foreach.across.modules.web.thymeleaf;

import org.thymeleaf.model.IModel;

/**
 * Created by slyoldfox on 26/10/16.
 */
public class ProcessableModel
{
	private IModel model;
	private boolean processable;

	public ProcessableModel( IModel model, boolean processable ) {
		this.model = model;
		this.processable = processable;
	}

	public IModel getModel() {
		return model;
	}

	public boolean isProcessable() {
		return processable;
	}
}
