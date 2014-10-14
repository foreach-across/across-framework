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
 * Interface for entities that are related to a specificy {@link com.foreach.across.core.revision.Revision}.
 * A revision is an abstract concept only identified by a revision number, it is not necessary an entity
 * in itself.
 * <p/>
 * A RevisionBasedEntity conceptually exists between one or more revisions.  It has a revision where it starts
 * and a revision where it ends.
 *
 * @author Arne Vandamme
 */
public interface RevisionBasedEntity<T extends RevisionBasedEntity>
{
	/**
	 * The identifier of this entity that is the same across all revisions.
	 * This is used to match the different versions of an entity with each other.
	 *
	 * @return The identifier that will be used to match against existing entities.
	 */
	Object getEntityIdentifier();

	/**
	 * @return First revision this entity was created.
	 */
	int getFirstRevision();

	void setFirstRevision( int revision );

	/**
	 * @return First revision in which this entity was NOT active.
	 */
	int getRemovalRevision();

	void setRemovalRevision( int revision );

	/**
	 * Special property used to determine if a draft entity is the "deleted version" of another
	 * entity.  Meaning that upon the next checkin, the other entity will stop existing.
	 *
	 * @return True if this entity is the deleted version for a currently active one.
	 */
	boolean isDeleteForRevision();

	void setDeleteForRevision( boolean deleted );

	/**
	 * @return True if this entity is a draft, it does not have an official starting revision yet.
	 */
	boolean isDraft();

	/**
	 * Determine if this entity is different from the other entity.  Where the other entity is
	 * expected to have the same identifier, but might be different on values.  If this method
	 * returns true, it means a new version of the entity should be created.
	 * <p/>
	 * <strong>Note:</strong> conceptually not the same as the {@link #equals(Object)} method.
	 *
	 * @return True if this entity is different from the other version of the same entity.
	 */
	boolean isDifferentVersionOf( T other );

}
