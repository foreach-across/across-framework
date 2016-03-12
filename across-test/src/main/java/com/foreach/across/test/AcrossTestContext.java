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
import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.registry.DefaultAcrossContextBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Closeable;

/**
 * This class is a wrapper around a bootstrapped {@link AcrossContext} and provides methods
 * for easy querying of said context and its modules.  This class also implements {@link Closeable}.
 * Calling {@link #close()} will close both the {@link AcrossContext} and (if provided) the additional
 * parent {@link org.springframework.context.ApplicationContext}.
 * <p>
 * Instances of this class should not be created manually but through one of the builders.
 * See {@link com.foreach.across.test.support.AcrossTestBuilders}.
 * Public constructors will be removes in a future release.
 * </p>
 *
 * @author Arne Vandamme
 * @see com.foreach.across.test.AcrossTestWebContext
 * @see com.foreach.across.test.AcrossTestConfiguration
 * @see com.foreach.across.test.support.AcrossTestBuilders
 * @see com.foreach.across.test.support.AcrossTestContextBuilder
 */
public class AcrossTestContext extends DefaultAcrossContextBeanRegistry implements Closeable
{
	private ConfigurableApplicationContext applicationContext;
	private AcrossContext acrossContext;
	private AcrossContextBeanRegistry beanRegistry;
	private AcrossContextInfo contextInfo;

	protected AcrossTestContext() {
	}

	/**
	 * @param configurers list of configures
	 * @deprecated use {@link com.foreach.across.test.support.AcrossTestBuilders} instead
	 */
	@Deprecated
	public AcrossTestContext( AcrossContextConfigurer... configurers ) {
		AcrossConfigurableApplicationContext parent = createApplicationContext();

		ProvidedBeansMap providedBeans = new ProvidedBeansMap();

		for ( int i = 0; i < configurers.length; i++ ) {
			providedBeans.put(
					"QueryableAcrossTestContext.AcrossContextConfigurer~" + i,
					new SingletonBean( configurers[i] )
			);
		}

		parent.provide( providedBeans );

		parent.refresh();
		parent.start();

		setApplicationContext( parent );
		setAcrossContext( parent.getBean( AcrossContext.class ) );
	}

	protected void setApplicationContext( ConfigurableApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
	}

	protected void setAcrossContext( AcrossContext acrossContext ) {
		this.acrossContext = acrossContext;

		beanRegistry = AcrossContextUtils.getBeanRegistry( acrossContext );
		contextInfo = AcrossContextUtils.getContextInfo( acrossContext );
		setContextInfo( (ConfigurableAcrossContextInfo) contextInfo );
	}

	protected AcrossConfigurableApplicationContext createApplicationContext() {
		AcrossApplicationContext ctx = new AcrossApplicationContext();
		ctx.register( AcrossTestContextConfiguration.class );

		return ctx;
	}

	@Override
	public void close() {
		acrossContext.shutdown();
		if ( applicationContext != null ) {
			applicationContext.stop();
		}
	}

	/**
	 * Provides access to registry for the {@link AcrossContext}.
	 *
	 * @return bean registry
	 * @deprecated no longer useful as {@link AcrossTestContext} implements the bean registry interface directly
	 */
	@Deprecated
	public AcrossContextBeanRegistry beanRegistry() {
		return beanRegistry;
	}

	/**
	 * @return the bootstrapped Across context info
	 */
	public AcrossContextInfo contextInfo() {
		return contextInfo;
	}
}
