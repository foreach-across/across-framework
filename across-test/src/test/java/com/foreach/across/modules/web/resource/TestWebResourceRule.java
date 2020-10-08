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

import com.foreach.across.modules.web.context.WebAppLinkBuilder;
import com.foreach.across.modules.web.ui.*;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static com.foreach.across.modules.web.resource.WebResource.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings( "deprecation" )
public class TestWebResourceRule extends AbstractViewElementTemplateTest
{
	@Autowired
	private WebAppLinkBuilder webAppLinkBuilder;

	@BeforeEach
	public void setBuilderContext() {
		DefaultViewElementBuilderContext defaultViewElementBuilderContext = new DefaultViewElementBuilderContext();
		defaultViewElementBuilderContext.setWebAppLinkBuilder( webAppLinkBuilder );
		ViewElementBuilderContextHolder.setViewElementBuilderContext( defaultViewElementBuilderContext );
	}

	@AfterEach
	public void resetBuilderContext() {
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}

	@Test
	public void webResourceRuleSortWorks() {
		WebResourceRegistry registry = new WebResourceRegistry( null );
		registry.apply(
				WebResourceRule.add( WebResource.css( "/css/1.css" ) ).order( 1 ).withKey( "first-css" ).toBucket( WebResource.CSS ),
				WebResourceRule.add( WebResource.css( "/css/4.css" ) ).after( "third-css" ).withKey( "fourth-css" ).toBucket( WebResource.CSS ),
				WebResourceRule.add( WebResource.css( "/css/3.css" ) ).withKey( "third-css" ).toBucket( WebResource.CSS ),
				WebResourceRule.add( WebResource.css( "/css/2.css" ) ).before( "third-css" ).withKey( "second-css" ).toBucket( WebResource.CSS )
		);
		renderAndExpect( registry.getResourcesForBucket( CSS ),
		                 "<link rel=\"stylesheet\" href=\"/css/1.css\" type=\"text/css\"></link>" +
				                 "<link rel=\"stylesheet\" href=\"/css/2.css\" type=\"text/css\"></link>" +
				                 "<link rel=\"stylesheet\" href=\"/css/3.css\" type=\"text/css\"></link>" +
				                 "<link rel=\"stylesheet\" href=\"/css/4.css\" type=\"text/css\"></link>" );
	}

	@Test
	public void addRendersViewElements() {
		WebResourceRegistry registry = new WebResourceRegistry( null );

		registry.apply(
				WebResourceRule.add( WebResource.css( "favicon.ico" ).rel( "icon" ).type( "image/x-icon" ) ).withKey( "favicon" ).toBucket( "FAVICON" ),
				WebResourceRule.add( WebResource.css( "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" ).rel( "alternate" ) )
				               .withKey( "bootstrap-3.3.5-min-css" )
				               .toBucket( "CSS_CDN" ),
				WebResourceRule.add( WebResource.css( "@static:/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.css().inline( "body {background-color: powderblue;}" ) ).withKey( "inline-body-blue" ).toBucket( CSS ),
				WebResourceRule.add( WebResource.javascript().inline( "alert('hello world');" ) ).withKey( "alert-page-top" ).toBucket( JAVASCRIPT ),
				WebResourceRule.add( WebResource.javascript( "bootstrap.min.js" ).defer().async() ).withKey( "bootstrap-min-js" )
				               .toBucket( JAVASCRIPT_PAGE_END ),
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
				WebResourceRule.add(
						WebResource.link().crossOrigin( "use-credentials" ).type( "application/json" ).rel( "license" )
						           .url( "https://en.wikipedia.org/wiki/BSD_licenses" )
				).withKey( "rel-license" ).toBucket( "custom-element" ),
				WebResourceRule.add( WebResource.meta().metaName( "keywords" ).content( "HTML, CSS, XML, HTML" ) ).withKey( "meta-keywords" ).toBucket( HEAD ),
				WebResourceRule.add( WebResource.meta().refresh( "30;URL=https://www.google.com/" ) ).withKey( "meta-refresh" ).toBucket( HEAD )
		);

		assertEquals( 8, registry.getBuckets().size() );

		renderAndExpect( registry.getResourcesForBucket( "FAVICON" ),
		                 "<link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\" />" );

		renderAndExpect( registry.getResourcesForBucket( "CSS_CDN" ),
		                 "<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"alternate\" type=\"text/css\"></link>" );

		renderAndExpect( registry.getResourcesForBucket( CSS ),
		                 "<link href=\"/across/resources/static/css/bootstrap.min.css\" rel=\"stylesheet\" type=\"text/css\"></link><style type=\"text/css\">body {background-color: powderblue;}</style>" );

		renderAndExpect( registry.getResourcesForBucket( JAVASCRIPT ),
		                 "<script type=\"text/javascript\">alert('hello world');</script>" );

		renderAndExpect( registry.getResourcesForBucket( JAVASCRIPT_PAGE_END ),
		                 "<script src=\"/bootstrap.min.js\" defer=\"defer\" async=\"async\" type=\"text/javascript\"></script><script src=\"/across/resources/bootstrapui.js\" type=\"text/javascript\"></script>" );

		renderAndExpect( registry.getResourcesForBucket( "base" ),
		                 "<base href=\"https://www.w3schools.com/images/\" target=\"_blank\"></base>" );

		renderAndExpect( registry.getResourcesForBucket( "custom-element" ),
		                 "<link crossorigin=\"use-credentials\" rel=\"license\" href=\"https://en.wikipedia.org/wiki/BSD_licenses\" type=\"application/json\"></link>" );

		renderAndExpect( registry.getResourcesForBucket( HEAD ),
		                 "<meta name=\"keywords\" content=\"HTML, CSS, XML, HTML\"></meta><meta http-equiv=\"refresh\" content=\"30;URL=https://www.google.com/\"></meta>" );
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
		assertFalse( resourceRegistry.getResourcesForBucket( "custom-package-css" ).iterator().hasNext() );

		resourceRegistry.apply( WebResourceRule.remove().withKey( "bootstrap-min-css" ).fromBucket( CSS ) );
		resourceRegistry.apply( WebResourceRule.remove().withKey( "date-pickers" ) );

		assertFalse( resourceRegistry.getResourcesForBucket( "date-picker-bucket-css" ).iterator().hasNext() );
		assertFalse( resourceRegistry.getResourcesForBucket( "date-picker-bucket-js" ).iterator().hasNext() );

		assertThat( resourceRegistry.getResourcesForBucket( CSS ) ).hasSize( 1 );

		Iterator<WebResourceReference> it = resourceRegistry.getResourcesForBucket( JAVASCRIPT_PAGE_END ).iterator();
		assertEquals( "bootstrap-min-js", it.next().getKey() );
		assertEquals( "bootstrap-extra-min-js", it.next().getKey() );
		assertEquals( "bootstrap-custom-min-js", it.next().getKey() );
	}

	@SuppressWarnings("deprecation")
	class BootstrapUiFormElementsWebResources extends SimpleWebResourcePackage
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
