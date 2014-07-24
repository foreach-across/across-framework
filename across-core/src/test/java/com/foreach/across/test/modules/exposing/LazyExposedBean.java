package com.foreach.across.test.modules.exposing;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
@Exposed
public class LazyExposedBean
{
	private static int creations = 0;

	public static int getCreationCount() {
		return creations;
	}

	public static void reset() {
		creations = 0;
	}

	public LazyExposedBean() {
		creations++;
	}
}
