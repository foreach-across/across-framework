package com.foreach.across.core;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public abstract class AcrossInstaller
{
	@Autowired
	private AcrossContext context;

	@Autowired
	private AcrossInstallerDao installerDao;

	@PostConstruct
	public void execute() {
		String installerName = getClass().getName();

		if ( !installerDao.isInstalled( installerName ) ) {
			if ( context.isAllowInstallers() ) {
				if ( !context.isOnlyRegisterInstallers() ) {
					install();
				}
				else {
					// Skipping installer execution
				}

				installerDao.setInstalled( installerName );
			}
			else {
				// Skip installer
			}
		}
	}

	protected abstract void install();
}
