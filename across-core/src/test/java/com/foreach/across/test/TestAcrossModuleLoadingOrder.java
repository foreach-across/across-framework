package com.foreach.across.test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapOrderBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestAcrossModuleLoadingOrder
{
	private ModuleOne one = new ModuleOne();
	private ModuleTwo two = new ModuleTwo();
	private ModuleThree three = new ModuleThree();
	private ModuleFour requiresTwo = new ModuleFour();
	private ModuleFive requiresTwoThreeAndOptionalOne = new ModuleFive();
	private ModuleSix cyclicOne = new ModuleSix();
	private ModuleSeven cyclicTwo = new ModuleSeven();
	private ModuleEight infrastructureRequiringTwo = new ModuleEight();
	private ModuleNine requiresTwoAndOptionalThreeTen = new ModuleNine();
	private ModuleTen requiresOneAndOptionalTwoNine = new ModuleTen();

	@Test
	public void resetEnabled() {
		infrastructureRequiringTwo.setEnabled( true );
	}

	@Test
	public void addingOrderIsKeptIfNoDependencies() {
		Collection<AcrossModule> added = list( one, two, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( added, ordered );
	}

	@Test
	public void runtimeDependencyInfluencesOrder() {
		ModuleTwo runtime = new ModuleTwo();

		Collection<AcrossModule> added = list( one, runtime, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( added, ordered );

		runtime.addRuntimeDependency( three.getName() );

		ordered = order( added );
		assertEquals( list( one, three, runtime ), ordered );
	}

	@Test
	public void addingOrderIsKeptBetweenDependencies() {
		Collection<AcrossModule> added = list( one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( one, two, requiresTwo, three ), ordered );

		// No change to adding order since dependency is met in the adding order
		added = list( one, two, three, requiresTwo );
		ordered = order( added );

		assertEquals( list( one, two, three, requiresTwo ), ordered );
	}

	@Test
	public void optionalAndRequiredMixed() {
		Collection<AcrossModule> added = list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );
	}

	@Test
	public void disabledOptionalDependencyHasNoImpact() {
		one.setEnabled( false );

		Collection<AcrossModule> added = list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );
	}

	@Test(expected = RuntimeException.class)
	public void disabledRequiredDependencyCausesException() {
		two.setEnabled( false );

		Collection<AcrossModule> added = list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );
	}

	@Test
	public void infrastructureModuleGetsPushedToTheFirstPossibleSpot() {
		Collection<AcrossModule> added =
				list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three, infrastructureRequiringTwo );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( two, infrastructureRequiringTwo, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ),
		              ordered );
	}

	@Test
	public void disablingInfrastructureModuleIsNoProblem() {
		Collection<AcrossModule> added =
				list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three, infrastructureRequiringTwo );
		infrastructureRequiringTwo.setEnabled( false );

		ModuleBootstrapOrderBuilder moduleBootstrapOrderBuilder = new ModuleBootstrapOrderBuilder( added );
		Collection<AcrossModule> ordered = moduleBootstrapOrderBuilder.getOrderedModules();

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo, infrastructureRequiringTwo ),
		              ordered );

		Collection<AcrossModule> dependencies = moduleBootstrapOrderBuilder.getRequiredDependencies( one );
		assertFalse( dependencies.contains( infrastructureRequiringTwo ) );
	}

	@Test(expected = RuntimeException.class)
	public void missingRequiredDependencyWillBreak() {
		Collection<AcrossModule> added = list( one, requiresTwo );
		order( added );
	}

	@Test(expected = RuntimeException.class)
	public void cyclicDependencyWillBreak() {
		Collection<AcrossModule> added =
				list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three, cyclicOne, cyclicTwo );
		order( added );
	}

	@Test
	public void cyclicOptionalDependencyShouldWork() {
		Collection<AcrossModule> added =
				list( requiresTwoAndOptionalThreeTen, requiresOneAndOptionalTwoNine, one, two, three );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( two, three, requiresTwoAndOptionalThreeTen, one, requiresOneAndOptionalTwoNine ), ordered );
	}

	@Test(expected = RuntimeException.class)
	public void missingRuntimeDependencyWillBreak() {
		ModuleTwo twoWithRuntimeDependency = new ModuleTwo();
		twoWithRuntimeDependency.addRuntimeDependency( three.getName() );

		Collection<AcrossModule> added = list( one, twoWithRuntimeDependency );
		order( added );
	}

	@Test
	public void emptyAcrossModuleForDependencySatisfying() {
		EmptyAcrossModule fakeTwo = new EmptyAcrossModule( two.getName() );

		Collection<AcrossModule> added = list( one, requiresTwo, fakeTwo );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( one, fakeTwo, requiresTwo ), ordered );
	}

	@Test
	public void complexRuntimeDependencies() {
		EmptyAcrossModule one = new EmptyAcrossModule( "one" );

		EmptyAcrossModule two = new EmptyAcrossModule( "two" );
		two.addRuntimeDependency( "five" );

		EmptyAcrossModule three = new EmptyAcrossModule( "three" );
		three.addRuntimeDependency( "one" );
		three.addRuntimeDependency( "six" );

		EmptyAcrossModule four = new EmptyAcrossModule( "four" );
		four.addRuntimeDependency( "three" );

		EmptyAcrossModule five = new EmptyAcrossModule( "five" );
		five.addRuntimeDependency( "three" );
		five.addRuntimeDependency( "four" );

		EmptyAcrossModule six = new EmptyAcrossModule( "six" );
		six.addRuntimeDependency( "one" );

		Collection<AcrossModule> added = list( one, two, three, four, five, six );
		Collection<AcrossModule> ordered = order( added );

		assertEquals( list( one, six, three, four, five, two ), ordered );
	}

	private Collection<AcrossModule> order( Collection<AcrossModule> list ) {
		return new ModuleBootstrapOrderBuilder( list ).getOrderedModules();
	}

	private Collection<AcrossModule> list( AcrossModule... modules ) {
		return Arrays.asList( modules );
	}

	public static class ModuleOne extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleOne";
		}

		@Override
		public String getDescription() {
			return null;
		}
	}

	public static class ModuleTwo extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleTwo";
		}
	}

	public static class ModuleThree extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleThree";
		}
	}

	@AcrossDepends(required = "ModuleTwo")
	public static class ModuleFour extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleFour";
		}
	}

	@AcrossDepends(required = { "ModuleTwo", "ModuleThree" }, optional = { "ModuleOne", "NonExistingModule" })
	public static class ModuleFive extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleFive";
		}
	}

	@AcrossDepends(required = "ModuleSeven")
	public static class ModuleSix extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleSix";
		}
	}

	@AcrossDepends(required = "ModuleSix")
	public static class ModuleSeven extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleSeven";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(required = "ModuleTwo")
	public static class ModuleEight extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleEight";
		}
	}

	@AcrossDepends(required = "ModuleTwo", optional = { "ModuleThree", "ModuleTen" })
	public static class ModuleNine extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleNine";
		}
	}

	@AcrossDepends(required = "ModuleOne", optional = { "ModuleTwo", "ModuleNine" })
	public static class ModuleTen extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleTen";
		}
	}
}

