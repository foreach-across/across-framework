package com.foreach.across.core.context.info;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;

public class ConfigurableAcrossModuleInfo implements AcrossModuleInfo
{
	private final int index;
	private final AcrossContextInfo context;
	private final AcrossModule module;

	private final String moduleName;
	private final boolean enabled;

	private AcrossModuleRole moduleRole = AcrossModuleRole.APPLICATION;
	private ModuleBootstrapStatus bootstrapStatus;
	private ModuleBootstrapConfig bootstrapConfiguration;

	private Collection<AcrossModuleInfo> requiredDependencies =
			Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );
	private Collection<AcrossModuleInfo> optionalDependencies =
			Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );

	public ConfigurableAcrossModuleInfo( AcrossContextInfo context, AcrossModule module, int index ) {
		this.context = context;
		this.module = module;
		this.index = index;

		moduleName = module.getName();
		enabled = module.isEnabled();
		bootstrapStatus = enabled ? ModuleBootstrapStatus.AwaitingBootstrap : ModuleBootstrapStatus.Disabled;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public AcrossContextInfo getContextInfo() {
		return context;
	}

	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public String getDescription() {
		return getModule().getDescription();
	}

	@Override
	public AcrossModule getModule() {
		return module;
	}

	@Override
	public Collection<AcrossModuleInfo> getRequiredDependencies() {
		return requiredDependencies;
	}

	public void setRequiredDependencies( Collection<AcrossModuleInfo> requiredDependencies ) {
		this.requiredDependencies = Collections.unmodifiableCollection( requiredDependencies );
	}

	public void setOptionalDependencies( Collection<AcrossModuleInfo> optionalDependencies ) {
		this.optionalDependencies = Collections.unmodifiableCollection( optionalDependencies );
	}

	@Override
	public Collection<AcrossModuleInfo> getOptionalDependencies() {
		return optionalDependencies;
	}

	@Override
	public AcrossModuleRole getModuleRole() {
		return moduleRole;
	}

	public void setModuleRole( AcrossModuleRole moduleRole ) {
		this.moduleRole = moduleRole;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isBootstrapped() {
		return bootstrapStatus == ModuleBootstrapStatus.Bootstrapped;
	}

	public void setBootstrapStatus( ModuleBootstrapStatus bootstrapStatus ) {
		this.bootstrapStatus = bootstrapStatus;
	}

	@Override
	public ModuleBootstrapStatus getBootstrapStatus() {
		return bootstrapStatus;
	}

	@Override
	public ModuleBootstrapConfig getBootstrapConfiguration() {
		return bootstrapConfiguration;
	}

	public void setBootstrapConfiguration( ModuleBootstrapConfig bootstrapConfiguration ) {
		this.bootstrapConfiguration = bootstrapConfiguration;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return AcrossContextUtils.getApplicationContext( module );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getBean( String beanName ) {
		return (T) getApplicationContext().getBean( beanName );
	}
}
