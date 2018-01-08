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

package com.foreach.across.core.annotations;

import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.info.AcrossContextInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestAcrossModuleCondition
{
	private AcrossBootstrapConfig contextConfig;

	@Before
	public void setup() {
		contextConfig = mock( AcrossBootstrapConfig.class );
	}

	@Test
	public void emptyAlwaysApplies() {
		assertConditionsMet( allOf(), anyOf() );
	}

	@Test
	public void requiredIsPresent() {
		modules( "moduleOne" );
		assertConditionsMet( allOf( "moduleOne" ), anyOf() );
	}

	@Test
	public void noRequiredIsPresent() {
		modules( "moduleOne" );
		assertConditionsNotMet( allOf( "moduleTwo" ), anyOf() );
	}

	@Test
	public void onlyOneRequiredIsPresent() {
		modules( "moduleOne" );
		assertConditionsNotMet( allOf( "moduleOne", "moduleTwo" ), anyOf() );
	}

	@Test
	public void oneOfTheOptionalsIsPresent() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsMet( allOf(), anyOf( "moduleTwo", "moduleFour" ) );
	}

	@Test
	public void optionalIsPresentButRequiredIsNot() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsNotMet( allOf( "moduleFour" ), anyOf( "moduleTwo" ) );
	}

	@Test
	public void requiredIsPresentButOptionalIsNot() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsNotMet( allOf( "moduleTwo" ), anyOf( "moduleFour" ) );
	}

	@Test
	public void requiredAndOptionalArePresent() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsMet( allOf( "moduleTwo", "moduleThree" ), anyOf( "moduleOne" ) );
	}

	@Test
	public void requiredAndForbiddenArePresent() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsNotMet( allOf( "moduleTwo", "moduleThree" ), anyOf(), noneOf( "moduleOne" ) );
	}

	@Test
	public void requiredAndOptionalAndForbiddenArePresent() {
		modules( "moduleOne", "moduleTwo", "moduleThree", "moduleFour" );
		assertConditionsNotMet( allOf( "moduleTwo", "moduleThree" ), anyOf( "moduleFour" ), noneOf( "moduleOne" ) );
	}

	@Test
	public void requiredAndForbiddenAreNotAndOptionalIsPresent() {
		modules( "moduleFour" );
		assertConditionsMet( allOf(), anyOf( "moduleFour" ), noneOf( "moduleOne" ) );
	}

	@Test
	public void requiredIsNotAndOptionalAndForbiddenArePresent() {
		modules( "moduleOne", "moduleFour" );
		assertConditionsNotMet( allOf(), anyOf( "moduleFour" ), noneOf( "moduleFour" ) );
	}

	@Test
	public void requiredAndOptionalArePresentAndForbiddenIsNot() {
		modules( "moduleTwo", "moduleFour" );
		assertConditionsMet( allOf( "moduleTwo" ), anyOf( "moduleFour" ), noneOf() );
	}

	@Test
	public void classWithEmptyConditionAlwaysApplies() {
		assertConditionOutcome( ClassWithEmptyCondition.class, true );
	}

	@Test
	public void classWithConditionDoesNotApplyIfNotMet() {
		modules( "moduleOne", "moduleThree", "moduleFour" );
		assertConditionOutcome( ClassWithCondition.class, false );

		// Check only the required have been checked (after that the condition failed)
		verify( contextConfig ).hasModule( "moduleOne" );
		verify( contextConfig ).hasModule( "moduleTwo" );
		verify( contextConfig, never() ).hasModule( "moduleThree" );
		verify( contextConfig, never() ).hasModule( "moduleFour" );

		modules( "moduleOne", "moduleTwo" );
		assertConditionOutcome( ClassWithCondition.class, false );
	}

	@Test
	public void classWithConditionAppliesIfMet() {
		modules( "moduleOne", "moduleTwo", "moduleThree", "moduleFour" );
		assertConditionOutcome( ClassWithCondition.class, true );

		// Check both required and the first optional has been checked (after that the condition applied)
		verify( contextConfig ).hasModule( "moduleOne" );
		verify( contextConfig ).hasModule( "moduleTwo" );
		verify( contextConfig ).hasModule( "moduleThree" );
		verify( contextConfig, never() ).hasModule( "moduleFour" );
	}

	@Test
	public void classWithConditionalOnAcrossModuleConditionDoesNotApplyIfNotMet() {
		modules( "moduleOne", "moduleTwo", "moduleThree", "moduleFour", "moduleFive" );
		assertConditionOutcome( ClassWithConditonalOnAcrossModuleCondition.class, false );

		// Check the required and forbidden have been checked (after that the condition failed)
		verify( contextConfig ).hasModule( "moduleOne" );
		verify( contextConfig ).hasModule( "moduleTwo" );
		verify( contextConfig ).hasModule( "moduleFive" );
		verify( contextConfig, never() ).hasModule( "moduleThree" );
		verify( contextConfig, never() ).hasModule( "moduleFour" );
	}

	@Test
	public void classWithConditionalOnAcrossModuleConditionAppliesIfMet() {
		modules( "moduleOne", "moduleTwo", "moduleThree", "moduleFour" );
		assertConditionOutcome( ClassWithConditonalOnAcrossModuleCondition.class, true );

		// Check required, forbidden and the first optional has been checked (after that the condition applied)
		verify( contextConfig ).hasModule( "moduleOne" );
		verify( contextConfig ).hasModule( "moduleTwo" );
		verify( contextConfig ).hasModule( "moduleFive" );
		verify( contextConfig ).hasModule( "moduleThree" );
		verify( contextConfig, never() ).hasModule( "moduleFour" );
	}

	private void assertConditionsMet( String[] required, String[] optional ) {
		assertConditionsMet( required, optional, new String[0] );
	}

	private void assertConditionsMet( String[] allOf, String[] anyOf, String[] noneOf ) {
		assertConditionOutcome( buildAnnotatedTypeMetadata( allOf, anyOf, noneOf ), true );
	}

	private void assertConditionsNotMet( String[] required, String[] optional ) {
		assertConditionsNotMet( required, optional, new String[0] );
	}

	private void assertConditionsNotMet( String[] allOf, String[] anyOf, String[] noneOf ) {
		assertConditionOutcome( buildAnnotatedTypeMetadata( allOf, anyOf, noneOf ), false );
	}

	private AnnotatedTypeMetadata buildAnnotatedTypeMetadata( String[] allOf, String[] anyOf, String[] noneOf ) {
		AnnotatedTypeMetadata metadata = mock( AnnotatedTypeMetadata.class );
		Map<String, Object> attributes = new HashMap<>();
		attributes.put( "allOf", allOf );
		attributes.put( "anyOf", anyOf );
		attributes.put( "noneOf", noneOf );
		when( metadata.getAnnotationAttributes( ConditionalOnAcrossModule.class.getName() ) ).thenReturn( attributes );
		return metadata;
	}

	private void assertConditionOutcome( Class<?> clazz, boolean match ) {
		assertConditionOutcome( new StandardAnnotationMetadata( clazz ), match );
	}

	private void assertConditionOutcome( AnnotatedTypeMetadata metadata, boolean match ) {
		AcrossModuleCondition condition = new AcrossModuleCondition();
		ConditionContext context = mock( ConditionContext.class );
		ConfigurableListableBeanFactory beanFactory = mock( ConfigurableListableBeanFactory.class );
		when( context.getBeanFactory() ).thenReturn( beanFactory );

		AcrossContextInfo contextInfo = mock( AcrossContextInfo.class );
		when( contextInfo.getBootstrapConfiguration() ).thenReturn( contextConfig );
		when( beanFactory.containsLocalBean( AcrossContextInfo.BEAN ) ).thenReturn( true );
		when( beanFactory.getBean( AcrossContextInfo.BEAN, AcrossContextInfo.class ) ).thenReturn( contextInfo );

		assertEquals( match, condition.getMatchOutcome( context, metadata ).isMatch() );
	}

	// Alias method to improve test readability
	private String[] allOf( String... moduleNames ) {
		return moduleNames;
	}

	// Alias method to improve test readability
	private String[] anyOf( String... moduleNames ) {
		return moduleNames;
	}

	// Alias method to improve test readability
	private String[] noneOf( String... moduleNames ) {
		return moduleNames;
	}

	private void modules( String... moduleNames ) {
		reset( contextConfig );

		for ( String moduleName : moduleNames ) {
			when( contextConfig.hasModule( moduleName ) ).thenReturn( true );
		}
	}

	@ConditionalOnAcrossModule(allOf = { "moduleOne", "moduleTwo" }, anyOf = { "moduleThree", "moduleFour" }, noneOf = { "moduleFive" })
	static class ClassWithConditonalOnAcrossModuleCondition
	{

	}

	@AcrossDepends(required = { "moduleOne", "moduleTwo" }, optional = { "moduleThree", "moduleFour" })
	static class ClassWithCondition
	{
	}

	@AcrossDepends
	static class ClassWithEmptyCondition
	{

	}
}
