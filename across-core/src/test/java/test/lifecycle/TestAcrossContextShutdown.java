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

package test.lifecycle;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.transformers.BeanPrefixingTransformer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Arne Vandamme
 */
public class TestAcrossContextShutdown
{
	@Test
	public void withoutParentSingleModuleApplicationContextDestroyed() {
		AcrossContext context = boot();
		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );

		assertEquals( "one", fetch( registry, "one" ) );
		assertEquals( "two", fetch( registry, "two" ) );

		destroy( context, "two" );

		assertEquals( "one", fetch( registry, "one" ) );
		assertNull( fetch( registry, "two" ) );

		destroy( context, "one" );
		assertNull( fetch( registry, "one" ) );
	}

	@Test
	public void withParentSingleModuleApplicationContextDestroyed() {
		AcrossApplicationContext parent = new AcrossApplicationContext();
		parent.refresh();

		AcrossContext context = boot( parent );

		assertEquals( "one", fetch( parent, "one" ) );
		assertEquals( "two", fetch( parent, "two" ) );

		destroy( context, "two" );

		assertEquals( "one", fetch( parent, "one" ) );
		assertNull( fetch( parent, "two" ) );

		destroy( context, "one" );
		assertNull( fetch( parent, "one" ) );
	}

	@Test
	public void withoutParentRootApplicationContextDestroyed() {
		AcrossContext context = boot();
		AcrossContextBeanRegistry registry = AcrossContextUtils.getBeanRegistry( context );

		assertEquals( "one", fetch( registry, "one" ) );
		assertEquals( "two", fetch( registry, "two" ) );

		context.shutdown();

		assertNull( fetch( registry, "two" ) );
		assertNull( fetch( registry, "one" ) );
	}

	@Test
	public void withParentRootApplicationContextDestroyed() {
		AcrossApplicationContext parent = new AcrossApplicationContext();
		parent.refresh();

		AcrossContext context = boot( parent );

		assertEquals( "one", fetch( parent, "one" ) );
		assertEquals( "two", fetch( parent, "two" ) );

		context.shutdown();

		assertNull( fetch( parent, "two" ) );
		assertNull( fetch( parent, "one" ) );
	}

	private String fetch( ApplicationContext context, String moduleName ) {
		try {
			return context.getBean( moduleName + "ExposedBean" ).toString();
		}
		catch ( IllegalStateException ise ) {
			return null;
		}
	}

	private String fetch( AcrossContextBeanRegistry registry, String moduleName ) {
		try {
			return registry.getBean( moduleName + "ExposedBean" ).toString();
		}
		catch ( IllegalStateException ise ) {
			return null;
		}
	}

	private void destroy( AcrossContext context, String moduleName ) {
		AcrossContextInfo contextInfo = AcrossContextUtils.getContextInfo( context );
		( (AbstractApplicationContext) contextInfo.getModuleInfo( moduleName ).getApplicationContext() ).destroy();
	}

	private AcrossContext boot() {
		return boot( null );
	}

	private AcrossContext boot( ApplicationContext parent ) {
		AcrossContext context = new AcrossContext();

		if ( parent != null ) {
			context.setParentApplicationContext( parent );
		}

		context.addModule( new TestModule( "one" ) );
		context.addModule( new TestModule( "two" ) );

		context.bootstrap();

		return context;
	}

	protected static class TestModule extends AcrossModule
	{
		private final String name;

		public TestModule( String name ) {
			this.name = name;
			setExposeTransformer( new BeanPrefixingTransformer( name ) );
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new AnnotatedClassConfigurer( ModuleConfig.class ) );
		}
	}

	@Configuration
	protected static class ModuleConfig
	{
		@Bean
		@Exposed
		public BeanFromModule exposedBean() {
			return new BeanFromModule();
		}
	}

	protected static class BeanFromModule
	{
		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModuleInfo currentModule;

		@Override
		public String toString() {
			return currentModule.getName();
		}
	}
}
