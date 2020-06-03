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
package com.foreach.across.modules.web.extensions;

import com.foreach.across.core.annotations.ModuleConfiguration;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpSessionScope;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Activates web socket scope in every module.
 *
 * @author Steven Gentens
 * @since 3.0.0
 */
@ModuleConfiguration(optional = true)
@ConditionalOnClass({ WebSocketMessageBrokerConfigurer.class, SimpSessionScope.class })
class EnableWebSocketScopeConfiguration
{
	@Bean
	public static CustomScopeConfigurer webSocketScopeConfigurer() {
		CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		configurer.addScope( "websocket", new SimpSessionScope() );
		return configurer;
	}
}
