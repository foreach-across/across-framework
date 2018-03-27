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
package com.foreach.across.test.application.app.application.controllers;

import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Custom view resolver for a runtime exception.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Component
public class ExceptionErrorViewResolver implements ErrorViewResolver
{
	@Override
	public ModelAndView resolveErrorView( HttpServletRequest request, HttpStatus status, Map<String, Object> model ) {
		if ( status.is5xxServerError() ) {
			return new ModelAndView( "th/dummy/5xx.html" );
		}

		return null;
	}
}
