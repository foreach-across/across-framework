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
package com.foreach.across.core.context.module;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the actual configuration that will be used to bootstrap a module.
 * Internal to the framework, clients usually use either {@link AcrossModuleConfiguration} or {@link MutableAcrossModuleConfiguration}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class AcrossModuleBootstrapConfiguration implements MutableAcrossModuleConfiguration
{
	@NonNull
	@Getter
	private final AcrossModuleDescriptor moduleDescriptor;

	@Getter
	private final Collection<AcrossModuleConfiguration> extensions = new ArrayList<>();

	@Override
	public void extendWith( @NonNull AcrossModuleConfiguration configuration ) {
		extensions.add( configuration );
	}
}
