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
package test.encoding;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import test.AbstractWebIntegrationTest;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = TestDefaultCharacterEncoding.Config.class)
@TestPropertySource(properties = "spring.http.encoding.charset=UTF-16")
public class TestCustomCharacterEncoding extends AbstractWebIntegrationTest
{
	@Test
	public void encodingShouldBeUTF16() {
		assertEquals( "UTF-16", get( "/characterEncoding" ) );
	}

	@Test
	public void filterShouldBeRegistered() {
		assertEquals( "true", get( "filtered?name=characterEncodingFilter" ) );
	}
}
