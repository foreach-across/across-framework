package com.foreach.across.modules.adminweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.database.HasSchemaConfiguration;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.modules.adminweb.config.AdminServicesConfig;
import com.foreach.across.modules.adminweb.config.AdminWebConfig;
import com.foreach.across.modules.adminweb.config.UserSchemaConfiguration;
import com.foreach.across.modules.adminweb.installers.AdminWebRolesInstaller;
import com.foreach.across.modules.adminweb.installers.AdminWebSchemaInstaller;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@AcrossDepends(required = "AcrossHibernateModule", optional = "AcrossWebModule")
public class AdminWebModule extends AcrossModule implements HasHibernatePackageProvider, HasSchemaConfiguration
{
	public static final String NAME = "AdminWebModule";

	private String rootPath = "/admin";

	private final SchemaConfiguration schemaConfiguration = new UserSchemaConfiguration();

	/**
	 * @return The root path for all AdminWebControllers.
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * Set the root path that all AdminWebController instances should use.  All request mappings
	 * will be prefixed with the path specified here.
	 *
	 * @param rootPath The root path for all AdminWebControllers.
	 * @see org.springframework.web.bind.annotation.RequestMapping
	 */
	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Provides user authentication and authorization features with management web pages.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( AdminServicesConfig.class, AdminWebConfig.class ) );
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] { new AdminWebSchemaInstaller( schemaConfiguration ), new AdminWebRolesInstaller() };
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
					new PackagesToScanProvider( "com.foreach.across.modules.adminweb.business" ),
					new TableAliasProvider( schemaConfiguration.getTables() ) );
		}

		return null;
	}

	@Override
	public SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}
}
