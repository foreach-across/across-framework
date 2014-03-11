package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.adminweb.controllers.AdminWebController;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AcrossDepends(required = "AcrossWebModule")
public class AdminWebConfig
{
	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping adminRequestMappingHandlerMapping() {
		PrefixingRequestMappingHandlerMapping mappingHandlerMapping =
				new PrefixingRequestMappingHandlerMapping( "/admin",
				                                           new AnnotationClassFilter( AdminWebController.class ) );

		return mappingHandlerMapping;
	}
}
