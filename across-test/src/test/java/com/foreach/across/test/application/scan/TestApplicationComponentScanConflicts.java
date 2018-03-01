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
package com.foreach.across.test.application.scan;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.context.AcrossApplicationContext;
import org.junit.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

import java.lang.annotation.*;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@SuppressWarnings("WeakerAccess")
public class TestApplicationComponentScanConflicts
{
	@Test
	public void startsWithUnknownComponentScan() {
		bootstrap( ValidComponentScan.class );
	}

	@Test
	public void exceptionWithDefaultComponentScan() {
		assertThatExceptionOfType( AcrossConfigurationException.class )
				.isThrownBy( () -> bootstrap( DefaultComponentScan.class ) );
	}

	@Test
	public void overlapWithNested() {
		assertThatExceptionOfType( AcrossConfigurationException.class )
				.isThrownBy( () -> bootstrap( NestedComponentScan.class ) );
	}

	@Test
	public void metaComponentScan() {
		assertThatExceptionOfType( AcrossConfigurationException.class )
				.isThrownBy( () -> bootstrap( MetaComponentScan.class ) );
	}

	private void bootstrap( Class<?> applicationClass ) {
		AcrossApplicationContext context = new AcrossApplicationContext();
		context.register( applicationClass );
		context.refresh();
		context.start();
	}

	@AcrossApplication
	@ComponentScan("some.other.package")
	protected static class ValidComponentScan
	{
	}

	@AcrossApplication
	@ComponentScan
	protected static class DefaultComponentScan
	{
	}

	@AcrossApplication
	@ComponentScans({
			@ComponentScan("some.other.package"),
			@ComponentScan("com.foreach.across.test.application.scan.infrastructure")
	})
	protected static class NestedComponentScan
	{
	}

	@AcrossApplication
	@MyScan
	protected static class MetaComponentScan
	{
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Documented
	@ComponentScan(basePackageClasses = MyScan.class)
	public @interface MyScan
	{
	}
}
