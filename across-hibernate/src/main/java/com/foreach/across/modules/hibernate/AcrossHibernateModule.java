package com.foreach.across.modules.hibernate;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.SingletonBeanConfigurer;
import com.foreach.across.core.context.configurer.TransactionManagementConfigurer;
import com.foreach.across.modules.hibernate.provider.HasHibernatePackageProvider;
import com.foreach.across.modules.hibernate.provider.HibernatePackage;
import com.foreach.across.modules.hibernate.provider.HibernatePackageProvider;

import javax.sql.DataSource;
import java.util.*;

/**
 * Activates hibernate support on all modules implementing HasHibernatePackageProvider
 * Will also activate Transactional support on the modules.
 */
public class AcrossHibernateModule extends AcrossModule
{
	private Properties hibernateProperties = new Properties();

	private boolean autoEnableModules = true;
	private Set<HibernatePackageProvider> hibernatePackageProviders = new HashSet<HibernatePackageProvider>();
	private DataSource dataSource;

	public AcrossHibernateModule() {
	}

	public AcrossHibernateModule( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	@Override
	public String getName() {
		return "AcrossHibernateModule";
	}

	/**
	 * @return Description of the content of this module.
	 */
	@Override
	public String getDescription() {
		return "Enables Hibernate support on the context.  Scans modules that are HibernatePackageProviders for this module.";
	}

	/**
	 * If true this module will scan other modules to see if they provide HibernatePackageProviders for this interface.
	 *
	 * @return True if modules will be scanned and activated automatically.
	 */
	public boolean isAutoEnableModules() {
		return autoEnableModules;
	}

	public void setAutoEnableModules( boolean autoEnableModules ) {
		this.autoEnableModules = autoEnableModules;
	}

	/**
	 * Returns the set of HibernatePackageProvider instances configured directly on this module.
	 *
	 * @return Set of configured HibernatePackageProviders.
	 */
	public Set<HibernatePackageProvider> getHibernatePackageProviders() {
		return hibernatePackageProviders;
	}

	public void setHibernatePackageProviders( Set<HibernatePackageProvider> hibernatePackageProviders ) {
		this.hibernatePackageProviders = hibernatePackageProviders;
	}

	public void addHibernatePackageProvider( HibernatePackageProvider... hibernatePackageProvider ) {
		this.hibernatePackageProviders.addAll( Arrays.asList( hibernatePackageProvider ) );
	}

	public Properties getHibernateProperties() {
		return hibernateProperties;
	}

	public void setHibernateProperties( Properties hibernateProperties ) {
		this.hibernateProperties = hibernateProperties;
	}

	public void setHibernateProperty( String name, String value ) {
		hibernateProperties.put( name, value );
	}

	/**
	 * Get the datasource associated with this module.  Will return the context datasource if none
	 * has been set explicitly.
	 *
	 * @return Datasource associated with this module.
	 */
	public DataSource getDataSource() {
		return dataSource != null ? dataSource : getContext().getDataSource();
	}

	/**
	 * Set the datasource for the Hibernate sessionFactory.
	 * If the datasource is null, the context datasource will be used instead.
	 *
	 * @param dataSource Datasource for this module.
	 */
	public void setDataSource( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( HibernateConfiguration.class ) );
	}

	/**
	 * <p>Called when a context is preparing to bootstrap, but before the actual bootstrap happens.
	 * This is the last chance for a module to modify itself or its siblings before the actual
	 * bootstrapping will occur.</p>
	 * <p>Only modules that will actually bootstrap will be passed as parameters to this method.
	 * Any disabled modules will not be present.</p>
	 *
	 * @param currentModule  Bootstrap configuration of the current module.
	 * @param modulesInOrder Map of all modules that are being bootstrapped, in the bootstrap order and with their corresponding config.
	 */
	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
	                                 Map<AcrossModule, ModuleBootstrapConfig> modulesInOrder ) {
		HibernatePackage hibernatePackage = new HibernatePackage();

		for ( HibernatePackageProvider provider : getHibernatePackageProviders() ) {
			hibernatePackage.add( provider );
		}

		if ( autoEnableModules ) {
			for ( ModuleBootstrapConfig config : modulesInOrder.values() ) {
				AcrossModule module = config.getModule();

				if ( module instanceof HasHibernatePackageProvider ) {
					HibernatePackageProvider provider =
							( (HasHibernatePackageProvider) module ).getHibernatePackageProvider( this );

					if ( provider != null ) {
						hibernatePackage.add( provider );
					}
				}

				// Activate transaction management
				config.addApplicationContextConfigurer( new TransactionManagementConfigurer() );
			}
		}

		currentModule.addApplicationContextConfigurer(
				new SingletonBeanConfigurer( "hibernatePackage", hibernatePackage, true ) );
	}
}
