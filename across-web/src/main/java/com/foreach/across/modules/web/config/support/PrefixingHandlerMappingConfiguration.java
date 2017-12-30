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
package com.foreach.across.modules.web.config.support;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import org.springframework.aop.ClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.Collection;

/**
 * Base class for configuration of a {@link com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping}
 * instance that supports customizing its configuration through one or more
 * {@link com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurer} instances.
 * <p>Final configuration is done once after context bootstrap.</p>
 * <p>
 * Implementations should override {@link #controllerHandlerMapping()} method, and annotate it correctly for the
 * bean to be created.  Usually this means adding @Bean(name="BEAN_NAME") and @Exposed.
 * </p>
 *
 * @author Arne Vandamme
 */
public abstract class PrefixingHandlerMappingConfiguration
{
	/**
	 * Default order of the fallback handler mapping is 0.  Prefixing mappings should come before that,
	 * preferably in module order.  The offset is the number that is subtracted from the module index.
	 */
	public static final int DEFAULT_ORDER_OFFSET = 100;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AcrossModuleInfo currentModule;

	@RefreshableCollection(includeModuleInternals = true)
	private Collection<PrefixingHandlerMappingConfigurer> configurers;

	/**
	 * Override and annotate this method to create and expose the handler mapping correctly as a bean.
	 *
	 * @return HandlerMapping instance
	 */
	public PrefixingRequestMappingHandlerMapping controllerHandlerMapping() {
		PrefixingRequestMappingHandlerMapping handlerMapping =
				new PrefixingRequestMappingHandlerMapping( getPrefixPath(), getHandlerMatcher() );
		handlerMapping.setOrder( getHandlerMappingOrder() );

		return handlerMapping;
	}

	/**
	 * @return the prefix to apply to all mappings
	 */
	protected abstract String getPrefixPath();

	/**
	 * @return the filter used to scan for controllers to attach to this mapper
	 */
	protected abstract ClassFilter getHandlerMatcher();

	/**
	 * @return order for the handler mapping - defaults to module bootstrap index
	 */
	protected int getHandlerMappingOrder() {
		return currentModule.getIndex() - DEFAULT_ORDER_OFFSET;
	}

	/**
	 * You must override this method if you define more than one mapper in a single module.
	 *
	 * @return unique name of the mapper - defaults to module name
	 */
	protected String getHandlerMapperName() {
		return currentModule.getName();
	}

	@EventListener
	protected final void configureHandlerMapping( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
		InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
		PrefixingRequestMappingHandlerMapping mapping = controllerHandlerMapping();
		String handlerMapperName = getHandlerMapperName();

		for ( PrefixingHandlerMappingConfigurer configurer : configurers ) {
			if ( configurer.supports( handlerMapperName ) ) {
				configurer.addInterceptors( interceptorRegistry );
			}
		}

		mapping.setInterceptors( interceptorRegistry.getInterceptors().toArray() );
		mapping.reload();
	}
}
