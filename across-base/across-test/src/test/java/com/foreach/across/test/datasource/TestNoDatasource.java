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
package com.foreach.across.test.datasource;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.support.AcrossTestBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestNoDatasource
{
	@Test
	public void noDataSourceBeanDefinitionsShouldBeFoundIfThereIsNoDataSource() {
		try (AcrossTestContext ctx = AcrossTestBuilders.web( false ).modules( AcrossWebModule.NAME ).build()) {
			ApplicationContext moduleContext = ctx.getContextInfo().getModuleInfo( AcrossWebModule.NAME ).getApplicationContext();
			assertThat( moduleContext.getBeanNamesForType( DataSource.class ) ).isEmpty();

			ApplicationContext acrossContext = ctx.getContextInfo().getApplicationContext();
			assertThat( acrossContext.getBeanNamesForType( DataSource.class ) ).isEmpty();
		}
	}
}
