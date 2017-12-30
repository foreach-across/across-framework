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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
@Controller
public class HelloApplicationController
{
	@RequestMapping("/application")
	@ResponseBody
	public String hello() {
		return "application says hello";
	}

	@RequestMapping("/stringToDateConverterWithoutAnnotationPattern")
	@ResponseBody
	public String dateConversion( @RequestParam Date time ) {
		return time.toString();
	}

	@RequestMapping("/stringToDateConverterWithAnnotationPattern")
	@ResponseBody
	public String dateConversionWithPattern( @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") Date time ) {
		return time.toString();
	}
}
