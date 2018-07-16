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
package com.foreach.across.test.support.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

@Order(Ordered.HIGHEST_PRECEDENCE + 10000)
@Configuration
@Slf4j
@ConditionalOnProperty(name = "acrossTest.dbUtil.active", havingValue = "true", matchIfMissing = false)
public class TestDataSourcePostProcessor implements AcrossContextConfigurer
{

	@Override
	public void configure( AcrossContext context ) {
		LOG.info( "Postprocessing configured datasource " );
		DataSource dataSource = context.getDataSource();
		if ( dataSource != null ) {
			context.setDataSource( new ProxyTestDataSource( dataSource ) );
		}
	}

}
