package com.celements.search.lucene;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService;
import com.celements.search.lucene.index.rebuild.LuceneIndexRebuildService.IndexRebuildFuture;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.google.common.collect.ImmutableList;

@Component(LuceneSearchScriptService.NAME)
public class LuceneSearchScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneSearchScriptService.class);

  public static final String NAME = "lucene";

  /**
   * Return value for {@link #rebuildIndex()} meaning that the caller does not have rights.
   */
  public static final int REBUILD_NOT_ALLOWED = -1;

  /**
   * Return value for {@link #rebuildIndex()} meaning that the rebuild is already in progress.
   */
  public static final int REBUILD_ALREADY_IN_PROGRESS = -2;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ILuceneIndexService indexService;

  @Requirement
  private LuceneIndexRebuildService indexRebuildService;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  public LuceneQuery createQuery() {
    return searchService.createQuery();
  }

  public LuceneQuery createQuery(List<String> types) {
    return searchService.createQuery(types);
  }

  /**
   * @deprecated instead use {@link LuceneQuery#copy()}
   * @param query
   * @return
   */
  @Deprecated
  public LuceneQuery createQuery(LuceneQuery query) {
    return query.copy();
  }

  public QueryRestrictionGroup createAndRestrictionGroup() {
    return searchService.createRestrictionGroup(Type.AND);
  }

  public QueryRestrictionGroup createOrRestrictionGroup() {
    return searchService.createRestrictionGroup(Type.OR);
  }

  public QueryRestrictionGroup createAndRestrictionGroup(List<String> fields, List<String> values) {
    return searchService.createRestrictionGroup(Type.AND, fields, values);
  }

  public QueryRestrictionGroup createOrRestrictionGroup(List<String> fields, List<String> values) {
    return searchService.createRestrictionGroup(Type.OR, fields, values);
  }

  public QueryRestriction createRestriction(String field, String value) {
    return searchService.createRestriction(field, value);
  }

  public QueryRestriction createSpaceRestriction(String spaceName) {
    spaceName = spaceName.replace("\"", "");
    SpaceReference spaceRef = null;
    if (StringUtils.isNotBlank(spaceName)) {
      spaceRef = modelUtils.resolveRef(spaceName, SpaceReference.class);
    }
    return searchService.createSpaceRestriction(spaceRef);
  }

  public QueryRestriction createDocRestriction(String fullName) {
    fullName = fullName.replace("\"", "");
    DocumentReference docRef = null;
    if (StringUtils.isNotBlank(fullName)) {
      docRef = modelUtils.resolveRef(fullName, DocumentReference.class);
    }
    return searchService.createDocRestriction(docRef);
  }

  public QueryRestriction createObjectRestriction(String objectName) {
    objectName = objectName.replace("\"", "");
    return searchService.createObjectRestriction(modelUtils.resolveRef(objectName,
        DocumentReference.class));
  }

  public QueryRestriction createObjectFieldRestriction(String objectName, String field,
      String value) {
    objectName = objectName.replace("\"", "");
    return searchService.createFieldRestriction(modelUtils.resolveRef(objectName,
        DocumentReference.class), field, value);
  }

  public QueryRestriction createRangeRestriction(String field, String from, String to) {
    return createRangeRestriction(field, from, to, true);
  }

  public QueryRestriction createOjbectFieldRangeRestriction(String objectName, String field,
      String from, String to, boolean inclusive) {
    return createRangeRestriction(objectName + "." + field, from, to, inclusive);
  }

  public QueryRestriction createRangeRestriction(String field, String from, String to,
      boolean inclusive) {
    return searchService.createRangeRestriction(field, from, to, inclusive);
  }

  public QueryRestriction createDateRestriction(String field, Date date) {
    return searchService.createDateRestriction(field, date);
  }

  public QueryRestriction createFromDateRestriction(String field, Date fromDate,
      boolean inclusive) {
    return searchService.createFromDateRestriction(field, fromDate, inclusive);
  }

  public QueryRestriction createToDateRestriction(String field, Date toDate, boolean inclusive) {
    return searchService.createToDateRestriction(field, toDate, inclusive);
  }

  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, Date toDate,
      boolean inclusive) {
    return searchService.createFromToDateRestriction(field, fromDate, toDate, inclusive);
  }

  public QueryRestriction createNumberRestriction(String field, Number number) {
    return searchService.createNumberRestriction(field, number);
  }

  public QueryRestriction createFromToNumberRestriction(String field, Number fromNumber,
      Number toNumber, boolean inclusive) {
    return searchService.createFromToNumberRestriction(field, fromNumber, toNumber, inclusive);
  }

  public QueryRestrictionGroup createAttachmentRestrictionGroup(List<String> mimeTypes,
      List<String> mimeTypesBlackList, List<String> filenamePrefs) {
    return searchService.createAttachmentRestrictionGroup(mimeTypes, mimeTypesBlackList,
        filenamePrefs);
  }

  public LuceneSearchResult search(LuceneQuery query) {
    return searchService.search(query, null, null);
  }

  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields) {
    return searchService.search(query, sortFields, null);
  }

  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields,
      List<String> languages) {
    return searchService.search(query, sortFields, languages);
  }

  public LuceneSearchResult search(String queryString) {
    return searchService.search(queryString, null, null);
  }

  public LuceneSearchResult search(String queryString, List<String> sortFields) {
    return searchService.search(queryString, sortFields, null);
  }

  public LuceneSearchResult search(String queryString, List<String> sortFields,
      List<String> languages) {
    return searchService.search(queryString, sortFields, languages);
  }

  public int getResultLimit() {
    return searchService.getResultLimit();
  }

  public int getResultLimit(boolean skipChecks) {
    return searchService.getResultLimit(skipChecks);
  }

  public long getIndexSize() {
    if (rightsAccess.isAdmin()) {
      return indexService.getIndexSize();
    }
    return 0;
  }

  public boolean queueIndexing(EntityReference ref) {
    boolean ret = false;
    if (rightsAccess.isAdmin()) {
      indexService.queue(ref);
      ret = true;
    }
    return ret;
  }

  public long getQueueSize() {
    if (rightsAccess.isLoggedIn()) {
      return indexService.getQueueSize();
    }
    return 0;
  }

  public IndexRebuildFuture getRunningIndexRebuild() {
    if (rightsAccess.isAdmin()) {
      return indexRebuildService.getRunningRebuild().orElse(null);
    }
    return null;
  }

  public List<IndexRebuildFuture> getIndexRebuilds() {
    if (rightsAccess.isAdmin()) {
      return indexRebuildService.getQueuedRebuilds();
    }
    return ImmutableList.of();
  }

  public void pauseIndexRebuilder(Duration duration) {
    if (rightsAccess.isSuperAdmin()) {
      indexRebuildService.pause(duration);
    }
  }

  public Optional<Instant> isIndexRebuilderPaused() {
    if (rightsAccess.isAdmin()) {
      return indexRebuildService.isPaused();
    }
    return Optional.empty();
  }

  public void unpauseIndexRebuilder() {
    if (rightsAccess.isSuperAdmin()) {
      indexRebuildService.unpause();
    }
  }

  public int rebuildIndex() {
    return rebuildIndex(null);
  }

  public int rebuildIndex(EntityReference entityRef) {
    return guardIndex(() -> indexService.rebuildIndex(entityRef));
  }

  public int rebuildIndexForAllWikis() {
    return guardIndex(() -> indexService.rebuildIndexForAllWikis());
  }

  public int rebuildIndexWithWipe() {
    return REBUILD_NOT_ALLOWED;
  }

  public int rebuildIndexForWikiBySpace(WikiReference wikiRef) {
    return guardIndex(() -> indexService.rebuildIndexForWikiBySpace(wikiRef));
  }

  public int rebuildIndexForAllWikisBySpace() {
    return guardIndex(() -> indexService.rebuildIndexForAllWikisBySpace());
  }

  public boolean optimizeIndex() {
    return guardIndex(() -> indexService.optimizeIndex()) == 0;
  }

  private int guardIndex(Runnable runnable) {
    int ret;
    if (!rightsAccess.isSuperAdmin()) {
      ret = REBUILD_NOT_ALLOWED;
    } else {
      runnable.run();
      ret = 0;
    }
    return ret;
  }

}
