package com.foreach.across.modules.adminweb;

public class AdminWebModuleSettings
{
	protected AdminWebModuleSettings() {
	}

	/**
	 * URL or relative path (without admin prefix) for the page to which a user will be sent
	 * after successful login without previously accessing a protected url.
	 *
	 * String
	 */
	public static final String LOGIN_DASHBOARD_URL = "adminWebModule.login.dashboardUrl";

	/**
	 * Boolean property.
	 * If true, the user will always redirect to the dashboard after login, even if a previously
	 * protected url has been accessed.
	 *
	 * Boolean - default: false
	 */
	public static final String LOGIN_REDIRECT_TO_DASHBOARD = "adminWebModule.login.redirectToDashboard";
}
