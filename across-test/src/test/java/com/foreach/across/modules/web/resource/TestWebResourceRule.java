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
package com.foreach.across.modules.web.resource;

import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
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

		Map<String, String> statics = new HashMap<String, String>()
		{{
			put( "static", "@static:/" );
			put( "admin", "@adminWeb:/" );
		}};
		Map<String, Object> vars = new HashMap<String, Object>()
		{{
			put( "rootPaths", statics );
			put( "language", "nl" );
		}};

		resourceRegistry.apply(
				WebResourceRule.add( WebResource.css( "/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.css().inline( "body {background-color: powderblue;}" ) ).withKey( "inline-body-blue" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.javascript().inline( "alert('hello world');" ) ).withKey( "alert-page-top" ).toBucket( JAVASCRIPT ),
				WebResourceRule.add( WebResource.javascript().data( vars ) ).withKey( "alert-page-top" ).toBucket( "javascript_vars" ),
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
		assertEquals( 2, resourceRegistry.getBucketResources( CSS ).size() );
		assertEquals( 1, resourceRegistry.getBucketResources( JAVASCRIPT ).size() );
		assertEquals( 1, resourceRegistry.getBucketResources( "javascript_vars" ).size() );
		assertEquals( 2, resourceRegistry.getBucketResources( JAVASCRIPT_PAGE_END ).size() );
		assertEquals( 1, resourceRegistry.getBucketResources( "base" ).size() );

		Collection<ViewElement> resources = resourceRegistry.getBucketResources();
		assertEquals( 7, resources.size() );
		Iterator<ViewElement> it = resources.iterator();

		renderAndExpect( it.next(), "<link href=\"/css/bootstrap.min.css\" type=\"text/css\"></link>" );
		renderAndExpect( it.next(), "<style type=\"text/css\">body {background-color: powderblue;}</style>" );
		renderAndExpect( it.next(), "<script type=\"text/javascript\">alert('hello world');</script>" );
		renderAndExpect( it.next(), "<script type=\"text/javascript\">(function ( Across ) {\n" +
				"var data={\"rootPaths\":{\"static\":\"@static:/\",\"admin\":\"@adminWeb:/\"},\"language\":\"nl\"};\n" +
				"for(var key in data) Across[key] = data[key];\n" +
				"        })( window.Across = window.Across || {} );\n" +
				"</script>" );
		renderAndExpect( it.next(), "<script src=\"bootstrap.min.js\" type=\"text/javascript\"></script>" );
		renderAndExpect( it.next(), "<script src=\"bootstrapui.js\" type=\"text/javascript\"></script>" );
		renderAndExpect( it.next(), "<base href=\"https://www.w3schools.com/images/\" target=\"_blank\"></base>" );
	}

	@Test
	public void legacyWebResources() {
		WebResourcePackageManager packageManager = new WebResourcePackageManager();
		packageManager.register( "BOOTSTRAP", new BootstrapUiFormElementsWebResources() );
		WebResourceRegistry resourceRegistry = new WebResourceRegistry( packageManager );

		Map<String, String> adminWebPathVariables = Collections.singletonMap( "rootPath", "@adminWeb:/" );
		resourceRegistry.addWithKey( "custom-bucket", "custom-bucket-key", "somedata" );
		resourceRegistry.addWithKey( WebResource.JAVASCRIPT, "AdminWebModule-data", adminWebPathVariables, WebResource.DATA );
		resourceRegistry.addWithKey( JAVASCRIPT, "AdminWebModule-date-time-css",
		                             "https://cdn.jsdelivr.net/webjars/org.webjars/Eonasdan-bootstrap-datetimepicker/4.14.30/bootstrap-datetimepicker.css" );

		resourceRegistry.apply(
				WebResourceRule.addPackage( "BOOTSTRAP" ),
				WebResourceRule.add( WebResource.javascript().data( adminWebPathVariables ) ).withKey( "AdminWebModule" ).toBucket( JAVASCRIPT )
		);

		assertEquals( 1, resourceRegistry.getResources( "custom-bucket" ).size() );
		assertEquals( 2, resourceRegistry.getResources( "javascript" ).size() );
	}

	@Test
	public void removeDoesNotRenderViewElement() {
		WebResourcePackageManager packageManager = new WebResourcePackageManager();
		WebResourcePackage customResourcePackage = new SimpleWebResourcePackage(
				Collections.singleton( new WebResource( "custom-package-css", "CUSTOM-PACKAGE",
				                                        "package.css",
				                                        WebResource.EXTERNAL ) ) );
		packageManager.register( "CUSTOM-PACKAGE", customResourcePackage );
		WebResourceRegistry resourceRegistry = new WebResourceRegistry( packageManager );

		resourceRegistry.apply(
				WebResourceRule.addPackage( "CUSTOM-PACKAGE" ),
				WebResourceRule.add( WebResource.css( "/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.css( "/css/bootstrap-extra.min.css" ) ).withKey( "bootstrap-extra-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.javascript( "bootstrap.min.js" ) ).withKey( "bootstrap-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.javascript( "bootstrap-extra.min.js" ) ).withKey( "bootstrap-extra-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.javascript( "bootstrap-custom.min.js" ) ).withKey( "bootstrap-custom-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.css( "/foo.css" ) ).withKey( "date-pickers" ).toBucket( "date-picker-bucket-css" ),
				WebResourceRule.add( WebResource.css( "/foo.js" ) ).withKey( "date-pickers" ).toBucket( "date-picker-bucket-js" )
		);

		assertEquals( 1, resourceRegistry.getResources( "custom-package-css" ).size() );
		resourceRegistry.removePackage( "CUSTOM-PACKAGE" );
		assertEquals( 0, resourceRegistry.getBucketResources( "custom-package-css" ).size() );

		resourceRegistry.apply( WebResourceRule.remove().withKey( "bootstrap-min-css" ).fromBucket( CSS ) );
		resourceRegistry.apply( WebResourceRule.remove().withKey( "date-pickers" ) );
		assertEquals( 0, resourceRegistry.getBucketResources( "date-picker-bucket-css" ).size() );
		assertEquals( 0, resourceRegistry.getBucketResources( "date-picker-bucket-js" ).size() );
		assertEquals( 1, resourceRegistry.getBucketResources( CSS ).size() );
		assertEquals( 3, resourceRegistry.getBucketResources( JAVASCRIPT_PAGE_END ).size() );
	}

	@Test
	public void mergeTwoRegistries() {
		WebResourceRegistry original = new WebResourceRegistry( null );
		WebResourceRegistry additional = new WebResourceRegistry( null );

		original.apply( WebResourceRule.add( WebResource.css( "/one.css" ) ).toBucket( "bucket-css" ),
		                WebResourceRule.add( WebResource.javascript( "/one.js" ) ).toBucket( "bucket-js" ) );

		additional.apply( WebResourceRule.add( WebResource.css( "two.css" ) ).toBucket( "bucket-css" ),
		                  WebResourceRule.add( WebResource.javascript( "/two-1.js" ) ).toBucket( "bucket-two-js" ),
		                  WebResourceRule.add( WebResource.javascript( "/two-2.js" ) ).toBucket( "bucket-two-js" ),
		                  WebResourceRule.add( WebResource.javascript( "/two-3.js" ) ).toBucket( "bucket-two-js" )
		);

		original.merge( additional );
		assertEquals( 2, original.getBucketResources( "bucket-css" ).size() );
		assertEquals( 1, original.getBucketResources( "bucket-js" ).size() );
		assertEquals( 3, original.getBucketResources( "bucket-two-js" ).size() );

		additional.clear();
		assertEquals( 6, original.getBucketResources().size() );
		assertEquals( 0, additional.getBucketResources().size() );

		original.clear( "bucket-js" );
		assertEquals( 5, original.getBucketResources().size() );
		assertEquals( 0, additional.getBucketResources().size() );

		original.clear();
		assertEquals( 0, original.getBucketResources().size() );
		assertEquals( 0, additional.getBucketResources().size() );
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
