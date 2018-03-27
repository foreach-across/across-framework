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
package test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapOrderBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestAcrossModuleLoadingOrderTwo
{
	private WebModule web = new WebModule();
	private EntityModule entity = new EntityModule();
	private JpaModule jpa = new JpaModule();
	private TestDataModule test = new TestDataModule();
	private AdminWebModule adminWeb = new AdminWebModule();
	private SecurityModule security = new SecurityModule();
	private BootstrapUiModule bootstrapUiModule = new BootstrapUiModule();
	private SecurityAclModule securityAcl = new SecurityAclModule();

	@Test
	public void combinedDependencies() {
		Collection<AcrossModule> added
				= list( web, securityAcl, entity, jpa, test, adminWeb, bootstrapUiModule, security );
		Collection<AcrossModule> ordered = order( added );

		assertEquals(
				list( jpa, securityAcl, web, bootstrapUiModule, adminWeb, entity, test, security ),
				ordered
		);
	}

	private Collection<AcrossModule> order( Collection<AcrossModule> list ) {
		ModuleBootstrapOrderBuilder bootstrapOrderBuilder = new ModuleBootstrapOrderBuilder();
		bootstrapOrderBuilder.setSourceModules( list );
		return bootstrapOrderBuilder.getOrderedModules();
	}

	private Collection<AcrossModule> list( AcrossModule... modules ) {
		return Arrays.asList( modules );
	}

	public class WebModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "WebModule";
		}

		@Override
		public String getDescription() {
			return null;
		}
	}

	@AcrossDepends(optional = { "WebModule", "AdminWebModule" })
	public class EntityModule extends WebModule
	{
		@Override
		public String getName() {
			return "EntityModule";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	public class JpaModule extends WebModule
	{
		@Override
		public String getName() {
			return "JpaModule";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(optional = "JpaModule")
	public class SecurityAclModule extends WebModule
	{
		@Override
		public String getName() {
			return "SecurityAclModule";
		}
	}

	@AcrossDepends(required = "JpaModule")
	public class TestDataModule extends WebModule
	{
		@Override
		public String getName() {
			return "TestDataModule";
		}
	}

	@AcrossDepends(required = { "WebModule", "SecurityModule", "BootstrapUiModule" })
	public class AdminWebModule extends WebModule
	{
		@Override
		public String getName() {
			return "AdminWebModule";
		}
	}

	@AcrossRole(AcrossModuleRole.POSTPROCESSOR)
	public class SecurityModule extends WebModule
	{
		@Override
		public String getName() {
			return "SecurityModule";
		}
	}

	@AcrossDepends(required = "WebModule")
	public class BootstrapUiModule extends WebModule
	{
		@Override
		public String getName() {
			return "BootstrapUiModule";
		}
	}
}
