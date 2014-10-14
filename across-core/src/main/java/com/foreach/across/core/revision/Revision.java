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

package com.foreach.across.core.revision;

/**
 * Interface representing a particular revision of an entity.
 * At any point in time there is always exactly one *latest* revision, and optionally
 * exactly one *draft* revision.  The draft revision is never the latest revision.
 *
 * @author Arne Vandamme
 */
public interface Revision<T>
{
	int DRAFT = -1;
	int LATEST = 0;

	/**
	 * @return Identifier for the owner of the revision series.
	 */
	T getRevisionOwner();

	/**
	 * @return The unique revision id within the entire revision series of the owning entity.
	 */
	int getRevisionNumber();

	/**
	 * @return True if this is the draft (private working copy) revision of the entity.
	 */
	boolean isDraftRevision();

	/**
	 * @return True if this is the latest revision of the entity.
	 */
	boolean isLatestRevision();
}
