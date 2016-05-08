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
package com.foreach.across.test.web.module.controllers;

import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Test controller applying the default template.
 *
 * @author Arne Vandamme
 * @since 1.1.3
 */
@Controller
public class DefaultController
{
	@ModelAttribute
	public void registerWebResources( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.add( WebResource.CSS, "/controller.css", WebResource.VIEWS );
	}

	@RequestMapping("/home")
	public String home() {
		return "th/webControllers/childPage";
	}

	@RequestMapping("/databaseError")
	public String throwDatabaseError() {
		throw new CannotAcquireLockException( "bad data access" );
	}

	@RequestMapping("/runtimeError")
	public String throwRuntimeError() {
		throw new RuntimeException( "Runtime error occurred." );
	}

	@ExceptionHandler(DataAccessException.class)
	public String databaseError() {
		return "th/webControllers/databaseError";
	}
}
