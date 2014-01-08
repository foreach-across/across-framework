package com.foreach.across.test.modules.module1;

import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import com.foreach.across.test.modules.module2.TestModule2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ScannedBeanModule1
{
	public static final AtomicInteger CONSTRUCTION_COUNTER = new AtomicInteger();

	private boolean postConstructed = false;

	@Autowired(required = false)
	private TestModule2 referenceToModule2;

	@Autowired(required = false)
	private ScannedBeanModule2 referenceToBeanFromModule2;

	@Value("${module1.beanValue}")
	private String beanValue;

	public TestModule2 getReferenceToModule2() {
		return referenceToModule2;
	}

	public ScannedBeanModule2 getReferenceToBeanFromModule2() {
		return referenceToBeanFromModule2;
	}

	public boolean isPostConstructed() {
		return postConstructed;
	}

	public String getBeanValue() {
		return beanValue;
	}

	public ScannedBeanModule1() {
		CONSTRUCTION_COUNTER.incrementAndGet();
	}

	@PostConstruct
	public void postConstruct() {
		postConstructed = true;
		CONSTRUCTION_COUNTER.incrementAndGet();
	}
}
