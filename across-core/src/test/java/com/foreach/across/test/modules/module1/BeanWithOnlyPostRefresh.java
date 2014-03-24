package com.foreach.across.test.modules.module1;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.PostRefresh;
import org.springframework.stereotype.Component;

@Component
@Exposed
public class BeanWithOnlyPostRefresh
{
	public boolean refreshed = false;

	public boolean isRefreshed() {
		return refreshed;
	}

	@PostRefresh
	public void refresh() {
		refreshed = true;
	}
}
