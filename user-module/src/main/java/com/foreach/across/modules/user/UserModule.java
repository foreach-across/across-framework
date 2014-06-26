package com.foreach.across.modules.user;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.database.HasSchemaConfiguration;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.installers.AcrossSequencesInstaller;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.*;
import com.foreach.across.modules.user.config.UserRepositoriesConfiguration;
import com.foreach.across.modules.user.config.UserSchemaConfiguration;
import com.foreach.across.modules.user.config.UserServicesConfiguration;
import com.foreach.across.modules.user.config.modules.UserAdminWebConfiguration;
import com.foreach.across.modules.user.config.modules.UserSpringSecurityConfiguration;
import com.foreach.across.modules.user.installers.DefaultUserInstaller;
import com.foreach.across.modules.user.installers.UserSchemaInstaller;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@AcrossDepends(required = AcrossHibernateModule.NAME, optional = "AdminWebModule")
public class UserModule extends AcrossModule implements HasHibernatePackageProvider, HasSchemaConfiguration
{
	public static final String NAME = "UserModule";

	private final SchemaConfiguration schemaConfiguration = new UserSchemaConfiguration();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Provides services and structure for user management with authorization functionality.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add(
				new AnnotatedClassConfigurer(
						UserRepositoriesConfiguration.class,
						UserServicesConfiguration.class,
						UserAdminWebConfiguration.class,
						UserSpringSecurityConfiguration.class
				)
		);
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				new AcrossSequencesInstaller(),
				new UserSchemaInstaller( schemaConfiguration ),
				new DefaultUserInstaller()
		};
	}

	/**
	 * Returns the package provider associated with this implementation.
	 *
	 * @param hibernateModule AcrossHibernateModule that is requesting packages.
	 * @return HibernatePackageProvider instance.
	 */
	public HibernatePackageProvider getHibernatePackageProvider( AcrossHibernateModule hibernateModule ) {
		if ( StringUtils.equals( "AcrossHibernateModule", hibernateModule.getName() ) ) {
			return new HibernatePackageProviderComposite(
					new PackagesToScanProvider( "com.foreach.across.modules.user.business" ),
					new TableAliasProvider( schemaConfiguration.getTables() ) );
		}

		return null;
	}

	@Override
	public SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule, AcrossBootstrapConfig contextConfig ) {
		contextConfig.extendModule( "SpringSecurityModule",
		                            UserSpringSecurityConfiguration.UserDetailsServiceConfiguration.class );
	}
}
