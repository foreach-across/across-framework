package com.foreach.across.test.modules.module1;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Simply here to ensure a target class is in fact an aop proxy.
 */
@Aspect
public class Interceptor
{
	@Around("any()")
	public Object invoke( ProceedingJoinPoint point ) throws Throwable {
		return point.proceed();
	}

	@Pointcut("execution(* ConstructedBeanModule1+.*(..))")
	public void any() {
	}
}
