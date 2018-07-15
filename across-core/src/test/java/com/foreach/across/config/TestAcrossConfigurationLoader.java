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
package com.foreach.across.config;

import com.foreach.across.config.AcrossConfiguration.AutoConfigurationClass;
import com.foreach.across.config.AcrossConfiguration.IllegalConfiguration.ClassEntry;
import lombok.val;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAcrossConfigurationLoader
{
	@Test
	public void allConfigurationFilesAreProcessed() {
		AcrossConfiguration configuration = AcrossConfiguration.get( Thread.currentThread().getContextClassLoader() );
		assertThat( configuration ).isNotNull();
		assertThat( configuration ).isSameAs( AcrossConfiguration.get( Thread.currentThread().getContextClassLoader() ) );

		assertThat( configuration.getGroups() ).hasSize( 2 );
		assertThat( configuration.getGroups().stream().map( AcrossConfiguration.Group::getName ) ).containsExactly( "test", "across.core" );

		assertThat( configuration.getGroup( "bad" ) ).isNull();

		val core = configuration.getGroup( "across.core" );
		assertThat( core ).isNotNull();
		assertThat( core.getPriority() ).isEqualTo( 0 );

		val test = configuration.getGroup( "test" );
		assertThat( test ).isNotNull();
		assertThat( test.getPriority() ).isEqualTo( 1000 );

		assertThat( test.getExposeRules() ).containsExactlyInAnyOrder( "my.class", "your.class" );
		assertThat( core.getExposeRules() ).isNotEmpty();
		assertThat( configuration.getExposeRules() )
				.contains( "my.class", "your.class", "com.foreach.across.core.annotations.Exposed", "javax.servlet.Filter" );

		val classes = test.getAutoConfigurationClasses();
		assertThat( classes )
				.hasSize( 8 )
				.contains( new AutoConfigurationClass( "enabled.class", true, null, null ) )
				.contains( new AutoConfigurationClass( "disabled.class", false, null, null ) )
				.contains( new AutoConfigurationClass( "other.enabled.class", true, null, null ) )
				.contains( new AutoConfigurationClass( "other.disabled.class", false, null, null ) )
				.contains( new AutoConfigurationClass( "adapted.class", true, null, "my.class" ) )
				.contains( new AutoConfigurationClass( "moved.class", true, "AcrossContextPostProcessorModule", null ) )
				.contains( new AutoConfigurationClass( "adapted.and.moved.class", true, "SomewhereModule", "my.class" ) )
				.contains( new AutoConfigurationClass( "org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration", true, "SomeModule",
				                                       "my.class" ) );

		assertThat( core.getAutoConfigurationClasses() )
				.contains( new AutoConfigurationClass( "org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration", false, null, null ) );

		assertThat( configuration.getAutoConfigurationClasses() )
				.contains( new AutoConfigurationClass( "org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration", true, "SomeModule",
				                                       "my.class" ) );

		assertThat( core.getIllegalConfigurations() ).isEmpty();

		assertThat( test.getIllegalConfigurations() )
				.containsExactly( new AcrossConfiguration.IllegalConfiguration(
						"spring-data",
						"Cause of this error.",
						"What you should do.",
						Arrays.asList(
								new ClassEntry( "my.class", "AcrossContext,AcrossContextPostProcessorModule", "AcrossModule,OtherModule" ),
								new ClassEntry( "other.class", null, "AcrossContext" )
						)
				) );

	}
}
