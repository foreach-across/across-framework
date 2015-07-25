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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.test.modules.webtest.controllers.RenderViewElementController;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author Arne Vandamme
 */
public class TestTemplateViewElement extends AbstractViewElementTemplateTest
{
	@Before
	public void globalCallback() {
		setCallback( new RenderViewElementController.Callback()
		{
			@Override
			public void prepareModel( ModelMap model ) {
				model.put( "customAttribute", 123 );
			}
		} );
	}

	@Test
	public void customTemplateFromTestLibrary() {
		TemplateViewElement template = new TemplateViewElement( CUSTOM_TEMPLATE );
		renderAndExpect( template, CUSTOM_TEMPLATE_OUTPUT );
	}

	@Test
	public void customAttributeSetThroughGlobalCallback() {
		TemplateViewElement template = new TemplateViewElement( "th/test/elements/text :: customAttribute" );
		renderAndExpect( template, "Custom attribute value: 123" );
	}

	@Test
	public void customAttributeSetThroughSimpleCallback() {
		TemplateViewElement template = new TemplateViewElement( "th/test/elements/text :: customAttribute" );

		renderAndExpect( template,
		                 new RenderViewElementController.Callback()
		                 {
			                 @Override
			                 public void prepareModel( ModelMap model ) {
				                 RequestContextHolder
						                 .getRequestAttributes()
						                 .setAttribute( "customAttribute", "hello!", RequestAttributes.SCOPE_REQUEST );
			                 }
		                 },
		                 "Custom attribute value: hello!" );
		renderAndExpect( template, "Custom attribute value: 123" );
	}
}
