package com.foreach.across.core;

/**
 * Interface that defines the order of an instance within the AcrossModule.  Useful if multiple instances
 * of the same type need to have a specific order within the module, but without influencing the order
 * across the entire context.
 *
 * @author Arne Vandamme
 * @see org.springframework.core.Ordered
 * @see com.foreach.across.core.annotations.OrderInModule
 * @since 1.0.3
 */
public interface OrderedInModule
{
	/**
	 * @return The order in the module.
	 */
	int getOrderInModule();
}
