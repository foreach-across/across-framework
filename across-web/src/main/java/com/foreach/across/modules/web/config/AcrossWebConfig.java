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

package com.foreach.across.modules.web.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.registry.RefreshableRegistry;
import com.foreach.across.modules.web.config.resources.ResourceConfigurationProperties;
import com.foreach.across.modules.web.context.AcrossWebArgumentResolver;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.menu.MenuBuilder;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.menu.MenuStore;
import com.foreach.across.modules.web.mvc.WebAppPathResolverExposingInterceptor;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import com.foreach.across.modules.web.resource.WebResourceTranslator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@Exposed
@OrderInModule(1)
@Deprecated
public class AcrossWebConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	private PrefixingPathRegistry prefixingPathRegistry;

	@Autowired
	private ResourceConfigurationProperties resourceConfigurationProperties;

	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( new WebAppPathResolverExposingInterceptor( prefixingPathRegistry ) );
		registry.addInterceptor( webResourceRegistryInterceptor() );
	}

	@Override
	public void addArgumentResolvers( List<HandlerMethodArgumentResolver> argumentResolvers ) {
		argumentResolvers.add( acrossWebArgumentResolver() );
	}

	@Bean
	public AcrossWebArgumentResolver acrossWebArgumentResolver() {
		return new AcrossWebArgumentResolver();
	}

	@Bean
	public WebResourcePackageManager webResourcePackageManager() {
		return new WebResourcePackageManager();
	}

	@Bean
	public WebResourceRegistryInterceptor webResourceRegistryInterceptor() {
		WebResourceRegistryInterceptor interceptor = new WebResourceRegistryInterceptor( webResourcePackageManager() );
		interceptor.setWebResourceTranslators( webResourceTranslatorRegistry() );

		return interceptor;
	}

	@Bean
	protected RefreshableRegistry<WebResourceTranslator> webResourceTranslatorRegistry() {
		return new RefreshableRegistry<>( WebResourceTranslator.class, true );
	}

	@Bean
	public MenuFactory menuFactory( MenuBuilder requestMenuBuilder, MenuStore requestMenuStore ) {
		MenuFactory menuFactory = new MenuFactory();
		menuFactory.setDefaultMenuBuilder( requestMenuBuilder );
		menuFactory.setDefaultMenuStore( requestMenuStore );

		return menuFactory;
	}

	@Bean
	public WebResourceTranslator viewsWebResourceTranslator() {
		if ( resourceConfigurationProperties.getPath() != null ) {
			return new WebResourceTranslator()
			{
				public boolean shouldTranslate( WebResource resource ) {
					return StringUtils.equals( WebResource.VIEWS, resource.getLocation() );
				}

				public void translate( WebResource resource ) {
					resource.setLocation( WebResource.RELATIVE );
					resource.setData( resourceConfigurationProperties.getPath() + resource.getData() );
				}
			};
		}
		else {
			return null;
		}
	}
}
