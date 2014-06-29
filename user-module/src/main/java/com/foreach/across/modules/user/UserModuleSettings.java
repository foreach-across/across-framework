package com.foreach.across.modules.user;

public class UserModuleSettings
{
	/**
	 * Optional PasswordEncoder instance to be used.
	 * <p/>
	 * PasswordEncoder instance
	 *
	 * @see org.springframework.security.crypto.password.PasswordEncoder
	 */
	public static final String PASSWORD_ENCODER = "userModule.passwordEncoder";

	protected UserModuleSettings() {
	}
}