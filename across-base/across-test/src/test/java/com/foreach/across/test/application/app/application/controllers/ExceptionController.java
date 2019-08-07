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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Controller
public class ExceptionController
{
	@RequestMapping("/exception")
	public String runtimeException() {
		throw new RuntimeException( "General runtime exception.." );
	}

	@RequestMapping("/unauthorized")
	public ResponseEntity unauthorized() {
		throw new UnauthorizedException();
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")  // 401
	public class UnauthorizedException extends RuntimeException
	{
	}
}
