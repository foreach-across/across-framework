package com.foreach.across.modules.debugweb.handlers;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.debugweb.mvc.DebugHandlerMapping;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Scans modules for all DebugWebController instances, even if they are not exposed.
 */
@AcrossEventHandler
public class DebugWebEventHandler
{
	@Autowired
	private DebugHandlerMapping mapping;

	@Handler
	public void registerDebugWebControllers( AcrossContextBootstrappedEvent event ) {
		for ( AcrossModuleInfo moduleInfo : event.getModules() ) {
			mapping.scanContext( AcrossContextUtils.getApplicationContext( moduleInfo.getModule() ) );
		}
	}
}
