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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.beans.SingletonBean;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;

import java.io.Closeable;

/**
 * Creates and bootstraps a new AcrossContext instance using the configurers passed in
 * as constructor parameters.  The default test datasource is used and no default modules
 * are added to the context.
 * <p>
 * The test context provides methods for easy querying of an AcrossContext and its modules.
 * <p>
 * <strong>Note:</strong> when finished with an AcrossTestContext it is important that
 * the {@link #close()} method is called.  To make this easier this class implements
 * {@link java.io.Closeable}.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.test.AcrossTestWebContext
 * @see com.foreach.across.test.AcrossTestConfiguration
 */
public class AcrossTestContext implements Closeable
{
	private final AcrossConfigurableApplicationContext applicationContext;
	private final AcrossContext acrossContext;
	private final AcrossContextBeanRegistry beanRegistry;
	private final AcrossContextInfo contextInfo;

	public AcrossTestContext( AcrossContextConfigurer... configurers ) {
		applicationContext = createApplicationContext();

		ProvidedBeansMap providedBeans = new ProvidedBeansMap();

		for ( int i = 0; i < configurers.length; i++ ) {
			providedBeans.put(
					"QueryableAcrossTestContext.AcrossContextConfigurer~" + i,
					new SingletonBean( configurers[i] )
			);
		}

		applicationContext.provide( providedBeans );

		applicationContext.refresh();
		applicationContext.start();

		acrossContext = applicationContext.getBean( AcrossContext.class );
		beanRegistry = AcrossContextUtils.getBeanRegistry( acrossContext );
		contextInfo = AcrossContextUtils.getContextInfo( acrossContext );
	}

	protected AcrossConfigurableApplicationContext createApplicationContext() {
		AcrossApplicationContext ctx = new AcrossApplicationContext();
		ctx.register( AcrossTestContextConfiguration.class );

		return ctx;
	}

	@Override
	public void close() {
		acrossContext.shutdown();
		applicationContext.stop();
	}

	public AcrossContextBeanRegistry beanRegistry() {
		return beanRegistry;
	}

	public AcrossContextInfo contextInfo() {
		return contextInfo;
	}
}
