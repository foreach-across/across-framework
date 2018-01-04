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
package test;

import com.foreach.across.config.EnableAcrossContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.modules.simple.SimpleWebModule;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestSimpleControllers.Config.class)
public class TestSimpleControllers extends AbstractWebIntegrationTest
{
	@Test
	public void helloController() {
		assertEquals( "hello", get( "/hello" ) );
	}

	@EnableAcrossContext(modules = SimpleWebModule.NAME)
	@Configuration
	static class Config
	{
	}
}
