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

import com.foreach.across.modules.web.resource.*;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;

import java.util.*;

import static com.foreach.across.modules.web.resource.WebResource.*;
import static org.junit.Assert.assertEquals;

public class TestWebResourceRule extends AbstractViewElementTemplateTest
{
	@Test
	public void addRendersViewElements() {
		WebResourceRegistry resourceRegistry = new WebResourceRegistry( null );
		WebResourcePackage bootstrapUiPackage = new BootstrapUiFormElementsWebResources();

		resourceRegistry.apply(
				WebResourceRule.addPackage( bootstrapUiPackage ),
				WebResourceRule.add( WebResource.css( "/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.javascript( "bootstrap.min.js" ) ).withKey( "bootstrap-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.javascript( "bootstrapui.js" ) ).withKey( "BootstrapUiModule-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( new AbstractWebResourceBuilder()
				{
					@Override
					public MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
						NodeViewElement element = new NodeViewElement( "base" );
						element.setAttribute( "href", "https://www.w3schools.com/images/" );
						element.setAttribute( "target", "_blank" );
						return element;
					}
				} ).toBucket( "base" )
		);

		assertEquals( 0, resourceRegistry.getBucketResources( UUID.randomUUID().toString() ).size() );
		assertEquals( 1, resourceRegistry.getBucketResources( CSS ).size() );
		assertEquals( 2, resourceRegistry.getBucketResources( JAVASCRIPT_PAGE_END ).size() );
		assertEquals( 1, resourceRegistry.getBucketResources( "base" ).size() );

		Collection<ViewElement> resources = resourceRegistry.getBucketResources();
		assertEquals( 4, resources.size() );
		Iterator<ViewElement> it = resources.iterator();

		renderAndExpect( it.next(), "<link href=\"/css/bootstrap.min.css\" type=\"text/css\"></link>" );
		renderAndExpect( it.next(), "<script src=\"bootstrap.min.js\" type=\"text/javascript\"></script>" );
		renderAndExpect( it.next(), "<script src=\"bootstrapui.js\" type=\"text/javascript\"></script>" );
		renderAndExpect( it.next(), "<base href=\"https://www.w3schools.com/images/\" target=\"_blank\"></base>" );
	}

	@Test
	public void legacyWebResources() {
		WebResourceRegistry resourceRegistry = new WebResourceRegistry( null );
		WebResourcePackage bootstrapUiPackage = new BootstrapUiFormElementsWebResources();

		Map<String, String> adminWebPathVariables = Collections.singletonMap( "rootPath", "@adminWeb:/" );
		resourceRegistry.addWithKey( "custom-bucket", "custom-bucket-key", "somedata" );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "AdminWebModule-data", adminWebPathVariables, WebResource.DATA );
		resourceRegistry.addWithKey( JAVASCRIPT, "AdminWebModule-date-time-css",
		                             "https://cdn.jsdelivr.net/webjars/org.webjars/Eonasdan-bootstrap-datetimepicker/4.14.30/bootstrap-datetimepicker.css" );

		resourceRegistry.apply(
				WebResourceRule.addPackage( bootstrapUiPackage ),
				WebResourceRule.add( WebResource.javascript().data( adminWebPathVariables ) ).withKey( "AdminWebModule" ).toBucket( JAVASCRIPT )
		);

		assertEquals( 1, resourceRegistry.getResources( "custom-bucket" ).size() );
		assertEquals( 2, resourceRegistry.getResources( "javascript" ).size() );
	}

	@Test
	public void removeDoesNotRenderViewElement() {
		WebResourceRegistry resourceRegistry = new WebResourceRegistry( null );

		resourceRegistry.apply(
				//WebResourceRule.addPackage( jquery ),
				WebResourceRule.add( WebResource.css( "/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.css( "/css/bootstrap-extra.min.css" ) ).withKey( "bootstrap-extra-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.javascript( "bootstrap.min.js" ) ).withKey( "bootstrap-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.javascript( "bootstrap-extra.min.js" ) ).withKey( "bootstrap-extra-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.javascript( "bootstrap-custom.min.js" ) ).withKey( "bootstrap-custom-min-js" ).toBucket( JAVASCRIPT_PAGE_END )
		);

		resourceRegistry.apply( WebResourceRule.remove().withKey( "bootstrap-min-css" ).fromBucket( CSS ) );
		assertEquals( 1, resourceRegistry.getBucketResources( CSS ).size() );
		assertEquals( 3, resourceRegistry.getBucketResources( JAVASCRIPT_PAGE_END ).size() );
	}

	public class BootstrapUiFormElementsWebResources extends SimpleWebResourcePackage
	{
		private static final String MODULE_NAME = "BootstrapUiModule";
		public static final String NAME = "bootstrapui-formelements";

		public BootstrapUiFormElementsWebResources() {

			setWebResources(
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME + "-momentjs",
					                 "https://cdn.jsdelivr.net/webjars/momentjs/2.10.6/moment-with-locales.js",
					                 WebResource.EXTERNAL ),
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME + "-momentjs-locale-nl-BE",
					                 "/static/" + MODULE_NAME + "/js/moment/locale-nl-BE.js",
					                 WebResource.VIEWS ),
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME + "-datetimepicker",
					                 "https://cdn.jsdelivr.net/webjars/org.webjars/Eonasdan-bootstrap-datetimepicker/4.14.30/bootstrap-datetimepicker.min.js",
					                 WebResource.EXTERNAL ),
					new WebResource( WebResource.CSS, NAME + "-datetimepicker-css",
					                 "https://cdn.jsdelivr.net/webjars/org.webjars/Eonasdan-bootstrap-datetimepicker/4.14.30/bootstrap-datetimepicker.css",
					                 WebResource.EXTERNAL ),
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME + "-numeric",
					                 "https://cdn.jsdelivr.net/webjars/org.webjars.bower/autoNumeric/1.9.30/autoNumeric.js",
					                 WebResource.EXTERNAL ),
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME + "-autosize",
					                 "/webjars/autosize/3.0.20/dist/autosize.min.js",
					                 WebResource.RELATIVE ),

					// Bootstrap select
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, MODULE_NAME + "-select",
					                 "//cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.2/js/bootstrap-select.min.js",
					                 WebResource.EXTERNAL ),
					new WebResource( WebResource.CSS, MODULE_NAME + "-select",
					                 "//cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.2/css/bootstrap-select.min.css",
					                 WebResource.EXTERNAL ),

					// Form elements initializer javascript
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME,
					                 "/static/" + MODULE_NAME + "/js/bootstrapui-formelements.js",
					                 WebResource.VIEWS ),
					new WebResource( WebResource.JAVASCRIPT_PAGE_END, "typeahead",
					                 "https://cdnjs.cloudflare.com/ajax/libs/typeahead.js/0.11.1/typeahead.bundle.min.js",
					                 WebResource.EXTERNAL ),
					new WebResource( WebResource.CSS, "autosuggest",
					                 "/static/BootstrapUiModule/css/bootstrapui.css",
					                 WebResource.VIEWS )

			);
		}
	}

}
