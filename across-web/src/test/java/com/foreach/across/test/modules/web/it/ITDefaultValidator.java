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
package com.foreach.across.test.modules.web.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.mvc.ValidatorDelegate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITDefaultValidator.Config.class)
public class ITDefaultValidator
{
	@Autowired
	private Validator validator;

	@Test
	public void validatorShouldBeCorrectlyInitializedWithHibernateValidator() {
		assertNotNull( validator );
		ValidatorDelegate delegate = (ValidatorDelegate) validator;

		Validator implementation = delegate.getImplementation();
		assertNotNull( implementation );

		assertTrue( implementation instanceof OptionalValidatorFactoryBean );
		assertNotNull( ( (OptionalValidatorFactoryBean) implementation ).getValidator() );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config extends WebMvcConfigurerAdapter implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
		}
	}
}
