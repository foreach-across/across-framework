package com.foreach.across.core.revision;

/**
 * Interface representing a particular revision of an entity.
 * At any point in time there is always exactly one *latest* revision, and optionally
 * exactly one *draft* revision.  The draft revision is never the latest revision.
 *
 * @author Arne Vandamme
 */
public interface Revision
{
	int DRAFT = -1;
	int LATEST = 0;

	/**
	 * @return The unique revision id within the entire revision series of the entity.
	 */
	int getRevisionId();

	/**
	 * @return True if this is the draft (private working copy) revision of the entity.
	 */
	boolean isDraftRevision();

	/**
	 * @return True if this is the latest revision of the entity.
	 */
	boolean isLatestRevision();
}
