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
package com.foreach.across.core.context;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertSame;

class TestAcrossListableBeanFactory
{
	@Test
	void acrossListableBeanFactoryShouldSerialize() {
		DefaultListableBeanFactory beanFactory = new AcrossListableBeanFactory();
		Comparator comparator = beanFactory.getDependencyComparator();
		beanFactory.setSerializationId( RandomStringUtils.randomAlphanumeric( 100 ) );
		byte[] object = SerializationUtils.serialize( beanFactory );
		AcrossListableBeanFactory deserialized = SerializationUtils.deserialize( object );
		assertSame( comparator, deserialized.getDependencyComparator() );
	}
}
