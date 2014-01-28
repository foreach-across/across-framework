package com.foreach.across.modules.web;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.context.AcrossWebArgumentResolver;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import com.foreach.across.modules.web.resource.WebResourceTranslator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Exposed
public class AcrossWebConfig
{
	@Autowired
	private AcrossWebModule acrossWebModule;

	@Bean
	public AcrossWebArgumentResolver acrossWebArgumentResolver() {
		return new AcrossWebArgumentResolver();
	}

	@Bean
	public WebResourceRegistryInterceptor webResourceRegistryInterceptor() {
		return new WebResourceRegistryInterceptor();
	}

	@Bean
	public WebResourceTranslator viewsWebResourceTranslator() {
		if ( acrossWebModule.getViewsResourcePath() != null ) {
			return new WebResourceTranslator()
			{
				public boolean shouldTranslate( WebResource resource ) {
					return StringUtils.equals( WebResource.VIEWS, resource.getLocation() );
				}

				public void translate( WebResource resource ) {
					resource.setLocation( WebResource.RELATIVE );
					resource.setData( acrossWebModule.getViewsResourcePath() + resource.getData() );
				}
			};
		}
		else {
			return null;
		}
	}
}
