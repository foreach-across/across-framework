package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.adminweb.controllers.AdminWebController;
import com.foreach.across.modules.adminweb.controllers.RoleController;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AcrossDepends(required = "AcrossWebModule")
public class AdminWebConfig
{
	@Autowired
	private AdminWebModule adminWebModule;

	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping adminRequestMappingHandlerMapping() {
		PrefixingRequestMappingHandlerMapping mappingHandlerMapping =
				new PrefixingRequestMappingHandlerMapping( adminWebModule.getRootPath(),
				                                           new AnnotationClassFilter( AdminWebController.class ) );

		return mappingHandlerMapping;
	}

	@Bean
	public RoleController roleController() {
		return new RoleController();
	}
}
