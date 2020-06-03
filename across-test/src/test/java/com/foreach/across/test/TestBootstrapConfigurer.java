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
package com.foreach.across.test;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static com.foreach.across.test.support.AcrossTestBuilders.standard;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestBootstrapConfigurer
{
	@Test
	public void testDataSourceIsClosed() {
		HikariDataSource dataSource;
		try (AcrossTestContext ctx = standard().build()) {
			dataSource = ctx.getBeanOfType( HikariDataSource.class );
		}
		assertTrue( dataSource.isClosed() );
	}

	@Test
	public void customizedBootstrap() {
		try (AcrossTestContext ctx = standard()
				.register( BootstrapConfigurer.class )
				.modules( new EmptyAcrossModule( "one" ), new EmptyAcrossModule( "two" ) )
				.build()) {
			assertEquals( "moduleBean", ctx.getBeanFromModule( "one", "moduleBean" ) );
			assertEquals( "contextBean", ctx.getBeanFromModule( "two", "contextBean" ) );

			assertEquals( "moduleBean", ctx.getBean( "moduleBean" ) );
			assertFalse( ctx.containsBean( "contextBean" ) );
		}
	}

	@Test
	public void configurationsExcluded() {
		try (AcrossTestContext ctx = standard()
				.register( BootstrapConfigurer.class, ExcludedConfigurationConfigurer.class )
				.modules( new EmptyAcrossModule( "one" ), new EmptyAcrossModule( "two" ) )
				.build()) {

			assertFalse( ctx.containsBean( "moduleBean" ) );
			assertFalse( ctx.moduleContainsLocalBean( "one", "moduleBean" ) );

			assertFalse( ctx.containsBean( "contextBean" ) );
			assertFalse( ctx.moduleContainsLocalBean( "two", "contextBean" ) );
		}
	}

	@Component
	static class BootstrapConfigurer implements AcrossBootstrapConfigurer
	{
		@Override
		public void configureContext( AcrossBootstrapConfig contextConfiguration ) {
			contextConfiguration.extendModule( "two", ContextConfiguration.class );
		}

		@Override
		public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
			if ( "one".equals( moduleConfiguration.getModuleName() ) ) {
				moduleConfiguration.expose( "moduleBean" );
				moduleConfiguration.addApplicationContextConfigurer( ModuleConfiguration.class );
			}
		}
	}

	@Component
	static class ExcludedConfigurationConfigurer implements AcrossBootstrapConfigurer
	{
		@Override
		public void configureContext( AcrossBootstrapConfig contextConfiguration ) {
			contextConfiguration.excludeFromModule( "one", ModuleConfiguration.class.getName() );
		}

		@Override
		public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
			if ( "two".equals( moduleConfiguration.getModuleName() ) ) {
				moduleConfiguration.exclude( ContextConfiguration.class );
			}
		}
	}

	@Configuration
	static class ContextConfiguration
	{
		@Bean
		public String contextBean() {
			return "contextBean";
		}
	}

	@Configuration
	static class ModuleConfiguration
	{
		@Bean
		public String moduleBean() {
			return "moduleBean";
		}
	}
}
