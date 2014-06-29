package com.foreach.across.modules.adminweb;

public class AdminWebModuleSettings
{
	/**
	 * URL or relative path (without admin prefix) for the page to which a user will be sent
	 * after successful login without previously accessing a protected url.
	 * <p/>
	 * String
	 */
	public static final String LOGIN_DASHBOARD_URL = "adminWebModule.login.dashboardUrl";

	/**
	 * If true, the user will always redirect to the dashboard after login, even if a previously
	 * protected url has been accessed.
	 * <p/>
	 * Boolean - default: false
	 */
	public static final String LOGIN_REDIRECT_TO_DASHBOARD = "adminWebModule.login.redirectToDashboard";

	/**
	 * Key used for creating the remember me cookie.  If this property is not present, remember me will not be enabled.
	 */
	public static final String REMEMBER_ME_KEY = "adminWebModule.login.rememberMe.key";

	/**
	 * Number of seconds a remember me token should be valid.
	 * Defaults to 2592000 (30 days).
	 * <p/>
	 * Integer
	 */
	public static final String REMEMBER_ME_TOKEN_VALIDITY_SECONDS =
			"adminWebModule.login.rememberMe.tokenValiditySeconds";

	protected AdminWebModuleSettings() {
	}
}
