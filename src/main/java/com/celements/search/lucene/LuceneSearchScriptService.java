package com.celements.search.lucene;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.exception.DocumentAccessException;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;

@Component(LuceneSearchScriptService.NAME)
public class LuceneSearchScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      LuceneSearchScriptService.class);

  public static final String NAME = "lucene";

  /**
   * Return value for {@link #rebuildIndex()} meaning that the caller does not have rights.
   */
  public static final int REBUILD_NOT_ALLOWED = -1;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ILuceneIndexService indexService;

  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  public LuceneQuery createQuery() {
    return searchService.createQuery();
  }

  public LuceneQuery createQuery(List<String> types) {
    return searchService.createQuery(types);
  }

  /**
   * @deprecated instead use {@link LuceneQuery#copy()}
   * 
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

  public QueryRestrictionGroup createAndRestrictionGroup(List<String> fields,
      List<String> values) {
    return searchService.createRestrictionGroup(Type.AND, fields, values);
  }

  public QueryRestrictionGroup createOrRestrictionGroup(List<String> fields,
      List<String> values) {
    return searchService.createRestrictionGroup(Type.OR, fields, values);
  }

  public QueryRestriction createRestriction(String field, String value) {
    return searchService.createRestriction(field, value);
  }

  public QueryRestriction createSpaceRestriction(String spaceName) {
    spaceName = spaceName.replace("\"", "");
    SpaceReference spaceRef = null;
    if (StringUtils.isNotBlank(spaceName)) {
      spaceRef = webUtilsService.resolveSpaceReference(spaceName);
    }
    return searchService.createSpaceRestriction(spaceRef);
  }

  public QueryRestriction createDocRestriction(String fullName) {
    fullName = fullName.replace("\"", "");
    DocumentReference docRef = null;
    if (StringUtils.isNotBlank(fullName)) {
      docRef = webUtilsService.resolveDocumentReference(fullName);
    }
    return searchService.createDocRestriction(docRef);
  }

  public QueryRestriction createObjectRestriction(String objectName) {
    objectName = objectName.replace("\"", "");
    return searchService.createObjectRestriction(webUtilsService.resolveDocumentReference(
        objectName));
  }

  public QueryRestriction createObjectFieldRestriction(String objectName, String field,
      String value) {
    objectName = objectName.replace("\"", "");
    return searchService.createFieldRestriction(webUtilsService.resolveDocumentReference(
        objectName), field, value);
  }

  public QueryRestriction createRangeRestriction(String field, String from, String to) {
    return createRangeRestriction(field, from, to, true);
  }

  public QueryRestriction createOjbectFieldRangeRestriction(String objectName,
      String field, String from, String to, boolean inclusive) {
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

  public QueryRestriction createToDateRestriction(String field, Date toDate,
      boolean inclusive) {
    return searchService.createToDateRestriction(field, toDate, inclusive);
  }

  public QueryRestriction createFromToDateRestriction(String field, Date fromDate,
      Date toDate, boolean inclusive) {
    return searchService.createFromToDateRestriction(field, fromDate, toDate, inclusive);
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

  public void queueIndexing(DocumentReference docRef) {
    try {
      indexService.queueForIndexing(docRef);
    } catch (DocumentAccessException dae) {
      LOGGER.error("Failed to access doc '{}'", docRef, dae);
    }
  }
  
  public int rebuildIndex() {
    return rebuildIndex("");
  }
  
  public int rebuildIndex(String hqlFilter) {
    int ret;
    if (webUtilsService.isAdminUser()) {
      ret = indexService.rebuildIndex(Arrays.asList(webUtilsService.getWikiRef()),
          hqlFilter);
    } else {
      ret = REBUILD_NOT_ALLOWED;
    }
    return ret;
  }
  
  public int rebuildIndexForAllWikis() {
    int ret;
    if (webUtilsService.isSuperAdminUser()) {
      ret = indexService.rebuildIndexForAllWikis();
    } else {
      ret = REBUILD_NOT_ALLOWED;
    }
    return ret;
  }

}
