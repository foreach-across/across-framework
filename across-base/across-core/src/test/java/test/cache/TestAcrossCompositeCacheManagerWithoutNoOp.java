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
package test.cache;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author niels
 * @since 20/10/2014
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestAcrossCompositeCacheManagerWithoutNoOp.Config.class)
public class TestAcrossCompositeCacheManagerWithoutNoOp
{
	@Autowired
	private CacheManager cacheManager;

	@Test
	public void defaultCompositeCacheManagerShouldHaveBeenInitializedWithNoOp() {
		assertNotNull( cacheManager );
		Cache cache = cacheManager.getCache( "notASingleDummyCacheShouldWork" );
		assertNull( cache );
	}

	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
			Map<String, AcrossContextConfigurer> configurerMap =
					applicationContext.getBeansOfType( AcrossContextConfigurer.class );

			AcrossContext context = new AcrossContext( applicationContext );
			context.setInstallerAction( InstallerAction.DISABLED );

			for ( AcrossContextConfigurer configurer : configurerMap.values() ) {
				configurer.configure( context );
			}

			context.bootstrap();

			return context;
		}

		@Override
		public void configure( AcrossContext context ) {
			context.setDisableNoOpCacheManager( true );
		}
	}
}
