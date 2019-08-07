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
package com.foreach.across.core.installers;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * Represents the reference to a single installer instance.
 * An installer can be represented either as the actual bean,
 * the class of the installer or the class name of the installer.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class InstallerReference
{
	@Getter(AccessLevel.PACKAGE)
	private final Object target;

	static class InstallerClassName extends InstallerReference
	{
		InstallerClassName( Object target ) {
			super( target );
		}
	}

	static class InstallerBean extends InstallerReference
	{
		InstallerBean( Object target ) {
			super( target );
		}
	}

	/**
	 * Create a reference for an installer bean, class or class name.
	 *
	 * @param installer installer bean, class or class name
	 * @return reference
	 */
	@NonNull
	public static InstallerReference from( @NonNull @lombok.NonNull Object installer ) {
		if ( installer instanceof String ) {
			return from( (String) installer );
		}
		if ( installer instanceof Class ) {
			return from( (Class) installer );
		}

		return new InstallerBean( installer );
	}

	/**
	 * Create a reference for an installer class.
	 *
	 * @param installerClass class of the installer
	 * @return reference
	 */
	@NonNull
	public static InstallerReference from( @NonNull @lombok.NonNull Class<?> installerClass ) {
		return from( installerClass.getName() );
	}

	/**
	 * Create a reference for an installer class, by name.
	 *
	 * @param installerClassName name of the installer type
	 * @return reference
	 */
	@NonNull
	public static InstallerReference from( @NonNull @lombok.NonNull String installerClassName ) {
		return new InstallerClassName( installerClassName );
	}
}
