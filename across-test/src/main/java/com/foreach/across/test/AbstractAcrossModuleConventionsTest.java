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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.web.AcrossWebModule;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract unit test for testing {@link AcrossModule} conventions.
 *
 * @author Arne Vandamme
 */
public abstract class AbstractAcrossModuleConventionsTest
{
	private AcrossModule module;

	@BeforeEach
	public void setUp() {
		module = createModule();
	}

	@Test
	public void moduleProperties() throws IllegalAccessException {
		assertNotNull( module );
		assertNotNull( module.getName(), "A module should provide a name" );

		int nameLength = StringUtils.length( StringUtils.trim( module.getName() ) );

		assertTrue( nameLength > 0, "Module name must not be only whitespace" );
		assertFalse( StringUtils.containsWhitespace(
				module.getName() ), "Module name must not contain any whitespace" );
		assertTrue( nameLength <= 250, "Module name should not be longer than 250 characters" );
		assertTrue( StringUtils.isAlphanumeric(
				module.getName() ), "Module name should only contain alphanumeric characters" );
		assertNotNull( module.getDescription(), "A module should provide a description" );
		assertFalse( StringUtils.isBlank( module.getDescription() ), "A module should provide a description" );

		Class moduleClass = module.getClass();

		Field nameField = ReflectionUtils.findField( moduleClass, "NAME" );

		String nameMsg = "Module does not define a valid public static final NAME field";

		assertNotNull( nameField, nameMsg );
		assertTrue( ReflectionUtils.isPublicStaticFinal( nameField ), nameMsg );

		String name = (String) nameField.get( module );
		assertEquals( module.getName(), name, "Module name does not match with the public NAME field" );

		if ( !module.getName().equals( module.getResourcesKey() ) ) {
			String resourcesKey = module.getResourcesKey();

			if ( !AcrossWebModule.NAME.equals( module.getName() ) || !"".equals( resourcesKey ) ) {
				assertNotNull( resourcesKey, "A valid resources key must be specified" );
				assertFalse( StringUtils.containsWhitespace(
						resourcesKey ), "Resources key must not contain any whitespace" );
				assertTrue( StringUtils.isAlphanumeric(
						resourcesKey ), "Resources key must only be alphanumeric characters" );

				Field resourcesKeyField = ReflectionUtils.findField( moduleClass, "RESOURCES" );

				String resourcesKeyMsg = "Module does not define a valid public static final RESOURCES field.  " +
						"This is advised if the resources key is not the same as the module name.";

				assertNotNull( resourcesKeyField, resourcesKeyMsg );
				assertTrue( ReflectionUtils.isPublicStaticFinal( resourcesKeyField ), resourcesKeyMsg );

				String constantResourcesKey = (String) resourcesKeyField.get( module );
				assertEquals( resourcesKey,
				              constantResourcesKey, "Resources key does not match with the public RESOURCES field" );
			}
		}
	}

	protected abstract AcrossModule createModule();
}
