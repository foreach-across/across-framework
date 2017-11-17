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
package com.foreach.across.modules.web.config;

import com.foreach.across.modules.web.extensions.EnableWebMvcConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Creates the Across Web infrastructure for the module itself.
 * Beans created here can usually be accessed directly in dependant modules, whereas
 * default Web MVC support will be activated in the post processor module, requiring
 * the use of {@code @Lazy} or {@code @PostRefresh} to access them.
 *
 * @author Arne Vandamme
 * @see EnableWebMvcConfiguration
 * @since 3.0.0
 */
@Configuration
@Import({ HttpMessageConvertersAutoConfiguration.class, JacksonAutoConfiguration.class, GsonAutoConfiguration.class, WebClientAutoConfiguration.class })
class AcrossWebConfiguration
{
}
