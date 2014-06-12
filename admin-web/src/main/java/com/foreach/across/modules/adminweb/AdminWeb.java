package com.foreach.across.modules.adminweb;

import com.foreach.across.modules.web.context.PrefixingPathContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public final class AdminWeb extends PrefixingPathContext
{
	public static final String MODULE = "AdminWebModule";

	public static final String LAYOUT_TEMPLATE_CSS = "/css/adminweb/adminweb.css";
	public static final String LAYOUT_TEMPLATE = "th/adminweb/layouts/adminPage";

	@Autowired
	private Environment environment;

	private final String title;
	private final AdminWebSettings settings;

	public AdminWeb( String prefix, String title ) {
		super( prefix );

		this.title = title;
		this.settings = new AdminWebSettings();

	}

	public String getTitle() {
		return title;
	}

	public AdminWebSettings getSettings() {
		return settings;
	}

	public class AdminWebSettings
	{
		public boolean isRememberMeEnabled() {
			return !StringUtils.isBlank( environment.getProperty( AdminWebModuleSettings.REMEMBER_ME_KEY, "" ) );
		}
	}
}
