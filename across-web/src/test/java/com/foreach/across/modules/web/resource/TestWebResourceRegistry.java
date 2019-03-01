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

import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestWebResourceRegistry
{
	private WebResourceRegistry registry = new WebResourceRegistry( new WebResourcePackageManager() );

	@Test
	public void noResourcesForNonExistingBucket() {
		assertThat( registry.getResourcesForBucket( "nothing" ) ).isEmpty();
	}

	@Test
	public void addReferenceWithoutKey() {
		WebResourceReference one = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference two = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		assertThat( registry.addResourceToBucket( one, "one" ) ).isTrue();
		assertThat( registry.addResourceToBucket( two, "one" ) ).isTrue();

		assertThat( registry.getResourcesForBucket( "one" ) ).containsExactly( one, two );
	}

	@Test
	public void addReferenceWithKeyReplacesByDefault() {
		WebResourceReference one = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference two = WebResourceReference.builder().key( "otherResource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference three = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference four = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		assertThat( registry.addResourceToBucket( one, "one", false ) ).isTrue();
		assertThat( registry.addResourceToBucket( two, "one" ) ).isTrue();
		assertThat( registry.addResourceToBucket( three, "one" ) ).isTrue();
		assertThat( registry.addResourceToBucket( four, "one", false ) ).isFalse();

		assertThat( registry.getResourcesForBucket( "one" ) ).containsExactly( three, two );
	}

	@Test
	public void findResourceReferenceWithKey() {
		WebResourceReference one = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference two = WebResourceReference.builder().key( "otherResource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();

		assertThat( registry.findResourceWithKeyInBucket( "resource", "one" ) ).isEmpty();

		registry.addResourceToBucket( one, "one" );
		registry.addResourceToBucket( two, "one" );
		assertThat( registry.findResourceWithKeyInBucket( "resource", "one" ) ).contains( one );
		assertThat( registry.findResourceWithKeyInBucket( "otherResource", "one" ) ).contains( two );
	}

	@Test
	public void bucketsAreSeparate() {
		WebResourceReference one = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference two = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();

		registry.addResourceToBucket( one, "one" );
		registry.addResourceToBucket( two, "two" );

		assertThat( registry.findResourceWithKeyInBucket( "resource", "one" ) ).contains( one );
		assertThat( registry.findResourceWithKeyInBucket( "resource", "two" ) ).contains( two );
	}

	@Test
	public void removeReferenceWithKeyFromBucket() {
		WebResourceReference one = WebResourceReference.builder().key( "resource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		WebResourceReference two = WebResourceReference.builder().key( "otherResource" ).viewElementBuilder( mock( ViewElementBuilder.class ) ).build();

		assertThat( registry.findResourceWithKeyInBucket( "resource", "one" ) ).isEmpty();

		registry.addResourceToBucket( one, "one" );
		registry.addResourceToBucket( two, "one" );
		assertThat( registry.getResourcesForBucket( "one" ) ).containsExactly( one, two );

		assertThat( registry.removeResourceWithKeyFromBucket( "resource", "one" ) ).contains( one );

		assertThat( registry.getResourcesForBucket( "one" ) ).containsExactly( two );
		assertThat( registry.removeResourceWithKeyFromBucket( "otherResource", "one" ) ).contains( two );

		assertThat( registry.getResourcesForBucket( "one" ) ).isEmpty();
	}

	@Test
	public void samePackageShouldOnlyBeInstalledOnce() {
		WebResourcePackageManager packageManager = new WebResourcePackageManager();
		WebResourcePackage resourcePackage = mock( WebResourcePackage.class );
		packageManager.register( "myPackage", resourcePackage );

		WebResourceRegistry registry = new WebResourceRegistry( packageManager );
		registry.addPackage( "myPackage" );
		verify( resourcePackage ).install( registry );

		registry.addPackage( "myPackage" );
		verify( resourcePackage, times( 1 ) ).install( registry );
	}
}
