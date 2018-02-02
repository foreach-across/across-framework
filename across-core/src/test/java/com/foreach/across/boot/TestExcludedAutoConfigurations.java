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
package com.foreach.across.boot;

import com.foreach.across.config.AcrossApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;

/**
 * @author Steven Gentens
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestExcludedAutoConfigurations.SampleApplication.class)
public class TestExcludedAutoConfigurations
{
	@Autowired
	private ConfigurableListableBeanFactory beanFactory;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void specifiedAutoConfigurationsAreDisallowed() {
		AcrossApplicationAutoConfiguration autoConfiguration = AcrossApplicationAutoConfiguration.retrieve( beanFactory, applicationContext.getClassLoader() );
		assertFalse( autoConfiguration.notExcluded( AopAutoConfiguration.class.getName() ) );
		assertFalse( autoConfiguration.notExcluded( WebSocketServletAutoConfiguration.class.getName() ) );
		assertFalse( autoConfiguration.notExcluded( RabbitAutoConfiguration.class.getName() ) );
	}

	@AcrossApplication(excludeAutoConfigurations = { AopAutoConfiguration.class, WebSocketServletAutoConfiguration.class, RabbitAutoConfiguration.class })
	@Configuration
	protected static class SampleApplication
	{
	}
}
