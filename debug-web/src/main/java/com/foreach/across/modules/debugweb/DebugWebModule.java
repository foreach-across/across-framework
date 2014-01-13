package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.annotations.AcrossEventHandler;
import net.engio.mbassy.listener.Handler;

@AcrossEventHandler
public class DebugWebModule extends AcrossModule
{
	private String rootPath = "/debug";

	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
	}

	public String getRootPath() {
		return rootPath;
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] { new TestInstaller() };
	}

	public String getName() {
		return "DebugWebModule";
	}

	@Override
	public String getDescription() {
		return "Provides a debug web path and functionality to easily register additional debug controllers.";
	}

	@Handler
	public void onApplicationEvent( AcrossContextBootstrappedEvent event ) {
		System.out.println( "finished bootstrapping " + event.getContext().getModules().size() + " modules" );
	}
}

