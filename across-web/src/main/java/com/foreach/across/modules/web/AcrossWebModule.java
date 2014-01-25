package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.bootstrap.BootstrapAdapter;

public class AcrossWebModule extends AcrossModule implements BootstrapAdapter
{
	public AcrossWebModule() {
	}

	@Override
	public String getName() {
		return "AcrossWebModule";
	}

	@Override
	public String getDescription() {
		return "Base Across web functionality based on spring mvc";
	}

	@Override
	public void bootstrap() {
		super.bootstrap();
	}

	@Override
	public String[] getComponentScanPackages() {
		return new String[] { "com.foreach.across.modules.web.menu", "com.foreach.across.modules.web.ui" };
	}

	/**
	 * Customize the AcrossBootstrapper involved.
	 *
	 * @param bootstrapper AcrossBootstrapper instance.
	 */
	public void customizeBootstrapper( AcrossBootstrapper bootstrapper ) {
		bootstrapper.setApplicationContextFactory( new WebBootstrapApplicationContextFactory() );
	}
}
