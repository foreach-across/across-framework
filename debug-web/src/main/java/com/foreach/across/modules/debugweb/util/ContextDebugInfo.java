package com.foreach.across.modules.debugweb.util;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.LinkedList;

public class ContextDebugInfo
{
	private final String name;
	private final ApplicationContext applicationContext;
	private boolean enabled;

	public ContextDebugInfo( String name, ApplicationContext applicationContext ) {
		this.name = name;
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public Environment getEnvironment() {
		return applicationContext != null ? applicationContext.getEnvironment() : null;
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	/**
	 * Gathers all debug info for an entire AcrossContext.
	 */
	public static Collection<ContextDebugInfo> create( AcrossContextInfo context ) {

		LinkedList<ContextDebugInfo> list = new LinkedList<>();

		for ( AcrossModuleInfo moduleInfo : context.getModules() ) {
			list.add( createForModule( moduleInfo ) );
		}

		list.addFirst( createForContext( context ) );

		ApplicationContext parent = context.getConfiguration().getParentApplicationContext();

		while ( parent != null ) {
			list.addFirst( createForApplicationContext( parent ) );

			parent = parent.getParent();
		}

		return list;
	}

	private static ContextDebugInfo createForModule( AcrossModuleInfo moduleInfo ) {
		ContextDebugInfo debugInfo = new ContextDebugInfo( moduleInfo.getName(), moduleInfo.getApplicationContext() );
		debugInfo.setEnabled( moduleInfo.isEnabled() );

		return debugInfo;
	}

	private static ContextDebugInfo createForContext( AcrossContextInfo context ) {
		ContextDebugInfo debugInfo = new ContextDebugInfo( "[Across]", context.getApplicationContext() );

		return debugInfo;
	}

	private static ContextDebugInfo createForApplicationContext( ApplicationContext applicationContext ) {
		ContextDebugInfo debugInfo = new ContextDebugInfo( applicationContext.getDisplayName(), applicationContext );

		return debugInfo;
	}
}
