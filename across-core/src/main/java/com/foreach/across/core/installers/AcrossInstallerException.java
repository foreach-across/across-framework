/*
 * Copyright 2014 the original author or authors
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

import com.foreach.across.core.AcrossException;
import lombok.Getter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

import static java.lang.String.format;

/**
 * Specialization of {@link AcrossException} for an exception that occurred during the execution of an installer.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.diagnostics.AcrossInstallerExceptionFailureAnalyzer
 * @since 3.0.0
 */
public class AcrossInstallerException extends AcrossException
{
	@Getter
	private Class<?> installerClass;

	@Getter
	private Method installerMethod;

	@Getter
	private InstallerMetaData installerMetaData;

	public AcrossInstallerException( String moduleName, InstallerMetaData installerMetaData, Object installer, Method method, Throwable cause ) {
		super( format("Exception executing installer %s for module %s", installerMetaData.getName(), moduleName), cause );

		setModuleBeingProcessed( moduleName );
		this.installerMetaData = installerMetaData;
		this.installerClass = ClassUtils.getUserClass( installer );
		this.installerMethod = method;
	}
}
