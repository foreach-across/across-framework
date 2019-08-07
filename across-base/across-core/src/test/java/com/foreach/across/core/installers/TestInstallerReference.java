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
package com.foreach.across.core.installers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("InstallerReference creation")
class TestInstallerReference
{
	@Test
	@DisplayName("from bean")
	void bean() {
		InstallerReference reference = InstallerReference.from( 123L );
		assertThat( reference )
				.isInstanceOf( InstallerReference.InstallerBean.class )
				.isEqualTo( InstallerReference.from( 123L ) );
	}

	@Test
	@DisplayName("from class")
	void type() {
		InstallerReference reference = InstallerReference.from( String.class );
		assertThat( reference )
				.isInstanceOf( InstallerReference.InstallerClassName.class )
				.isEqualTo( InstallerReference.from( String.class ) )
				.isEqualTo( InstallerReference.from( String.class.getName() ) );
	}

	@Test
	@DisplayName("from class name")
	void className() {
		InstallerReference reference = InstallerReference.from( "java.lang.String" );
		assertThat( reference )
				.isInstanceOf( InstallerReference.InstallerClassName.class )
				.isEqualTo( InstallerReference.from( String.class.getName() ) )
				.isEqualTo( InstallerReference.from( String.class ) )
				.isNotEqualTo( InstallerReference.from( 123L ) );
	}
}
