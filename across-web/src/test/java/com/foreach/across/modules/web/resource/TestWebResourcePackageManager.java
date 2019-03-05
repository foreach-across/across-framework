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
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebResourcePackageManager
{
	@Mock
	private WebResourcePackage resourcePackage;

	private WebResourcePackageManager packageManager = new WebResourcePackageManager();

	@Test
	public void extendPackageReturnsFalseIfPackageNotPresent() {
		assertThat( packageManager.extendPackage( "MYPACKAGE", resourcePackage ) ).isFalse();
	}

	@Test
	public void extendPackageAppendsExtension() {
		WebResourcePackage extension = mock( WebResourcePackage.class );
		WebResourceRegistry registry = mock( WebResourceRegistry.class );

		packageManager.register( "MYPACKAGE", resourcePackage );
		assertThat( packageManager.extendPackage( "MYPACKAGE", extension ) ).isTrue();

		packageManager.getPackage( "MYPACKAGE" ).install( registry );

		InOrder inOrder = Mockito.inOrder( resourcePackage, extension );
		inOrder.verify( resourcePackage ).install( registry );
		inOrder.verify( extension ).install( registry );
	}
}
