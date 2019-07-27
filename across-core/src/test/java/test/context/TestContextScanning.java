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

package test.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.registry.IncrementalRefreshableRegistry;
import com.foreach.across.core.registry.RefreshableRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestContextScanning
{
	private AcrossContext context;

	@BeforeEach
	public void setUp() {
		context = new AcrossContext();
	}

	@AfterEach
	public void tearDown() {
		context.shutdown();
	}

	@Test
	public void beansShouldBeReturnedInTheRegisteredOrderOfTheModules() {
		context.addModule( new ModuleOne() );
		context.addModule( new ModuleTwo() );
		context.addModule( new ModuleThree() );
		context.bootstrap();

		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );
		List<MyBeanConfig> beans = registry.getBeansOfType( MyBeanConfig.class );
		assertTrue( beans.isEmpty() );

		beans = registry.getBeansOfType( MyBeanConfig.class, true );
		assertEquals( 3, beans.size() );

		assertEquals( "ModuleOne", beans.get( 0 ).getModule() );
		assertEquals( "ModuleTwo", beans.get( 1 ).getModule() );
		assertEquals( "ModuleThree", beans.get( 2 ).getModule() );

		Map<String, MyBeanConfig> beansWithName = registry.getBeansOfTypeAsMap( MyBeanConfig.class );
		assertTrue( beansWithName.isEmpty() );

		beansWithName = registry.getBeansOfTypeAsMap( MyBeanConfig.class, true );
		assertEquals( 3, beansWithName.size() );

		assertEquals( "ModuleOne", beansWithName.get( "ModuleOne:test.context.TestContextScanning.MyBeanConfig" ).getModule() );
		assertEquals( "ModuleTwo", beansWithName.get( "ModuleTwo:test.context.TestContextScanning.MyBeanConfig" ).getModule() );
		assertEquals( "ModuleThree", beansWithName.get( "ModuleThree:test.context.TestContextScanning.MyBeanConfig" ).getModule() );
	}

	@Test
	public void internalGenericBeanResolving() {
		context.addModule( new ModuleOne() );
		context.addModule( new ModuleTwo() );
		context.addModule( new ModuleThree() );
		context.bootstrap();

		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );
		List<GenericBean> beans = registry.getBeansOfType( GenericBean.class, true );
		Map<String, GenericBean> beansWithName = registry.getBeansOfTypeAsMap( GenericBean.class, true );

		assertEquals( 6, beans.size() );
		assertEquals( 6, beansWithName.size() );

		ResolvableType listType = ResolvableType.forClassWithGenerics( List.class, Integer.class );
		ResolvableType type = ResolvableType.forClassWithGenerics( GenericBean.class,
		                                                           ResolvableType.forClass( Long.class ),
		                                                           listType
		);

		beans = registry.getBeansOfType( type, true );
		beansWithName = registry.getBeansOfTypeAsMap( type, true );

		assertEquals( 3, beans.size() );
		assertEquals( "longWithIntegerList", beans.get( 0 ).getName() );
		assertEquals( "longWithIntegerList", beans.get( 1 ).getName() );
		assertEquals( "longWithIntegerList", beans.get( 2 ).getName() );

		assertEquals( 3, beansWithName.size() );
		assertEquals( "longWithIntegerList", beansWithName.get( "ModuleOne:longWithIntegerList" ).getName() );
		assertEquals( "longWithIntegerList", beansWithName.get( "ModuleTwo:longWithIntegerList" ).getName() );
		assertEquals( "longWithIntegerList", beansWithName.get( "ModuleThree:longWithIntegerList" ).getName() );

		listType = ResolvableType.forClassWithGenerics( List.class, Date.class );
		type = ResolvableType.forClassWithGenerics( GenericBean.class,
		                                            ResolvableType.forClass( String.class ),
		                                            listType
		);

		beans = registry.getBeansOfType( type, true );
		beansWithName = registry.getBeansOfTypeAsMap( type, true );

		assertEquals( 3, beans.size() );
		assertEquals( "stringWithDateList", beans.get( 0 ).getName() );
		assertEquals( "stringWithDateList", beans.get( 1 ).getName() );
		assertEquals( "stringWithDateList", beans.get( 2 ).getName() );

		assertEquals( 3, beansWithName.size() );
		assertEquals( "stringWithDateList", beansWithName.get( "ModuleOne:stringWithDateList" ).getName() );
		assertEquals( "stringWithDateList", beansWithName.get( "ModuleTwo:stringWithDateList" ).getName() );
		assertEquals( "stringWithDateList", beansWithName.get( "ModuleThree:stringWithDateList" ).getName() );
	}

	@Test
	public void refreshableCollectionTesting() {
		context.addModule( new ModuleOne() );
		context.addModule( new ModuleTwo() );
		context.bootstrap();

		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );
		MyBeanConfig one = registry.getBeanOfTypeFromModule( "ModuleOne", MyBeanConfig.class );
		MyBeanConfig two = registry.getBeanOfTypeFromModule( "ModuleTwo", MyBeanConfig.class );

		Collection<GenericBean<Long, List<Integer>>> integersOne = one.getIntegerLists();
		assertNotNull( integersOne );
		assertTrue( integersOne.getClass().equals( RefreshableRegistry.class ) );
		assertEquals( 2, integersOne.size() );

		Collection<GenericBean<String, List<Date>>> datesOne = one.getDateLists();
		assertNotNull( datesOne );
		assertTrue( datesOne.getClass().equals( IncrementalRefreshableRegistry.class ) );
		assertTrue( datesOne.isEmpty() );

		Collection<GenericBean<Long, List<Integer>>> integersTwo = two.getIntegerLists();
		assertNotNull( integersTwo );
		assertTrue( integersTwo.getClass().equals( RefreshableRegistry.class ) );
		assertEquals( 2, integersTwo.size() );

		Collection<GenericBean<String, List<Date>>> datesTwo = two.getDateLists();
		assertNotNull( datesTwo );
		assertTrue( datesTwo.getClass().equals( IncrementalRefreshableRegistry.class ) );
		assertTrue( datesTwo.isEmpty() );
	}

	@Test
	public void refreshableTesting() {
		context.addModule( new ModuleOne() );
		context.addModule( new ModuleTwo() );
		context.bootstrap();

		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );
		MyBeanConfig configOne = registry.getBeanOfTypeFromModule( "ModuleOne", MyBeanConfig.class );
		BeanWithRefreshables one = registry.getBeanOfTypeFromModule( "ModuleOne", BeanWithRefreshables.class );
		BeanWithRefreshables two = registry.getBeanOfTypeFromModule( "ModuleOne", BeanWithRefreshables.class );
		BeanWithNormalAutowiredConstructor beanWithOneConstructor = registry.getBeanOfTypeFromModule( "ModuleOne",
		                                                                                              BeanWithNormalAutowiredConstructor.class );

		assertNotNull( one );
		assertNotNull( two );
		assertNotNull( beanWithOneConstructor );
		assertNotNull( beanWithOneConstructor.getConversionServiceSet() );

		assertNotNull( one.integerLists );
		assertNotNull( one.otherIntegerLists );
		assertNotNull( one.dateLists );
		assertNotNull( one.otherDateLists );
		assertNotSame( one.dateLists, one.otherDateLists );
		assertNotSame( configOne.getIntegerLists(), one.integerLists );
		assertNotSame( configOne.getDateLists(), one.dateLists );

		Collection<GenericBean<Long, List<Integer>>> integerLists = one.integerLists;
		assertNotNull( integerLists );
		assertTrue( integerLists.getClass().equals( RefreshableRegistry.class ) );
		assertEquals( 2, integerLists.size() );

		Collection<GenericBean<Long, List<Integer>>> otherIntegerLists = one.otherIntegerLists;
		assertNotNull( otherIntegerLists );
		assertTrue( otherIntegerLists.getClass().equals( IncrementalRefreshableRegistry.class ) );
		assertEquals( 2, otherIntegerLists.size() );

		Collection<GenericBean<String, List<Date>>> dateLists = two.dateLists;
		assertNotNull( dateLists );
		assertTrue( dateLists.getClass().equals( RefreshableRegistry.class ) );
		assertTrue( dateLists.isEmpty() );

		Iterable<GenericBean<String, List<Date>>> otherDateLists = two.otherDateLists;
		assertNotNull( otherDateLists );
		assertTrue( otherDateLists.getClass().equals( IncrementalRefreshableRegistry.class ) );
	}

	@Test
	public void beansShouldBeReturnedInTheBootstrapOrderOfModules() {
		ModuleOne moduleOne = new ModuleOne();
		moduleOne.addRuntimeDependency( "ModuleThree" );
		context.addModule( moduleOne );

		ModuleTwo moduleTwo = new ModuleTwo();
		moduleTwo.addRuntimeDependency( "ModuleOne" );
		context.addModule( moduleTwo );

		ModuleThree moduleThree = new ModuleThree();
		context.addModule( moduleThree );

		context.bootstrap();

		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );
		List<MyBeanConfig> beans = registry.getBeansOfType( MyBeanConfig.class, true );
		Map<String, MyBeanConfig> beansWithName = registry.getBeansOfTypeAsMap( MyBeanConfig.class, true );
		assertEquals( 3, beans.size() );
		assertEquals( 3, beansWithName.size() );

		assertEquals( "ModuleThree", beans.get( 0 ).getModule() );
		assertEquals( "ModuleOne", beans.get( 1 ).getModule() );
		assertEquals( "ModuleTwo", beans.get( 2 ).getModule() );

		beansWithName.forEach( ( beanName, bean ) -> assertTrue( beanName.startsWith( bean.getModule() + ":" ) ) );
	}

	@Test
	public void beansFromTheParentContextArePositionedBeforeTheModuleBeans() {
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.getBeanFactory().registerSingleton( "fixed-config", new MyFixedBeanConfig() );
		applicationContext.refresh();

		context = new AcrossContext( applicationContext );

		ModuleOne moduleOne = new ModuleOne();
		moduleOne.addRuntimeDependency( "ModuleTwo" );
		context.addModule( moduleOne );

		ModuleTwo moduleTwo = new ModuleTwo();
		moduleTwo.addRuntimeDependency( "ModuleThree" );
		context.addModule( moduleTwo );

		ModuleThree moduleThree = new ModuleThree();
		context.addModule( moduleThree );

		context.bootstrap();

		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );
		List<MyBeanConfig> beans = registry.getBeansOfType( MyBeanConfig.class, true );
		Map<String, MyBeanConfig> beansWithName = registry.getBeansOfTypeAsMap( MyBeanConfig.class, true );
		assertEquals( 4, beans.size() );
		assertEquals( 4, beansWithName.size() );

		assertEquals( "ApplicationContext", beans.get( 0 ).getModule() );
		assertEquals( "ModuleThree", beans.get( 1 ).getModule() );
		assertEquals( "ModuleTwo", beans.get( 2 ).getModule() );
		assertEquals( "ModuleOne", beans.get( 3 ).getModule() );

		assertEquals( "ApplicationContext", beansWithName.get( "fixed-config" ).getModule() );
		assertEquals( "ModuleThree", beansWithName.get( "ModuleThree:test.context.TestContextScanning.MyBeanConfig" ).getModule() );
		assertEquals( "ModuleTwo", beansWithName.get( "ModuleTwo:test.context.TestContextScanning.MyBeanConfig" ).getModule() );
		assertEquals( "ModuleOne", beansWithName.get( "ModuleOne:test.context.TestContextScanning.MyBeanConfig" ).getModule() );

		List<String> namesInOrder = new ArrayList<>();

		for ( Map.Entry<String, MyBeanConfig> entry : beansWithName.entrySet() ) {
			namesInOrder.add( entry.getKey() );
		}

		assertEquals( Arrays.asList( "fixed-config",
		                             "ModuleThree:test.context.TestContextScanning.MyBeanConfig",
		                             "ModuleTwo:test.context.TestContextScanning.MyBeanConfig",
		                             "ModuleOne:test.context.TestContextScanning.MyBeanConfig" ),
		              namesInOrder
		);

		context.shutdown();
		applicationContext.stop();
	}

	@Configuration
	static class MyBeanConfig
	{
		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModule module;

		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModuleInfo moduleInfo;

		@RefreshableCollection(includeModuleInternals = true)
		private Collection<GenericBean<Long, List<Integer>>> integerLists;

		@RefreshableCollection(incremental = true)
		private Collection<GenericBean<String, List<Date>>> dateLists;

		public String getModule() {
			assertSame( module, moduleInfo.getModule() );
			return module.getName();
		}

		Collection<GenericBean<Long, List<Integer>>> getIntegerLists() {
			return integerLists;
		}

		Collection<GenericBean<String, List<Date>>> getDateLists() {
			return dateLists;
		}

		@Bean
		public GenericBean<Long, List<Integer>> longWithIntegerList() {
			return new GenericBean<>( "longWithIntegerList" );
		}

		@Bean
		public GenericBean<String, List<Date>> stringWithDateList() {
			return new GenericBean<>( "stringWithDateList" );
		}
	}

	static class GenericBean<T, Y>
	{
		private final String name;

		GenericBean( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	@Component
	public static class BeanWithNormalAutowiredConstructor
	{
		private Set<ConversionService> conversionServiceSet;

		@Autowired
		public BeanWithNormalAutowiredConstructor( Set<ConversionService> conversionServiceSet ) {
			this.conversionServiceSet = conversionServiceSet;
		}

		public Set<ConversionService> getConversionServiceSet() {
			return conversionServiceSet;
		}
	}

	@Component
	static class BeanWithRefreshables
	{
		final Collection<GenericBean<Long, List<Integer>>> integerLists, otherIntegerLists;
		Collection<GenericBean<String, List<Date>>> dateLists, otherDateLists;

		@Autowired
		public BeanWithRefreshables(
				@RefreshableCollection(includeModuleInternals = true) Collection<GenericBean<Long, List<Integer>>> integerLists,
				@RefreshableCollection(incremental = true, includeModuleInternals = true) Collection<GenericBean<Long, List<Integer>>> otherIntegerLists ) {
			this.integerLists = integerLists;
			this.otherIntegerLists = otherIntegerLists;
		}

		@Autowired
		public void autowireDateLists( @RefreshableCollection Collection<GenericBean<String, List<Date>>> dateLists ) {
			this.dateLists = dateLists;
		}

		@Autowired
		@RefreshableCollection(incremental = true)
		public void setOtherDateLists( Collection<GenericBean<String, List<Date>>> otherDateLists ) {
			this.otherDateLists = otherDateLists;
		}
	}

	static class MyFixedBeanConfig extends MyBeanConfig
	{
		@Override
		public String getModule() {
			return "ApplicationContext";
		}
	}

	static class ModuleOne extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleOne";
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new AnnotatedClassConfigurer( MyBeanConfig.class, BeanWithRefreshables.class,
			                                                      BeanWithNormalAutowiredConstructor.class ) );
		}
	}

	static class ModuleTwo extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleTwo";
		}
	}

	static class ModuleThree extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleThree";
		}
	}
}
