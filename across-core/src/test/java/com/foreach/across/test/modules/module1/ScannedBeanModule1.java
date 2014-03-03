package com.foreach.across.test.modules.module1;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.test.modules.TestContextEventListener;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import com.foreach.across.test.modules.module2.TestModule2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Exposed
@AcrossEventHandler
public class ScannedBeanModule1 extends TestContextEventListener
{
	public static final AtomicInteger CONSTRUCTION_COUNTER = new AtomicInteger();

	private boolean postConstructed = false;

	@Autowired(required = false)
	private TestModule2 referenceToModule2;

	@Autowired(required = false)
	private ScannedBeanModule2 referenceToBeanFromModule2;

	@Value("${module1.beanValue}")
	private String beanValue;

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE_QUALIFIER)
	private AcrossModule currentModule;

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

	public AcrossModule getCurrentModule() {
		return currentModule;
	}
}
