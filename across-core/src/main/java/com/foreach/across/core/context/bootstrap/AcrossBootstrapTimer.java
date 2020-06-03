/*
 * Copyright 2019 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.installers.AcrossBootstrapInstallerRegistry;
import com.foreach.across.core.installers.InstallerPhase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.*;
import java.util.stream.Stream;

/**
 * Used for creating an Across startup time report.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Slf4j
public class AcrossBootstrapTimer
{
	private StopWatch stopWatch = new StopWatch();

	private final Map<Phase, Long> timesByPhase = new HashMap<>();
	private final Map<String, ModuleTimeReport> moduleReports = new LinkedHashMap<>();

	@Getter
	private long configurationPhaseInMillis;

	@Getter
	private long refreshBeansInMillis;

	@Getter
	private long bootstrappedEventInMillis;

	public long getTotalModuleApplicationContextTimeInMillis() {
		return moduleReports.values().stream()
		                    .mapToLong( ModuleTimeReport::getApplicationContextTimeInMillis )
		                    .sum();
	}

	public long getTotalInstallersTimeInMillis() {
		return moduleReports.values().stream()
		                    .mapToLong( ModuleTimeReport::getTotalInstallersTimeInMillis )
		                    .sum();
	}

	public long getInstallersTimeInMillis( InstallerPhase phase ) {
		return moduleReports.values().stream()
		                    .mapToLong( tr -> tr.getInstallersTimeInMillis( phase ) )
		                    .sum();
	}

	/**
	 * Global Across context bootstrap.
	 */
	void start() {
		stopWatch.start();
	}

	void finish() {
		stopWatch.stop();
		timesByPhase.put( Phase.TOTAL, stopWatch.getTime() );
		stopWatch = null;
	}

	/**
	 * Building configuration of modules and context.
	 */
	void startConfigurationPhase() {
		configurationPhaseInMillis = System.currentTimeMillis();
	}

	void finishConfigurationPhase() {
		configurationPhaseInMillis = System.currentTimeMillis() - configurationPhaseInMillis;
	}

	/**
	 * Module bootstrap
	 */
	void startModuleBootstrap( AcrossModuleInfo moduleInfo ) {
		ModuleTimeReport moduleTimeReport = moduleReports.computeIfAbsent( moduleInfo.getName(), moduleName -> new ModuleTimeReport() );
		moduleTimeReport.moduleInfo = moduleInfo;
		moduleTimeReport.moduleBootstrapTimeInMillis = System.currentTimeMillis();
	}

	void finishModuleBootstrap( AcrossModuleInfo moduleInfo ) {
		ModuleTimeReport moduleTimeReport = moduleReports.get( moduleInfo.getName() );
		moduleTimeReport.moduleBootstrapTimeInMillis = System.currentTimeMillis() - moduleTimeReport.moduleBootstrapTimeInMillis;
	}

	void addInstallerTimeReports( Collection<AcrossBootstrapInstallerRegistry.ModuleInstallersTimeReport> installerTimeReports ) {
		installerTimeReports.forEach( tr -> {
			ModuleTimeReport moduleTimeReport = moduleReports.computeIfAbsent( tr.getModuleName(), moduleName -> new ModuleTimeReport() );
			moduleTimeReport.installerTimesInMillis.put( tr.getInstallerPhase(), tr.getDurationInMillis() );
		} );
	}

	void startRefreshBeansPhase() {
		refreshBeansInMillis = System.currentTimeMillis();
	}

	void finishRefreshBeansPhase() {
		refreshBeansInMillis = System.currentTimeMillis() - refreshBeansInMillis;
	}

	void startContextBootstrappedEventHandling() {
		bootstrappedEventInMillis = System.currentTimeMillis();
	}

	void finishContextBootstrappedEventHandling() {
		bootstrappedEventInMillis = System.currentTimeMillis() - bootstrappedEventInMillis;
	}

	void printReport() {
		if ( LOG.isInfoEnabled() ) {
			LOG.info( "" );
			LOG.info( "--- Across bootstrap time report (in seconds)" );
			LOG.info( "" );
			LOG.info( "    Total time: {}", timesByPhase.get( Phase.TOTAL ) / 1000.0 );
			LOG.info( "    Configuration: {}", configurationPhaseInMillis / 1000.0 );
			LOG.info( "    ApplicationContexts: {}", getTotalModuleApplicationContextTimeInMillis() / 1000.0 );
			LOG.info( "    Installers: {}", getTotalInstallersTimeInMillis() / 1000.0 );

			Stream.of( InstallerPhase.values() ).forEach( phase -> {
				long duration = getInstallersTimeInMillis( phase );
				if ( duration > 0 ) {
					LOG.info( "     + {}: {}", phase, duration / 1000.0 );
				}
			} );

			LOG.info( "    Refresh beans: {}", refreshBeansInMillis / 1000.0 );
			LOG.info( "    Context bootstrapped event: {}", bootstrappedEventInMillis / 1000.0 );
			LOG.info( "" );

			moduleReports.values()
			             .stream()
			             .sorted( Comparator.comparingInt( tr -> tr.moduleInfo.getIndex() ) )
			             .forEach( tr -> {
				             LOG.info( "    {}:", tr.moduleInfo.getName() );
				             LOG.info( "    - Status: {}", tr.moduleInfo.getBootstrapStatus().name() );
				             LOG.info( "    - ApplicationContext: {}", tr.getApplicationContextTimeInMillis() / 1000.0 );
				             LOG.info( "    - Installers: {}", tr.getTotalInstallersTimeInMillis() / 1000.0 );

				             tr.getInstallerTimesInMillis().forEach( ( phase, duration ) -> {
					             LOG.info( "      + {}: {}", phase, duration / 1000.0 );
				             } );

				             LOG.info( "" );
			             } );

			LOG.info( "--- end bootstrap time report" );
			LOG.info( "" );
		}
	}

	public static class ModuleTimeReport
	{
		private AcrossModuleInfo moduleInfo;
		private long moduleBootstrapTimeInMillis;

		@Getter
		private final Map<InstallerPhase, Long> installerTimesInMillis = new LinkedHashMap<>();

		public long getApplicationContextTimeInMillis() {
			return moduleBootstrapTimeInMillis
					- getInstallersTimeInMillis( InstallerPhase.BeforeModuleBootstrap )
					- getInstallersTimeInMillis( InstallerPhase.AfterModuleBootstrap );
		}

		public long getTotalInstallersTimeInMillis() {
			return installerTimesInMillis.values().stream().mapToLong( l -> l ).sum();
		}

		public long getInstallersTimeInMillis( InstallerPhase phase ) {
			return installerTimesInMillis.getOrDefault( phase, 0L );
		}
	}

	private enum Phase
	{
		TOTAL,
		CONFIGURATION,
		MODULE_BOOTSTRAP,
		REFRESH_BEANS,
		INSTALLERS,
		BOOTSTRAPPED_EVENT
	}
}
