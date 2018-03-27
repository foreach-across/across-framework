/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.modules.exposing;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.stereotype.Component;

/**
 * Bean with a custom hashCode() and equals() implementation that will
 * always return the same value for every instance of this class.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Component
@Exposed
public final class EqualBean
{
	@Override
	public int hashCode() {
		return EqualBean.class.hashCode();
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) {
			return false;
		}
		return EqualBean.class.equals( obj.getClass() );
	}
}
