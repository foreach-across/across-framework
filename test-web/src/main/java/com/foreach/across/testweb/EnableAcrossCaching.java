package com.foreach.across.testweb;

import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AcrossCachingSelector.class)
public @interface EnableAcrossCaching
{
	/**
		 * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
		 * to standard Java interface-based proxies. The default is {@code false}. <strong>
		 * Applicable only if {@link #mode()} is set to {@link org.springframework.context.annotation.AdviceMode#PROXY}</strong>.
		 *
		 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
		 * Spring-managed beans requiring proxying, not just those marked with
		 * {@code @Cacheable}. For example, other beans marked with Spring's
		 * {@code @Transactional} annotation will be upgraded to subclass proxying at the same
		 * time. This approach has no negative impact in practice unless one is explicitly
		 * expecting one type of proxy vs another, e.g. in tests.
		 */
		boolean proxyTargetClass() default false;

		/**
		 * Indicate how caching advice should be applied. The default is
		 * {@link org.springframework.context.annotation.AdviceMode#PROXY}.
		 * @see org.springframework.context.annotation.AdviceMode
		 */
		AdviceMode mode() default AdviceMode.PROXY;

		/**
		 * Indicate the ordering of the execution of the caching advisor
		 * when multiple advices are applied at a specific joinpoint.
		 * The default is {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE}.
		 */
		int order() default Ordered.LOWEST_PRECEDENCE;
}
