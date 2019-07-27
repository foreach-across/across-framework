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
package com.foreach.across.core.support;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestInheritedAttributeValue
{
	@Test
	public void existsIfAncestorLevelIsNonNegative() {
		assertThat( new InheritedAttributeValue<>( Optional.empty(), "any", -1 ).exists() ).isFalse();
		assertThat( new InheritedAttributeValue<>( Optional.empty(), "any", 0 ).exists() ).isTrue();
		assertThat( new InheritedAttributeValue<>( Optional.empty(), "any", 1 ).exists() ).isTrue();
	}

	@Test
	public void localAndInheritedAttribute() {
		InheritedAttributeValue<Object> local = new InheritedAttributeValue<>( Optional.empty(), "any", 0 );
		assertThat( local.isLocalAttribute() ).isTrue();
		assertThat( local.isInheritedAttribute() ).isFalse();

		InheritedAttributeValue<Object> inherited = new InheritedAttributeValue<>( Optional.empty(), "any", 1 );
		assertThat( inherited.isLocalAttribute() ).isFalse();
		assertThat( inherited.isInheritedAttribute() ).isTrue();

		InheritedAttributeValue<Object> nonExisting = new InheritedAttributeValue<>( Optional.empty(), "any", -1 );
		assertThat( nonExisting.isLocalAttribute() ).isFalse();
		assertThat( nonExisting.isInheritedAttribute() ).isFalse();
	}

	@Test
	public void empty() {
		InheritedAttributeValue<Object> existing = new InheritedAttributeValue<>( Optional.empty(), "any", 0 );
		assertThat( existing.isEmpty() ).isTrue();

		InheritedAttributeValue<Object> existingNonEmpty = new InheritedAttributeValue<>( Optional.of( 1 ), "any", 0 );
		assertThat( existingNonEmpty.isEmpty() ).isFalse();

		InheritedAttributeValue<Object> nonExisting = new InheritedAttributeValue<>( Optional.empty(), "any", -1 );
		assertThat( nonExisting.isEmpty() ).isTrue();
	}
}
