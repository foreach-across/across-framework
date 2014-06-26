package com.foreach.across.modules.hibernate;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.SingletonBeanConfigurer;
import com.foreach.across.core.context.configurer.TransactionManagementConfigurer;
import com.foreach.across.modules.hibernate.config.HibernateConfiguration;
import com.foreach.across.modules.hibernate.config.TransactionManagerConfiguration;
import com.foreach.across.modules.hibernate.config.UnitOfWorkConfiguration;
import com.foreach.across.modules.hibernate.provider.HasHibernatePackageProvider;
import com.foreach.across.modules.hibernate.provider.HibernatePackage;
import com.foreach.across.modules.hibernate.provider.HibernatePackageProvider;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Activates hibernate support on all modules implementing HasHibernatePackageProvider
 * Will also activate Transactional support on the modules.
 */
public class AcrossHibernateModule extends AcrossModule
{
	public static final String NAME = "AcrossHibernateModule";

	private Properties hibernateProperties = new Properties();

	private boolean autoEnableModules = true;
	private boolean configureTransactionManagement = true;
	private boolean configureUnitOfWorkFactory = false;
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
		return NAME;
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
	 * If true a UnitOfWorkFactory will be created for the sessionFactory defined in this module.
	 * It can then be used to start and stop sessions without using the SessionFactory directly.
	 *
	 * @return True if a UnitOfWorkFactory will be created.
	 * @see com.foreach.across.modules.hibernate.unitofwork.UnitOfWorkFactory
	 */
	public boolean isConfigureUnitOfWorkFactory() {
		return configureUnitOfWorkFactory;
	}

	public void setConfigureUnitOfWorkFactory( boolean configureUnitOfWorkFactory ) {
		this.configureUnitOfWorkFactory = configureUnitOfWorkFactory;
	}

	/**
	 * If true a TransactionManager will be created for the sessionFactory defined in this module.
	 * Additionally transaction management will be enabled in all other modules if autoEnableModules is true.
	 * <p/>
	 * If transaction management is disabled, the user will have to take care of managing the sessions outside the module.
	 *
	 * @return True if a TransactionManager will be created and transaction management will be enabled in all other modules.
	 * @see #isAutoEnableModules()
	 */
	public boolean isConfigureTransactionManagement() {
		return configureTransactionManagement;
	}

	public void setConfigureTransactionManagement( boolean configureTransactionManagement ) {
		this.configureTransactionManagement = configureTransactionManagement;
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

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
	                                 AcrossBootstrapConfig contextConfig ) {
		HibernatePackage hibernatePackage = new HibernatePackage();

		for ( HibernatePackageProvider provider : getHibernatePackageProviders() ) {
			hibernatePackage.add( provider );
		}

		if ( isConfigureTransactionManagement() ) {
			currentModule.addApplicationContextConfigurer(
					new AnnotatedClassConfigurer( TransactionManagerConfiguration.class )
			);
		}

		if ( autoEnableModules ) {
			for ( ModuleBootstrapConfig config : contextConfig.getModules() ) {
				AcrossModule module = config.getModule();

				if ( module instanceof HasHibernatePackageProvider ) {
					HibernatePackageProvider provider =
							( (HasHibernatePackageProvider) module ).getHibernatePackageProvider( this );

					if ( provider != null ) {
						hibernatePackage.add( provider );
					}
				}

				// Activate transaction management
				if ( isConfigureTransactionManagement() ) {
					config.addApplicationContextConfigurer( new TransactionManagementConfigurer() );
				}
			}
		}

		currentModule.addApplicationContextConfigurer(
				new SingletonBeanConfigurer( "hibernatePackage", hibernatePackage, true )
		);

		if ( configureUnitOfWorkFactory ) {
			currentModule.addApplicationContextConfigurer(
					new AnnotatedClassConfigurer( UnitOfWorkConfiguration.class )
			);
		}
	}
}
