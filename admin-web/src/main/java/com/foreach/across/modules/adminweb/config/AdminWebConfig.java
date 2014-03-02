package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.adminweb.controllers.AdminWebController;
import com.foreach.across.modules.web.mvc.ClassFilter;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

@Configuration
@AcrossDepends(required = "AcrossWebModule")
public class AdminWebConfig
{
	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping adminRequestMappingHandlerMapping() {
		PrefixingRequestMappingHandlerMapping mappingHandlerMapping =
				new PrefixingRequestMappingHandlerMapping( "/admin", new ClassFilter()
				{
					public boolean matches( Class<?> beanType ) {
						return AnnotationUtils.getAnnotation( beanType, AdminWebController.class ) != null;
					}
				} );

		return mappingHandlerMapping;
	}
}
