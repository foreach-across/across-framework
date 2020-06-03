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
package performance.applications;

import com.foreach.across.AcrossApplicationRunner;
import lombok.experimental.UtilityClass;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import performance.applications.simple.SimpleAcrossApplication;
import performance.applications.simple.SimpleBootApplication;
import performance.applications.simpleweb.SimpleWebAcrossApplication;
import performance.applications.simpleweb.SimpleWebBootApplication;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@UtilityClass
public class PerformanceTestApplications
{
	public ConfigurableApplicationContext springBootSimple() {
		SpringApplication application = new SpringApplication( SimpleBootApplication.class );
		application.setAdditionalProfiles( "boot" );
		application.setWebApplicationType( WebApplicationType.NONE );
		return application.run();
	}

	public ConfigurableApplicationContext springBootSimpleAsAcrossApplication() {
		SpringApplication application = new AcrossApplicationRunner( SimpleBootApplication.class );
		application.setAdditionalProfiles( "boot" );
		application.setWebApplicationType( WebApplicationType.NONE );
		return application.run();
	}

	public ConfigurableWebApplicationContext springBootSimpleWeb() {
		SpringApplication application = new SpringApplication( SimpleWebBootApplication.class );
		application.setAdditionalProfiles( "boot" );
		return (ConfigurableWebApplicationContext) application.run();
	}

	public ConfigurableWebApplicationContext springBootSimpleWebAsAcrossApplication() {
		SpringApplication application = new AcrossApplicationRunner( SimpleWebBootApplication.class );
		application.setAdditionalProfiles( "boot" );
		return (ConfigurableWebApplicationContext) application.run();
	}

	public ConfigurableApplicationContext acrossAsSpringBootApplicationSimple() {
		SpringApplication application = new SpringApplication( SimpleAcrossApplication.class );
		application.setAdditionalProfiles( "across" );
		application.setWebApplicationType( WebApplicationType.NONE );
		return application.run();
	}

	public ConfigurableWebApplicationContext acrossAsSpringBootApplicationSimpleWeb() {
		SpringApplication application = new SpringApplication( SimpleWebAcrossApplication.class );
		application.setAdditionalProfiles( "across" );
		return (ConfigurableWebApplicationContext) application.run();
	}

	public ConfigurableApplicationContext acrossApplicationSimple() {
		SpringApplication application = new AcrossApplicationRunner( SimpleAcrossApplication.class );
		application.setAdditionalProfiles( "across" );
		application.setWebApplicationType( WebApplicationType.NONE );
		return application.run();
	}

	public ConfigurableWebApplicationContext acrossApplicationSimpleWeb() {
		SpringApplication application = new AcrossApplicationRunner( SimpleWebAcrossApplication.class );
		application.setAdditionalProfiles( "across" );
		return (ConfigurableWebApplicationContext) application.run();
	}
}
