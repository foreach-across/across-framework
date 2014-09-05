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
	 * This is used to match existing entities.
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
	int getLastRevision();

	void setLastRevision( int revision );

	/**
	 * @return True if this entity has been deleted (and it is a draft entity).
	 */
	boolean isDeleted();

	void setDeleted( boolean deleted );

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
