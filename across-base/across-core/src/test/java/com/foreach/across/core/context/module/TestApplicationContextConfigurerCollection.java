/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.module;

import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class TestApplicationContextConfigurerCollection
{
	@Test
	void hasComponentsIfAtLeasOneConfigurerDoes( @Mock ApplicationContextConfigurer one, @Mock ApplicationContextConfigurer two ) {
		ApplicationContextConfigurerCollection configurers = new ApplicationContextConfigurerCollection( Arrays.asList( one, two ) );
		assertThat( configurers.hasComponents() ).isFalse();

		when( one.hasComponents() ).thenReturn( true );
		assertThat( configurers.hasComponents() ).isTrue();

		when( one.isOptional() ).thenReturn( true );
		assertThat( configurers.hasComponents() ).isFalse();

		when( two.hasComponents() ).thenReturn( true );
		assertThat( configurers.hasComponents() ).isTrue();
	}
}
