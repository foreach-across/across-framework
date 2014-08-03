package com.foreach.across.core.context.registry;

import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;

public class DefaultAcrossContextBeanRegistry implements AcrossContextBeanRegistry
{
	private final ConfigurableAcrossContextInfo contextInfo;

	public DefaultAcrossContextBeanRegistry( ConfigurableAcrossContextInfo contextInfo ) {
		this.contextInfo = contextInfo;
	}

	@Override
	public Object getBeanFromModule( String moduleName, String beanName ) {
		return contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getBean( beanName );
	}
}
