package com.foreach.across.core.context.info;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class ConfigurableAcrossContextInfo implements AcrossContextInfo
{
	private final AcrossContext context;

	private boolean bootstrapped;
	private Collection<AcrossModuleInfo> configuredModules =
			Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );

	private AcrossBootstrapConfig bootstrapConfiguration;

	public ConfigurableAcrossContextInfo( AcrossContext context ) {
		this.context = context;
	}

	@Override
	public AcrossContext getContext() {
		return context;
	}

	@Override
	public Collection<AcrossModuleInfo> getConfiguredModules() {
		return configuredModules;
	}

	public void setConfiguredModules( Collection<AcrossModuleInfo> configuredModules ) {
		this.configuredModules = Collections.unmodifiableCollection( configuredModules );
	}

	@Override
	public Collection<AcrossModuleInfo> getModules() {
		Collection<AcrossModuleInfo> modules = new LinkedList<>();

		for ( AcrossModuleInfo module : configuredModules ) {
			if ( module.isEnabled() ) {
				modules.add( module );
			}
		}

		return Collections.unmodifiableCollection( modules );
	}

	@Override
	public boolean hasModule( String moduleName ) {
		return getModuleInfo( moduleName ) != null;
	}

	@Override
	public AcrossModuleInfo getModuleInfo( String moduleName ) {
		for ( AcrossModuleInfo module : configuredModules ) {
			if ( StringUtils.equals( moduleName, module.getName() ) ) {
				return module;
			}
		}

		return null;
	}

	public ConfigurableAcrossModuleInfo getConfigurableModuleInfo( String moduleName ) {
		return (ConfigurableAcrossModuleInfo) getModuleInfo( moduleName );
	}

	@Override
	public AcrossModuleInfo getModuleBeingBootstrapped() {
		for ( AcrossModuleInfo moduleInfo : getModules() ) {
			if ( moduleInfo.getBootstrapStatus() == ModuleBootstrapStatus.BootstrapBusy ) {
				return moduleInfo;
			}
		}

		return null;
	}

	@Override
	public boolean isBootstrapped() {
		return bootstrapped;
	}

	public void setBootstrapped( boolean bootstrapped ) {
		this.bootstrapped = bootstrapped;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return AcrossContextUtils.getApplicationContext( context );
	}

	@Override
	public AcrossBootstrapConfig getBootstrapConfiguration() {
		return bootstrapConfiguration;
	}

	public void setBootstrapConfiguration( AcrossBootstrapConfig bootstrapConfiguration ) {
		this.bootstrapConfiguration = bootstrapConfiguration;
	}

	@Override
	public int getModuleIndex( String moduleName ) {
		for ( AcrossModuleInfo moduleInfo : configuredModules ) {
			if ( StringUtils.equals( moduleName, moduleInfo.getName() ) ) {
				return moduleInfo.getIndex();
			}
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public int getModuleIndex( AcrossModule module ) {
		Assert.notNull( module );
		return getModuleIndex( module.getName() );
	}

	@Override
	public int getModuleIndex( AcrossModuleInfo moduleInfo ) {
		Assert.notNull( moduleInfo );
		return getModuleIndex( moduleInfo.getName() );
	}

	@Override
	public <T> Collection<T> getBeansOfType( Class<T> requiredType, boolean scanModules ) {
		return AcrossContextUtils.getBeansOfType( getContext(), requiredType, scanModules );
	}
}
