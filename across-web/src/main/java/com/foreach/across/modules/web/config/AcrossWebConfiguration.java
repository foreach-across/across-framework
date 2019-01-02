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
package com.foreach.across.modules.web.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.registry.RefreshableRegistry;
import com.foreach.across.modules.web.config.resources.ResourceConfigurationProperties;
import com.foreach.across.modules.web.context.AcrossWebArgumentResolver;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.extensions.EnableWebMvcConfiguration;
import com.foreach.across.modules.web.menu.MenuBuilder;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.menu.MenuStore;
import com.foreach.across.modules.web.mvc.WebAppPathResolverExposingInterceptor;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import com.foreach.across.modules.web.resource.WebResourceTranslator;
import com.foreach.across.modules.web.support.MessageCodeSupportingLocalizedTextResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;

/**
 * Creates the Across Web infrastructure for the module itself.
 * Beans created here can usually be accessed directly in dependant modules, whereas
 * default Web MVC support will be activated in the post processor module, requiring
 * the use of {@code @Lazy} or {@code @PostRefresh} to access them.
 *
 * @author Arne Vandamme
 * @see EnableWebMvcConfiguration
 * @since 3.0.0
 */
@Configuration
@OrderInModule(1)
@Import({ JacksonAutoConfiguration.class, GsonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, RestTemplateAutoConfiguration.class })
class AcrossWebConfiguration implements WebMvcConfigurer
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

	/**
	 * Manually create and expose the handler mapping introspector early, so (f.i.) security modules can use it.
	 */
	@Bean
	@Lazy
	@Exposed
	@Primary
	public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
		return new HandlerMappingIntrospector();
	}

	@Bean
	@Exposed
	public AcrossWebArgumentResolver acrossWebArgumentResolver() {
		return new AcrossWebArgumentResolver();
	}

	@Bean
	@Exposed
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
	@Exposed
	public MenuFactory menuFactory( MenuBuilder requestMenuBuilder, MenuStore requestMenuStore ) {
		MenuFactory menuFactory = new MenuFactory();
		menuFactory.setDefaultMenuBuilder( requestMenuBuilder );
		menuFactory.setDefaultMenuStore( requestMenuStore );

		return menuFactory;
	}

	@Bean
	@Exposed
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

	@Bean
	@Exposed
	public MessageCodeSupportingLocalizedTextResolver localizedTextResolver() {
		return new MessageCodeSupportingLocalizedTextResolver();
	}
}
