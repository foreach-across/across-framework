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

import com.foreach.across.modules.web.template.Template;
import com.foreach.across.test.web.module.ui.ErrorTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@ControllerAdvice(assignableTypes = PrefixedController.class)
public class ErrorAdvice
{
	@Template(ErrorTemplate.NAME)
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView handleException( Exception e ) {
		ModelAndView mav = new ModelAndView( "th/webControllers/errorDetail" );
		mav.addObject( "message", e.getMessage() );

		return mav;
	}
}
