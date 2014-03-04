package com.foreach.across.test;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.BootstrapAcrossModuleOrder;
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

	@Test
	public void resetEnabled() {
		infrastructureRequiringTwo.setEnabled( true );
	}

	@Test
	public void addingOrderIsKeptIfNoDependencies() {
		Collection<AcrossModule> added = list( one, two, three );
		Collection<AcrossModule> ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( added, ordered );
	}

	@Test
	public void addingOrderIsKeptBetweenDependencies() {
		Collection<AcrossModule> added = list( one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( list( one, two, requiresTwo, three ), ordered );

		// No change to adding order since dependency is met in the adding order
		added = list( one, two, three, requiresTwo );
		ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( list( one, two, three, requiresTwo ), ordered );
	}

	@Test
	public void optionalAndRequiredMixed() {
		Collection<AcrossModule> added = list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );
	}

	@Test
	public void disabledOptionalDependencyHasNoImpact() {
		one.setEnabled( false );

		Collection<AcrossModule> added = list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );
	}

	@Test(expected = RuntimeException.class)
	public void disabledRequiredDependencyCausesException() {
		two.setEnabled( false );

		Collection<AcrossModule> added = list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three );
		Collection<AcrossModule> ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );
	}

	@Test
	public void infrastructureModuleGetsPushedToTheFirstPossibleSpot() {
		Collection<AcrossModule> added =
				list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three, infrastructureRequiringTwo );
		Collection<AcrossModule> ordered = BootstrapAcrossModuleOrder.create( added );

		assertEquals( list( two, infrastructureRequiringTwo, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ),
		              ordered );
	}

	@Test
	public void disablingInfrastructureModuleIsNoProblem() {
		Collection<AcrossModule> added =
				list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three, infrastructureRequiringTwo );
		infrastructureRequiringTwo.setEnabled( false );

		BootstrapAcrossModuleOrder bootstrapAcrossModuleOrder = new BootstrapAcrossModuleOrder( added, true );
		Collection<AcrossModule> ordered = bootstrapAcrossModuleOrder.getOrderedModules();

		assertEquals( list( two, three, one, requiresTwoThreeAndOptionalOne, requiresTwo ), ordered );

		Collection<AcrossModule> dependencies = bootstrapAcrossModuleOrder.getRequiredDependencies( one );
		assertFalse( dependencies.contains( infrastructureRequiringTwo ) );
	}

	@Test(expected = RuntimeException.class)
	public void missingRequiredDependencyWillBreak() {
		Collection<AcrossModule> added = list( one, requiresTwo );
		BootstrapAcrossModuleOrder.create( added );
	}

	@Test(expected = RuntimeException.class)
	public void cyclicDependencyWillBreak() {
		Collection<AcrossModule> added =
				list( requiresTwoThreeAndOptionalOne, one, requiresTwo, two, three, cyclicOne, cyclicTwo );
		BootstrapAcrossModuleOrder.create( added );
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
}
