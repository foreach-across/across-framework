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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.reset;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@ExtendWith(MockitoExtension.class)
public class TestWebResourcePackage
{
	@Mock
	private WebResourceRegistry registry;

	@Mock
	private WebResourceRule ruleOne;

	@Mock
	private WebResourceRule ruleTwo;

	@Test
	public void packageOfRules() {
		WebResourcePackage.of( ruleOne, ruleTwo ).install( registry );

		InOrder inOrder = Mockito.inOrder( ruleOne, ruleTwo );
		inOrder.verify( ruleOne ).applyTo( registry );
		inOrder.verify( ruleTwo ).applyTo( registry );

		reset( ruleOne, ruleTwo );

		WebResourcePackage.of( Arrays.asList( ruleTwo, ruleOne ) ).install( registry );
		inOrder.verify( ruleTwo ).applyTo( registry );
		inOrder.verify( ruleOne ).applyTo( registry );
	}

	@Test
	public void combine() {
		WebResourcePackage original = WebResourcePackage.of( ruleOne );
		WebResourcePackage extension = WebResourcePackage.of( ruleTwo );

		InOrder inOrder = Mockito.inOrder( ruleOne, ruleTwo );
		WebResourcePackage.combine( original, extension ).install( registry );
		inOrder.verify( ruleOne ).applyTo( registry );
		inOrder.verify( ruleTwo ).applyTo( registry );

		reset( ruleOne, ruleTwo );
		WebResourcePackage.combine( extension, original ).install( registry );
		inOrder.verify( ruleTwo ).applyTo( registry );
		inOrder.verify( ruleOne ).applyTo( registry );
	}
}
