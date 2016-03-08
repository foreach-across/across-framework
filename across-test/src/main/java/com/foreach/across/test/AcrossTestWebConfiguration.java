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

package com.foreach.across.test;

import com.foreach.across.modules.web.AcrossWebModule;

import java.lang.annotation.*;

/**
 * Boots a default {@link com.foreach.across.core.AcrossContext} with only the {@link AcrossWebModule} configured.
 * <p>Since refactoring the {@link AcrossTestConfiguration} annotation in 1.1.2, this annotation has little
 * added value and can be replaced by {@code @AcrossTestConfiguration(modules = { AcrossWebModule.NAME })}.
 * It will be removed in future releases.</p>
 *
 * @deprecated since 1.1.2
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@AcrossTestConfiguration(modules = { AcrossWebModule.NAME })
public @interface AcrossTestWebConfiguration
{
}
