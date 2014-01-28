package com.foreach.across.test.modules.module2;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.test.modules.TestContextEventListener;
import com.foreach.across.test.modules.module1.ScannedBeanModule1;
import com.foreach.across.test.modules.module1.TestModule1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AcrossEventHandler
public class ScannedBeanModule2 extends TestContextEventListener
{
	public static final AtomicInteger CONSTRUCTION_COUNTER = new AtomicInteger();

	@Autowired(required = false)
	private TestModule1 referenceToModule1;

	@Autowired(required = false)
	private ScannedBeanModule1 referenceToBeanFromModule1;

	public TestModule1 getReferenceToModule1() {
		return referenceToModule1;
	}

	public ScannedBeanModule1 getReferenceToBeanFromModule1() {
		return referenceToBeanFromModule1;
	}

	public ScannedBeanModule2() {
		CONSTRUCTION_COUNTER.incrementAndGet();
	}

	@PostConstruct
	public void postConstruct() {
		CONSTRUCTION_COUNTER.incrementAndGet();
	}
}
