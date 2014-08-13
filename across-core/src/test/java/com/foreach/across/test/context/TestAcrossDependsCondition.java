package com.foreach.across.test.context;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.conditions.AcrossDependsCondition;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TestAcrossDependsCondition
{
	private AcrossBootstrapConfig contextConfig;

	@Before
	public void setup() {
		contextConfig = mock( AcrossBootstrapConfig.class );
	}

	@Test
	public void emptyAlwaysApplies() {
		assertConditionsMet( required(), optional() );
	}

	@Test
	public void requiredIsPresent() {
		modules( "moduleOne" );
		assertConditionsMet( required( "moduleOne" ), optional() );
	}

	@Test
	public void noRequiredIsPresent() {
		modules( "moduleOne" );
		assertConditionsNotMet( required( "moduleTwo" ), optional() );
	}

	@Test
	public void onlyOneRequiredIsPresent() {
		modules( "moduleOne" );
		assertConditionsNotMet( required( "moduleOne", "moduleTwo" ), optional() );
	}

	@Test
	public void oneOfTheOptionalsIsPresent() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsMet( required(), optional( "moduleTwo", "moduleFour" ) );
	}

	@Test
	public void optionalIsPresentButRequiredIsNot() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsNotMet( required( "moduleFour" ), optional( "moduleTwo" ) );
	}

	@Test
	public void requiredIsPresentButOptionalIsNot() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsNotMet( required( "moduleTwo" ), optional( "moduleFour" ) );
	}

	@Test
	public void requiredAndOptionalArePresent() {
		modules( "moduleOne", "moduleTwo", "moduleThree" );
		assertConditionsMet( required( "moduleTwo", "moduleThree" ), optional( "moduleOne" ) );
	}

	@Test
	public void classWithoutConditionAlwaysApplies() {
		assertTrue( AcrossDependsCondition.applies( contextConfig, ClassWithoutCondition.class ) );
	}

	@Test
	public void classWithEmptyConditionAlwaysApplies() {
		assertTrue( AcrossDependsCondition.applies( contextConfig, ClassWithEmptyCondition.class ) );
	}

	@Test
	public void classWithConditionDoesNotApplyIfNotMet() {
		modules( "moduleOne", "moduleThree", "moduleFour" );
		assertFalse( AcrossDependsCondition.applies( contextConfig, ClassWithCondition.class ) );

		// Check only the required have been checked (after that the condition failed)
		verify( contextConfig ).hasModule( "moduleOne" );
		verify( contextConfig ).hasModule( "moduleTwo" );
		verify( contextConfig, never() ).hasModule( "moduleThree" );
		verify( contextConfig, never() ).hasModule( "moduleFour" );

		modules( "moduleOne", "moduleTwo" );
		assertFalse( AcrossDependsCondition.applies( contextConfig, ClassWithCondition.class ) );
	}

	@Test
	public void classWithConditionAppliesIfMet() {
		modules( "moduleOne", "moduleTwo", "moduleThree", "moduleFour" );
		assertTrue( AcrossDependsCondition.applies( contextConfig, ClassWithCondition.class ) );

		// Check both required and the first optional has been checked (after that the condition applied)
		verify( contextConfig ).hasModule( "moduleOne" );
		verify( contextConfig ).hasModule( "moduleTwo" );
		verify( contextConfig ).hasModule( "moduleThree" );
		verify( contextConfig, never() ).hasModule( "moduleFour" );
	}

	private void assertConditionsMet( String[] required, String[] optional ) {
		assertTrue( AcrossDependsCondition.applies( contextConfig, required, optional ) );
	}

	private void assertConditionsNotMet( String[] required, String[] optional ) {
		assertFalse( AcrossDependsCondition.applies( contextConfig, required, optional ) );
	}

	// Alias method to improve test readability
	private String[] required( String... moduleNames ) {
		return moduleNames;
	}

	// Alias method to improve test readability
	private String[] optional( String... moduleNames ) {
		return moduleNames;
	}

	private void modules( String... moduleNames ) {
		reset( contextConfig );

		for ( String moduleName : moduleNames ) {
			when( contextConfig.hasModule( moduleName ) ).thenReturn( true );
		}
	}

	@AcrossDepends(required = { "moduleOne", "moduleTwo" }, optional = { "moduleThree", "moduleFour" })
	static class ClassWithCondition
	{
	}

	@AcrossDepends
	static class ClassWithEmptyCondition
	{

	}

	static class ClassWithoutCondition
	{

	}

}
