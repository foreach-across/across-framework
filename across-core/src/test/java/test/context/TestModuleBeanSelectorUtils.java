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
package test.context;

import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.support.ModuleBeanSelectorUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.junit.Assert.*;

public class TestModuleBeanSelectorUtils
{
	private AnnotationConfigApplicationContext context;

	@Before
	public void initContext() {
		context = new AnnotationConfigApplicationContext();
	}

	@After
	public void stopContext() {
		if ( context.isActive() ) {
			context.close();
		}
	}

	@Test
	public void noBeanDefinitonInSingleApplicationContext() {
		context();

		assertNameHolderNotPresentInModule( RandomStringUtils.random( 10 ) );
	}

	@Test
	public void singleBeanDefinitonInSingleApplicationContext() {
		context( DefaultNameHolderConfig.class );

		assertValidMultipleFetches( "default" );
	}

	@Test
	public void multipleBeanDefinitonsWithQualifiersInSingleApplicationContext() {
		context( QualifiedNameHoldersConfig.class );

		assertValidMultipleFetches( "module1", "module1" );
		assertValidMultipleFetches( "module2", "module2" );
		;
		assertAndFetchBean( "module2", "module2" );

		assertNameHolderNotPresentInModule( RandomStringUtils.random( 10 ) );
	}

	@Test
	public void multipleBeanDefinitonsWithQualifiersAndDefaultBeanInSingleApplicationContext() {
		context( DefaultNameHolderConfig.class, QualifiedNameHoldersConfig.class );

		assertValidMultipleFetches( "default" );
		assertValidMultipleFetches( "module1", "module1" );
		assertValidMultipleFetches( "module2", "module2" );
	}

	@Test(expected = NoUniqueBeanDefinitionException.class)
	public void ambiguousDefaultBeanDefinitionsInSingleApplicationContext() {
		context( AmbiguousDefaultNameHoldersConfig.class );

		assertAndFetchBean( RandomStringUtils.random( 10 ), "default" );
	}

	@Test(expected = NoUniqueBeanDefinitionException.class)
	public void ambiguouslyQualifiedBeanDefinitionsInSingleApplicationContext() {
		context( AmbiguouslyQualifiedNameHoldersConfig.class );

		assertAndFetchBean( "module1", "module1" );
	}

	private void context( Class<?>... annotatedClasses ) {
		if ( annotatedClasses.length > 0 ) {
			context.register( annotatedClasses );
		}
		context.refresh();
		context.start();
	}

	public void assertValidMultipleFetches( String expectedName ) {
		assertAndFetchBean( RandomStringUtils.random( 10 ), expectedName );
	}

	public void assertValidMultipleFetches( String moduleName, String expectedName ) {
		NameHolder one = assertAndFetchBean( moduleName, expectedName );
		NameHolder two = assertAndFetchBean( moduleName, expectedName );
		assertSame( one, two );
	}

	public NameHolder assertAndFetchBean( String moduleName, String expectedName ) {
		Optional<NameHolder> nameHolder
				= ModuleBeanSelectorUtils.selectBeanForModule( NameHolder.class, moduleName, context.getBeanFactory() );

		assertTrue( nameHolder.isPresent() );
		assertEquals( expectedName, nameHolder.get().getName() );
		return nameHolder.get();
	}

	public void assertNameHolderNotPresentInModule( String moduleName ) {
		assertFalse(
				ModuleBeanSelectorUtils.selectBeanForModule( NameHolder.class, moduleName, context.getBeanFactory() )
				                       .isPresent()
		);
	}

	public static class NameHolder
	{
		private String name;

		public NameHolder( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public boolean nameEquals( String name ) {
			return StringUtils.equals( this.name, name );
		}

	}

	@Configuration
	protected static class DefaultNameHolderConfig
	{

		@Bean
		public NameHolder defaultNameHolder() {
			return new NameHolder( "default" );
		}

	}

	@Configuration
	protected static class QualifiedNameHoldersConfig
	{
		@Bean
		@Module("module1")
		public NameHolder moduleOneNameHolder() {
			return new NameHolder( "module1" );
		}

		@Bean
		@Module("module2")
		public NameHolder moduleTwoNameHolder() {
			return new NameHolder( "module2" );
		}
	}

	@Configuration
	protected static class AmbiguousDefaultNameHoldersConfig
	{
		@Bean
		public NameHolder moduleOneNameHolder() {
			return new NameHolder( "module1" );
		}

		@Bean
		public NameHolder moduleTwoNameHolder() {
			return new NameHolder( "module2" );
		}
	}

	@Configuration
	protected static class AmbiguouslyQualifiedNameHoldersConfig
	{
		@Bean
		@Module("module1")
		public NameHolder moduleOneNameHolder() {
			return new NameHolder( "module1" );
		}

		@Bean
		@Module("module1")
		public NameHolder moduleTwoNameHolder() {
			return new NameHolder( "module2" );
		}
	}
}
