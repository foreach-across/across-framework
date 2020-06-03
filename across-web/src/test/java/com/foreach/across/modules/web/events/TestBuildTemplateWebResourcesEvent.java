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
package com.foreach.across.modules.web.events;

import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@ExtendWith(MockitoExtension.class)
public class TestBuildTemplateWebResourcesEvent
{
	@Mock
	private WebResourceRegistry webResourceRegistry;

	private BuildTemplateWebResourcesEvent event;

	@BeforeEach
	public void before() {
		event = new BuildTemplateWebResourcesEvent( "template", webResourceRegistry );
	}

	@Test
	public void getters() {
		assertThat( event.getTemplateName() ).isEqualTo( "template" );
		assertThat( event.getWebResourceRegistry() ).isSameAs( webResourceRegistry );
	}

	@Test
	public void applyResourceRules() {
		WebResourceRule one = mock( WebResourceRule.class );
		WebResourceRule two = mock( WebResourceRule.class );

		event.applyResourceRules( one, two );
		verify( webResourceRegistry ).apply( one, two );

		event.applyResourceRules( Arrays.asList( two, one ) );
		verify( webResourceRegistry ).apply( Arrays.asList( two, one ) );
	}
}
