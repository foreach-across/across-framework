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
package com.foreach.across.modules.web.resource;

import com.foreach.across.modules.web.resource.rules.AddWebResourceRule;

import java.util.Optional;

/**
 * Interface that kan be implemented by {@link com.foreach.across.modules.web.ui.ViewElementBuilder}s that are
 * used inside {@link AddWebResourceRule}. If no explicit resource key is specified on the rule, the value of
 * {@link #getWebResourceKey()} will be used instead.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
@FunctionalInterface
public interface WebResourceKeyProvider
{
	/**
	 * @return the key that should be used for identifying the web resource
	 */
	Optional<String> getWebResourceKey();
}
