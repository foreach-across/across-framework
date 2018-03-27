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

import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestWebResourceRegistry
{
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
