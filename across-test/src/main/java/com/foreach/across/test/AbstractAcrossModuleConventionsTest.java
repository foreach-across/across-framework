package com.foreach.across.test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossModuleSettings;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Abstract unit test for testing AcrossModule conventions.
 *
 * @author Arne Vandamme
 */
public abstract class AbstractAcrossModuleConventionsTest
{
	private AcrossModule module;

	@Before
	public void setUp() {
		module = createModule();
	}

	@Test
	public void moduleProperties() throws IllegalAccessException {
		assertNotNull( module );
		assertNotNull( "A module should provide a name", module.getName() );

		int nameLength = StringUtils.length( StringUtils.trim( module.getName() ) );

		assertTrue( "Module name must not be only whitespace", nameLength > 0 );
		assertTrue( "Module name should not be longer than 250 characters", nameLength <= 250 );
		assertTrue( "Module name should only contain ASCII characters", StringUtils.isAsciiPrintable(
				module.getName() ) );
		assertNotNull( "A module should provide a description", module.getDescription() );
		assertFalse( "A module should provide a description", StringUtils.isBlank( module.getDescription() ) );

		Class moduleClass = module.getClass();

		Field nameField = ReflectionUtils.findField( moduleClass, "NAME" );

		String nameMsg = "Module does not define a valid public static final NAME field";

		assertNotNull( nameMsg, nameField );
		assertTrue( nameMsg, ReflectionUtils.isPublicStaticFinal( nameField ) );

		String name = (String) nameField.get( module );
		assertEquals( "Module name does not match with the public NAME field", module.getName(), name );
	}

	@Test
	public void moduleSettings() {
		String settingsClassName = module.getClass().getName() + "Settings";
		Class settingsClass = loadClass( settingsClassName );

		if ( hasSettings() ) {
			assertNotNull( "Test declares module has settings but class was not found: " + settingsClassName,
			               settingsClass );

			if ( !AcrossModuleSettings.class.isAssignableFrom( settingsClass ) ) {
				fail( "A settings class must extend AcrossModuleSettings" );
			}
		}
		else {
			assertNull( "Test declares module does not have settings but class was found", settingsClass );
		}
	}

	private Class loadClass( String className ) {
		try {
			return Class.forName( className );
		}
		catch ( Exception e ) {
			return null;
		}
	}

	/**
	 * @return True if settings file should be tested.
	 */
	protected abstract boolean hasSettings();

	protected abstract AcrossModule createModule();
}
