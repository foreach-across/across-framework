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
package com.foreach.across.modules.web.extensions;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketConfiguration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

/**
 * Ensures that the {@link DelegatingWebSocketConfiguration} is created at the latest possible moment,
 * when all {@link WebSocketConfigurer}s are created.
 * <p>
 * Exposes all {@link WebSocketConfigurer}s from the bootstrapped modules
 * so they can be picked up by the {@link DelegatingWebSocketConfiguration}.
 *
 * @author Steven Gentens
 * @since 3.0.0
 */
@ModuleConfiguration(AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE)
@ConditionalOnClass(WebSocketConfigurer.class)
@ConditionalOnBean(WebSocketConfigurer.class)
@Import({ DelegatingWebSocketConfiguration.class })
class EnableWebSocketConfiguration
{
}
