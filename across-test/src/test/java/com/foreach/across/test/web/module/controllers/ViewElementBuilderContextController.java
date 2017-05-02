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

import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DevelopmentModeCondition controller verifying the default {@link com.foreach.across.modules.web.ui.ViewElementBuilderContext}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Controller
public class ViewElementBuilderContextController
{
	@ModelAttribute("modelA")
	public String modelOnly() {
		return "modelOnly";
	}

	@ModelAttribute("modelB")
	public String modelWithBuilderContext( ViewElementBuilderContext builderContext ) {
		builderContext.setAttribute( "builderContextA", "one" );
		return "modelWithBuilderContext";
	}

	@RequestMapping("/viewElementBuilderContext")
	@ResponseBody
	public String writeValues( Model model, ViewElementBuilderContext builderContext ) {
		assertNotNull( builderContext );
		assertEquals( "one", builderContext.getAttribute( "builderContextA" ) );

		model.addAttribute( "modelC", "modelFromRequestMapping" );
		builderContext.setAttribute( "builderContextB", "two" );

		List<String> modelAttributeNames = new ArrayList<>( model.asMap().keySet() );
		Collections.sort( modelAttributeNames );

		return "[model:" +
				StringUtils.join( modelAttributeNames, ',' ) +
				"][builderContext:" +
				StringUtils.join( builderContext.attributeNames(), ',' ) +
				"]";
	}
}
