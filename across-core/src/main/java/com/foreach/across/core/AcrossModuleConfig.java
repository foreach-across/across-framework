package com.foreach.across.core;

import com.foreach.across.core.context.bootstrap.AcrossBootstrapContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the current module in the current applicationcontext using the default qualifier.
 */
@Configuration("across.defaultModuleConfig")
public class AcrossModuleConfig
{
	@Bean(name = AcrossModule.CURRENT_MODULE_QUALIFIER)
	public AcrossModule currentModule( AcrossBootstrapContext bootstrapContext ) {
		return bootstrapContext.getCurrentModule();
	}
}
