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

import lombok.NonNull;

/**
 * Represents a modifiable configuration of an Across module.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
public interface MutableAcrossModuleConfiguration extends AcrossModuleConfiguration
{
	/**
	 * Extend the current configuration with the parameter value.
	 * Usually the parameter represents the configuration of an extension module.
	 *
	 * @param configuration to extend the current configuration with
	 */
	void extendWith( @NonNull AcrossModuleConfiguration configuration );
}
