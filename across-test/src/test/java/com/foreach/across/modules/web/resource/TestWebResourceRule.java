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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.web.context.WebAppLinkBuilder;
import com.foreach.across.modules.web.ui.*;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.foreach.across.modules.web.resource.WebResource.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestWebResourceRule extends AbstractViewElementTemplateTest
{
	@Autowired
	private WebAppLinkBuilder webAppLinkBuilder;

	@Before
	public void setBuilderContext() {
		DefaultViewElementBuilderContext defaultViewElementBuilderContext = new DefaultViewElementBuilderContext();
		defaultViewElementBuilderContext.setWebAppLinkBuilder( webAppLinkBuilder );
		ViewElementBuilderContextHolder.setViewElementBuilderContext( defaultViewElementBuilderContext );
	}

	@After
	public void resetBuilderContext() {
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}

	@Test
	public void addRendersViewElements() {
		WebResourceRegistry registry = new WebResourceRegistry( null );

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

		registry.apply(
				WebResourceRule.add( WebResource.css( "favicon.ico" ).rel( "icon" ).type( "image/x-icon" ) ).withKey( "favicon" ).toBucket( "FAVICON" ),
				WebResourceRule.add( WebResource.css( "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" ).rel( "alternate" ) )
				               .withKey( "bootstrap-3.3.5-min-css" )
				               .toBucket( "CSS_CDN" ),
				WebResourceRule.add( WebResource.css( "@static:/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.css().inline( "body {background-color: powderblue;}" ) ).withKey( "inline-body-blue" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.javascript().inline( "alert('hello world');" ) ).withKey( "alert-page-top" ).toBucket( JAVASCRIPT ),
				WebResourceRule.add( WebResource.javascript().data( vars ).snippet( ( data ) -> {
					try {
						return "(function ( Across ) {\n" +
								"var data=" + new ObjectMapper().writeValueAsString( data ) + ";\n" +
								"for(var key in data) Across[key] = data[key];\n" +
								"        })( window.Across = window.Across || {} );\n";
					}
					catch ( JsonProcessingException e ) {
						throw new RuntimeException( e );
					}
				} ) ).withKey( "alert-page-top" ).toBucket( "javascript_vars" ),
				WebResourceRule.add( WebResource.javascript( "bootstrap.min.js" ) ).withKey( "bootstrap-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( WebResource.javascript( "@resource:bootstrapui.js" ) ).withKey( "BootstrapUiModule-js" ).toBucket( JAVASCRIPT_PAGE_END ),
				WebResourceRule.add( new ViewElementBuilderSupport()
				{
					@Override
					public MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
						NodeViewElement element = new NodeViewElement( "base" );
						element.setAttribute( "href", "https://www.w3schools.com/images/" );
						element.setAttribute( "target", "_blank" );
						return element;
					}
				} ).withKey( "base-w3-school" ).toBucket( "base" ),
				WebResourceRule.add( new CssWebResourceBuilder()
				{
					@Override
					public MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
						MutableViewElement element = super.createElement( builderContext );
						( (AbstractNodeViewElement) element ).setAttribute( "crossorigin", "use-credentials" );
						return element;
					}
				}.type( "application/json" ).rel( "license" ).url( "https://en.wikipedia.org/wiki/BSD_licenses" ) ).withKey( "rel-license" )
				               .toBucket( "custom-element" )
		);

		assertEquals( 8, registry.getBuckets().size() );

		renderAndExpect( registry.getBucketResources( "FAVICON" ),
		                 "<link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\" />" );

		renderAndExpect( registry.getBucketResources( "CSS_CDN" ),
		                 "<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"alternate\" type=\"text/css\"></link>" );

		renderAndExpect( registry.getBucketResources( CSS ),
		                 "<link href=\"/across/resources/static/css/bootstrap.min.css\" rel=\"stylesheet\" type=\"text/css\"></link><style type=\"text/css\">body {background-color: powderblue;}</style>" );

		renderAndExpect( registry.getBucketResources( JAVASCRIPT ),
		                 "<script type=\"text/javascript\">alert('hello world');</script>" );

		renderAndExpect( registry.getBucketResources( "javascript_vars" ),
		                 "<script type=\"text/javascript\">(function ( Across ) {\n" +
				                 "var data={\"rootPaths\":{\"static\":\"@static:/\",\"admin\":\"@adminWeb:/\"},\"language\":\"nl\"};\n" +
				                 "for(var key in data) Across[key] = data[key];\n" +
				                 "        })( window.Across = window.Across || {} );\n" +
				                 "</script>" );

		renderAndExpect( registry.getBucketResources( JAVASCRIPT_PAGE_END ),
		                 "<script src=\"/bootstrap.min.js\" type=\"text/javascript\"></script><script src=\"/across/resources/bootstrapui.js\" type=\"text/javascript\"></script>" );

		renderAndExpect( registry.getBucketResources( "base" ),
		                 "<base href=\"https://www.w3schools.com/images/\" target=\"_blank\"></base>" );

		renderAndExpect( registry.getBucketResources( "custom-element" ),
		                 "<link crossorigin=\"use-credentials\" rel=\"license\" href=\"https://en.wikipedia.org/wiki/BSD_licenses\" type=\"application/json\"></link>" );
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
		assertFalse( resourceRegistry.getBucketResources( "custom-package-css" ).iterator().hasNext() );

		resourceRegistry.apply( WebResourceRule.remove().withKey( "bootstrap-min-css" ).fromBucket( CSS ) );
		resourceRegistry.apply( WebResourceRule.remove().withKey( "date-pickers" ) );

		assertFalse( resourceRegistry.getBucketResources( "date-picker-bucket-css" ).iterator().hasNext() );
		assertFalse( resourceRegistry.getBucketResources( "date-picker-bucket-js" ).iterator().hasNext() );

		size( 1, resourceRegistry.getBucketResources( CSS ) );

		Iterator<WebResourceReference> it = resourceRegistry.getBucketResources( JAVASCRIPT_PAGE_END ).iterator();
		assertEquals( "bootstrap-min-js", it.next().getKey() );
		assertEquals( "bootstrap-extra-min-js", it.next().getKey() );
		assertEquals( "bootstrap-custom-min-js", it.next().getKey() );
	}

	@Test
	public void mergeTwoRegistries() {
		WebResourceRegistry original = new WebResourceRegistry( null );
		WebResourceRegistry additional = new WebResourceRegistry( null );

		original.apply( WebResourceRule.add( WebResource.css( "/one.css" ) ).withKey( "1-css" ).toBucket( "bucket-css" ),
		                WebResourceRule.add( WebResource.javascript( "/one.js" ) ).withKey( "1-js" ).toBucket( "bucket-js" ) );

		additional.apply( WebResourceRule.add( WebResource.css( "two.css" ) ).withKey( "2-css" ).toBucket( "bucket-css" ),
		                  WebResourceRule.add( WebResource.javascript( "/two-1.js" ) ).withKey( "2-1.js" ).toBucket( "bucket-two-js" ),
		                  WebResourceRule.add( WebResource.javascript( "/two-2.js" ) ).withKey( "2-2.js" ).toBucket( "bucket-two-js" ),
		                  WebResourceRule.add( WebResource.javascript( "/two-3.js" ) ).withKey( "2-3.js" ).toBucket( "bucket-two-js" )
		);

		original.merge( additional );
		size( 2, original.getBucketResources( "bucket-css" ) );
		size( 1, original.getBucketResources( "bucket-js" ) );
		size( 3, original.getBucketResources( "bucket-two-js" ) );

		additional.clear();
		size( 2, original.getBucketResources( "bucket-css" ) );
		size( 1, original.getBucketResources( "bucket-js" ) );
		size( 3, original.getBucketResources( "bucket-two-js" ) );
		size( 0, additional.getBucketResources( "bucket-css" ) );
		size( 0, additional.getBucketResources( "bucket-js" ) );
		size( 0, additional.getBucketResources( "bucket-two-js" ) );

		original.clear( "bucket-js" );
		size( 2, original.getBucketResources( "bucket-css" ) );
		size( 2, original.getBucketResources( "bucket-css" ) );
		size( 0, original.getBucketResources( "bucket-js" ) );
		size( 3, original.getBucketResources( "bucket-two-js" ) );

		original.clear();
		size( 0, original.getBucketResources( "bucket-css" ) );
		size( 0, original.getBucketResources( "bucket-css" ) );
		size( 0, original.getBucketResources( "bucket-js" ) );
		size( 0, original.getBucketResources( "bucket-two-js" ) );
	}

	public void size( int expectedSize, WebResourceReferenceCollection referenceCollection ) {
		int i = 0;
		for ( WebResourceReference ignore : referenceCollection ) {
			i++;
		}
		assertEquals( expectedSize, i );
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
