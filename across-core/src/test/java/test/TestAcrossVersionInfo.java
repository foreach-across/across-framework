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

package test;

import com.foreach.across.core.AcrossVersionInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestAcrossVersionInfo
{
	@Test
	void testDefaultManifestLoading() {
		Class mockedClass = Boolean.class;
		AcrossVersionInfo info = AcrossVersionInfo.load( mockedClass );
		assertTrue( info.isAvailable() );
	}

	@Test
	void testNoManifestLoadingForFileResource() {
		Class mockedClass = TestAcrossVersionInfo.class;
		AcrossVersionInfo info = AcrossVersionInfo.load( mockedClass );
		assertFalse( info.isAvailable() );
	}
}
