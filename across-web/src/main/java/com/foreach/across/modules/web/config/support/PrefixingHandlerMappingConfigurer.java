package com.foreach.across.modules.web.config.support;

import com.foreach.across.modules.web.mvc.InterceptorRegistry;

public interface PrefixingHandlerMappingConfigurer
{
	boolean supports( String mapperName );

	void addInterceptors( InterceptorRegistry interceptorRegistry );
}
