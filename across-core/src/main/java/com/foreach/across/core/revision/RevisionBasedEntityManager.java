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

import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Base class for revision based interaction of entities.  Supports creating and updating drafts,
 * making drafts final (checkin) and creating draft records based on an existing version (checkout).
 *
 * @author Arne Vandamme
 */
public abstract class RevisionBasedEntityManager<T extends RevisionBasedEntity<T>, U, R extends Revision<U>>
{
	protected static final class RevisionPair<T>
	{
		private T draft, nonDraft;
	}

	private boolean allowRevisionModification;

	protected RevisionBasedEntityManager() {
		this( false );
	}

	/**
	 * The single parameter of this constructor determines if it is allowed to modify entities for a specific
	 * revision.  It is always allowed to modify a draft revision and possibly a special latest revision
	 * (with start and removal revision number of 0).  Modifying a specific revision could impact all future
	 * revisions and can be considered a falsification of the history.  In short, it's usually better
	 * not to allow it in which case an exception will be thrown when trying.
	 *
	 * @param allowRevisionModification True if modifying a specific revision is allowed.
	 */
	protected RevisionBasedEntityManager( boolean allowRevisionModification ) {
		this.allowRevisionModification = allowRevisionModification;
	}

	public boolean isAllowRevisionModification() {
		return allowRevisionModification;
	}

	protected void setAllowRevisionModification( boolean allowRevisionModification ) {
		this.allowRevisionModification = allowRevisionModification;
	}

	/**
	 * Returns the single entity for that revision.
	 */
	@Transactional(readOnly = true)
	public T getEntityForRevision( R revision ) {
		return getEntityForRevision( revision.getRevisionOwner(), revision.getRevisionNumber() );
	}

	@Transactional(readOnly = true)
	public T getEntityForRevision( U owner, int revisionNumber ) {
		Collection<T> list = getEntitiesForRevision( owner, revisionNumber );

		if ( list.size() > 1 ) {
			throw new IllegalStateException( "Data store in illegal state: multiple entities found for object "
					                                 + owner + " and revision " + revisionNumber );
		}

		return list.isEmpty() ? null : list.iterator().next();
	}

	/**
	 * Fetches the most applicable entities for a particular revision.  In case the requested revision is
	 * a draft, either draft or latest checked in version might be returned, but only one of both.
	 */
	@Transactional(readOnly = true)
	public Collection<T> getEntitiesForRevision( R revision ) {
		return getEntitiesForRevision( revision.getRevisionOwner(), revision.getRevisionNumber() );
	}

	/**
	 * Fetches the most applicable entities for a particular revision.  In case the requested revision is
	 * a draft, either draft or latest checked in version might be returned, but only one of both.
	 */
	@Transactional(readOnly = true)
	public Collection<T> getEntitiesForRevision( U owner, int revisionNumber ) {
		Collection<T> candidates = loadEntitiesForRevision( owner, revisionNumber );

		return revisionNumber == Revision.DRAFT ? filterObsoleteEntities( candidates ) : candidates;
	}

	/**
	 * Filters all entities from the collection that have been removed or updated by a corresponding
	 * draft record.  Only the applicable draft records will be kept in that case.
	 */
	protected Collection<T> filterObsoleteEntities( Collection<T> candidates ) {
		List<T> filtered = new ArrayList<>( candidates.size() );
		Map<Object, RevisionPair<T>> pairs = buildRevisionPairs( candidates );

		for ( RevisionPair<T> pair : pairs.values() ) {
			T use = pair.draft != null ? pair.draft : pair.nonDraft;

			if ( !use.isDeleteForRevision() ) {
				filtered.add( use );
			}
		}

		return filtered;
	}

	@Transactional
	public void saveEntityForRevision( T entity, R revision ) {
		saveEntityForRevision( entity, revision.getRevisionOwner(), revision.getRevisionNumber() );
	}

	@Transactional
	public void saveEntityForRevision( T entity, U owner, int revisionNumber ) {
		saveEntitiesForRevision( Collections.singleton( entity ), owner, revisionNumber );
	}

	@Transactional
	public void saveEntitiesForRevision( Collection<T> entities, R revision ) {
		saveEntitiesForRevision( entities, revision.getRevisionOwner(), revision.getRevisionNumber() );
	}

	@Transactional
	public void saveEntitiesForRevision( Collection<T> entities, U owner, int revisionNumber ) {
		Collection<T> currentEntities = loadEntitiesForRevision( owner, revisionNumber );
		Map<Object, RevisionPair<T>> pairs = buildRevisionPairs( currentEntities );

		boolean useDraft = revisionNumber == Revision.DRAFT;

		for ( T entity : entities ) {
			entity.setFirstRevision( revisionNumber );

			if ( revisionNumber != Revision.DRAFT && revisionNumber != Revision.LATEST ) {
				if ( !allowRevisionModification ) {
					throw new RevisionModificationException(
							"Attempt to modify specific revision " + revisionNumber +
									" but this is not allowed by the revision manager" );
				}
				entity.setRemovalRevision( revisionNumber + 1 );
			}
			else {
				entity.setRemovalRevision( revisionNumber );
			}

			RevisionPair<T> pair = pairs.get( entity.getEntityIdentifier() );
			T current = pair != null ? pair.nonDraft : null;
			T draft = pair != null ? pair.draft : null;

			if ( current == null || entity.isDifferentVersionOf( current ) ) {
				if ( useDraft ) {
					if ( draft == null ) {
						// insert this item as a new draft
						insert( entity );
					}
					else if ( entity.isDifferentVersionOf( draft ) ) {
						// update the draft item if different
						update( entity, entity.getFirstRevision(), entity.getRemovalRevision() );
					}
				}
				else if ( current == null ) {
					insert( entity );
				}
				else {
					copyEntityValuesFromExisting( current, entity );
					update( current, current.getFirstRevision(), current.getRemovalRevision() );
				}
			}
			else if ( draft != null ) {
				// remove this draft as the new version is the same as the previous
				delete( draft );
			}

			pairs.remove( entity.getEntityIdentifier() );
		}

		// All pairs left should be removed
		for ( RevisionPair<T> remaining : pairs.values() ) {
			if ( remaining.draft == null ) {
				if ( useDraft ) {
					// new draft that needs to delete the item
					T candidate = createEntityFromExisting( remaining.nonDraft );
					candidate.setFirstRevision( Revision.DRAFT );
					candidate.setRemovalRevision( Revision.DRAFT );
					candidate.setDeleteForRevision( true );

					insert( candidate );
				}
				else {
					// delete the actual property
					delete( remaining.nonDraft );
				}
			}
			else if ( remaining.nonDraft == null ) {
				// draft should be removed
				delete( remaining.draft );
			}
			else if ( !remaining.draft.isDeleteForRevision() ) {
				// update draft for deletion
				remaining.draft.setDeleteForRevision( true );
				update( remaining.draft, remaining.draft.getFirstRevision(), remaining.draft.getRemovalRevision() );
			}
		}
	}

	/**
	 * This call expects a draft revision to be passed in, and will make all draft entities
	 * become active latest entities.
	 */
	@Transactional
	public void checkin( R revisionToCheckin, int newRevisionNumber ) {
		checkin( revisionToCheckin.getRevisionOwner(), revisionToCheckin.getRevisionNumber(), newRevisionNumber );
	}

	/**
	 * This call expects a draft revision to be passed in, and will make all draft entities
	 * become active latest entities.
	 */
	@Transactional
	public void checkin( U owner, int revisionNumberToCheckin, int newRevisionNumber ) {
		checkinEntities( loadEntitiesForRevision( owner, revisionNumberToCheckin ), newRevisionNumber );
	}

	/**
	 * Creates new draft records based on the revision specified.
	 * Any existing draft records will be removed or updated with the values from the specified revision.
	 *
	 * @param revision Revision that should serve as the base for the new draft.
	 * @return Collection of entities for the new draft revision.
	 */
	@Transactional
	public Collection<T> checkout( R revision ) {
		return checkout( revision.getRevisionOwner(), revision.getRevisionNumber() );
	}

	/**
	 * Creates new draft records based on the revision specified.
	 * Any existing draft records will be removed or updated with the values from the specified revision.
	 *
	 * @param owner          Owner of the revision series.
	 * @param revisionNumber Revision that should serve as the base for the new draft.
	 * @return Collection of entities for the new draft revision.
	 */

	@Transactional
	public Collection<T> checkout( U owner, int revisionNumber ) {
		if ( revisionNumber != Revision.DRAFT && revisionNumber != Revision.LATEST ) {
			// A specific base revision was passed in for checkout, let's update the draft first with its values
			Collection<T> revisionEntities = convertToNewDtos( getEntitiesForRevision( owner, revisionNumber ) );
			saveEntitiesForRevision( revisionEntities, owner, Revision.DRAFT );
		}

		return getEntitiesForRevision( owner, Revision.DRAFT );
	}

	/**
	 * Deletes all entities (all revisions) for the owner.
	 */
	@Transactional
	public void deleteEntities( U owner ) {
		deleteAllForOwner( owner );
	}

	/**
	 * Deletes all entities attached to the revision, usually a draft revision.
	 * This will only delete entities existing only in that revision.
	 * Entities starting before or ending after will be left kept.
	 * <p>
	 * This means that even after a delete, you could get results if you ask
	 * for the revision number you just deleted.</p>
	 */
	@Transactional
	public void deleteEntities( R revision ) {
		deleteEntities( revision.getRevisionOwner(), revision.getRevisionNumber() );
	}

	/**
	 * Deletes all entities attached to the revision, usually a draft revision.
	 * This will only delete entities existing only in that revision.
	 * Entities starting before or ending after will be left kept.
	 * <p>
	 * This means that even after a delete, you could get results if you ask
	 * for the revision number you just deleted.</p>
	 */
	@Transactional
	public void deleteEntities( U owner, int revisionNumber ) {
		Collection<T> entities = loadEntitiesForRevision( owner, revisionNumber );

		for ( T entity : entities ) {
			if ( entity.getFirstRevision() == revisionNumber
					&& ( entity.getRemovalRevision() == Revision.DRAFT
					|| entity.getRemovalRevision() == revisionNumber + 1 ) ) {
				delete( entity );
			}
		}
	}

	/**
	 * This checks in all items in the collection: making all drafts existing in the revision
	 * specified, and all active ones stop existing in that revision.
	 */
	protected void checkinEntities( Collection<T> items, int revisionNumber ) {
		Map<Object, RevisionPair<T>> pairs = buildRevisionPairs( items );

		for ( RevisionPair<T> pair : pairs.values() ) {
			if ( pair.nonDraft != null ) {
				// Existing record being updated
				if ( pair.draft != null ) {
					if ( pair.draft.isDeleteForRevision() ) {
						// Expire the non-draft and remove the draft
						expire( pair.nonDraft, revisionNumber );
						delete( pair.draft );
					}
					else if ( pair.draft.isDifferentVersionOf( pair.nonDraft ) ) {
						// If draft and non-draft are different, draft should replace non-draft
						expire( pair.nonDraft, revisionNumber );
						activate( pair.draft, revisionNumber );
					}
					else {
						// There's no point for this draft record
						delete( pair.draft );
					}
				}
				else if ( pair.nonDraft.isDeleteForRevision() ) {
					// If the existing record is deleted, put it on update list for expiring
					expire( pair.nonDraft, revisionNumber );
				}
			}
			else if ( pair.draft != null ) { // New record
				// If it's already deleted again, the record is pointless
				if ( pair.draft.isDeleteForRevision() ) {
					delete( pair.draft );
				}
				else {
					activate( pair.draft, revisionNumber );
				}
			}
		}
	}

	protected Collection<T> loadEntitiesForRevision( U owner, int revisionNumber ) {
		switch ( revisionNumber ) {
			case Revision.DRAFT:
				return getAllForDraftRevision( owner );
			case Revision.LATEST:
				return getAllForLatestRevision( owner );
			default:
				return getAllForSpecificRevision( owner, revisionNumber );
		}
	}

	/**
	 * Lines up draft and non-draft items in a collection.
	 */
	protected Map<Object, RevisionPair<T>> buildRevisionPairs( Collection<T> items ) {
		Map<Object, RevisionPair<T>> map = new HashMap<>();

		// Create the pairs of draft/non-draft instances
		for ( T item : items ) {
			// Get the key of the item
			Object id = item.getEntityIdentifier();

			// Get the revision pair
			RevisionPair<T> pair = map.get( id );

			if ( pair == null ) {
				pair = new RevisionPair<>();
				map.put( id, pair );
			}

			// Assign to the correct element
			if ( item.isDraft() ) {
				pair.draft = item;
			}
			else {
				pair.nonDraft = item;
			}
		}

		return map;
	}

	/**
	 * Makes an instance start in that revision.
	 */
	protected void activate( T entity, int revision ) {
		int previousFirst = entity.getFirstRevision();
		int previousLast = entity.getRemovalRevision();

		entity.setFirstRevision( revision );
		entity.setRemovalRevision( 0 );
		entity.setDeleteForRevision( false );

		update( entity, previousFirst, previousLast );
	}

	/**
	 * Makes an active instance end in this revision.
	 */
	protected void expire( T entity, int revision ) {
		int previousLast = entity.getRemovalRevision();

		entity.setRemovalRevision( revision );
		entity.setDeleteForRevision( false );

		update( entity, entity.getFirstRevision(), previousLast );
	}

	/**
	 * Convert a collection of existing/attached instances to "create" dtos as if they are
	 * entirely new values.  You must override this method to support specific revision checkouts.
	 *
	 * @param entitiesForRevision Collection of entities for a revision.
	 * @return Collection of dtos.
	 */
	protected Collection<T> convertToNewDtos( Collection<T> entitiesForRevision ) {
		throw new UnsupportedOperationException(
				"You must implement RevisionBasedEntityManager.convertToNewDtos() to support revision based checkouts." );
	}

	/**
	 * Inserts a new entity in the data store.
	 *
	 * @param entity New entity instance that should be stored.
	 */
	protected abstract void insert( T entity );

	/**
	 * Updates the entity value with the current first and last revision in the datastore.
	 * The new value, first and last revision are set on the entity instance.
	 *
	 * @param entity               New entity instance that should be stored.
	 * @param currentFirstRevision Currently stored first revision.
	 * @param currentLastRevision  Currently stored last revision.
	 */
	protected abstract void update( T entity, int currentFirstRevision, int currentLastRevision );

	/**
	 * Removes an entity from the data store.
	 *
	 * @param entity Entity instance that should be removed.
	 */
	protected abstract void delete( T entity );

	/**
	 * @return All entities relevant for the current/latest revision.
	 */
	protected abstract Collection<T> getAllForLatestRevision( U owner );

	/**
	 * @return All entities relevant in the specific revision.
	 */
	protected abstract Collection<T> getAllForSpecificRevision( U owner, int revisionNumber );

	/**
	 * @return All entities relevant in the draft revision.
	 */
	protected abstract Collection<T> getAllForDraftRevision( U owner );

	/**
	 * @return A new entity with its version relevant values copied from the existing entity.
	 */
	protected abstract T createEntityFromExisting( T existing );

	/**
	 * Updates the version relevant values of an entity with the values of another entity.
	 */
	protected abstract void copyEntityValuesFromExisting( T entity, T existing );

	/**
	 * Delete all entities across all revisions for the owner.
	 */
	protected abstract void deleteAllForOwner( U owner );
}
