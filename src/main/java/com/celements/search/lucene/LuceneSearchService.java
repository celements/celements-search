package com.celements.search.lucene;

import static com.celements.search.lucene.LuceneUtils.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.lucene.query.QueryRestrictionString;
import com.celements.search.web.WebSearchQueryBuilder;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

@Component
public class LuceneSearchService implements ILuceneSearchService {

  // yyyyMMddHHmm
  public static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{12}|\\d{1,12}\\*)$");

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneSearchService.class);

  private static final boolean DEFAULT_TOKENIZE = true;
  private static final boolean DEFAULT_FUZZY = false;

  @Requirement
  private ILuceneIndexService luceneIndexService;

  @Requirement
  private ConfigurationSource cfgSrc;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private ModelContext context;

  @Override
  public DateFormat getSDF() {
    return new SimpleDateFormat("yyyyMMddHHmm");
  }

  @Override
  public Version getVersion() {
    Version defaultVersion = LucenePlugin.VERSION;
    try {
      return Version.valueOf(cfgSrc.getProperty("lucene.version", defaultVersion.toString()));
    } catch (IllegalArgumentException exc) {
      LOGGER.warn("invalid version defined", exc);
      return defaultVersion;
    }
  }

  @Override
  public LuceneQuery createQuery() {
    return new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
  }

  @Override
  public LuceneQuery createQuery(List<String> types) {
    if ((types == null) || types.isEmpty()) {
      types = Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE, LucenePlugin.DOCTYPE_ATTACHMENT);
    }
    return new LuceneQuery(types);
  }

  @Override
  public QueryRestrictionGroup createRestrictionGroup(Type type) {
    return new QueryRestrictionGroup(type);
  }

  @Override
  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields,
      List<String> values) {
    return createRestrictionGroup(type, fields, values, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  @Override
  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields,
      List<String> values, boolean tokenize, boolean fuzzy) {
    QueryRestrictionGroup restrGrp = createRestrictionGroup(type);
    Iterator<String> fieldIter = getIter(fields);
    Iterator<String> valueIter = getIter(values);
    String field = null;
    String value = null;
    while (fieldIter.hasNext() || valueIter.hasNext()) {
      if (fieldIter.hasNext()) {
        field = fieldIter.next();
      }
      if (valueIter.hasNext()) {
        value = valueIter.next();
      }
      QueryRestriction restr = createRestriction(field, value, tokenize, fuzzy);
      if (restr != null) {
        restrGrp.add(restr);
      }
    }
    return restrGrp;
  }

  private Iterator<String> getIter(List<String> list) {
    if (list != null) {
      return list.iterator();
    } else {
      return Collections.<String>emptyList().iterator();
    }
  }

  @Override
  public QueryRestrictionString createRestriction(String query) throws ParseException {
    return new QueryRestrictionString(new QueryParser(getVersion(), "", new StandardAnalyzer(
        getVersion())).parse(query).toString());
  }

  @Override
  public QueryRestriction createRestriction(String field, String value) {
    return createRestriction(field, value, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  @Override
  public QueryRestriction createRestriction(String field, String value, boolean tokenize) {
    return createRestriction(field, value, tokenize, DEFAULT_FUZZY);
  }

  @Override
  public QueryRestriction createRestriction(String field, String value, boolean tokenize,
      boolean fuzzy) {
    QueryRestriction restriction = new QueryRestriction(field, value, tokenize);
    return fuzzy ? restriction.setFuzzy() : restriction;
  }

  @Override
  public QueryRestriction createDocTypeRestriction(LuceneDocType docType) {
    return createRestriction(IndexFields.DOCUMENT_TYPE, exactify(docType.key));
  }

  @Override
  public QueryRestriction createSpaceRestriction(SpaceReference spaceRef) {
    return createRestriction(IndexFields.DOCUMENT_SPACE, exactify(spaceRef));
  }

  @Override
  public QueryRestriction createDocRestriction(DocumentReference docRef) {
    return createRestriction(IndexFields.DOCUMENT_FULLNAME, exactify(docRef));
  }

  @Override
  @Deprecated
  public QueryRestriction createObjectRestriction(DocumentReference classRef) {
    return createObjectRestriction(new ClassReference(classRef));
  }

  @Override
  public QueryRestriction createObjectRestriction(ClassReference classRef) {
    QueryRestriction restriction = null;
    if (classRef != null) {
      restriction = createRestriction(IndexFields.OBJECT, exactify(classRef));
    }
    return restriction;
  }

  @Override
  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field,
      String value) {
    return createFieldRestriction(classRef, field, value, DEFAULT_TOKENIZE);
  }

  @Override
  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field,
      String value, boolean tokenize) {
    QueryRestriction restriction = null;
    if ((classRef != null) && StringUtils.isNotBlank(field)) {
      restriction = createRestriction(asFieldName(classRef, field), value, tokenize);
    }
    return restriction;
  }

  @Override
  public IQueryRestriction createFieldRefRestriction(DocumentReference classRef, String field,
      EntityReference ref) {
    IQueryRestriction restriction = null;
    if ((classRef != null) && StringUtils.isNotBlank(field)) {
      String fieldStr = asFieldName(classRef, field);
      if (ref != null) {
        restriction = createRestriction(fieldStr, exactify(ref, false));
        if (classRef.getWikiReference().equals(modelUtils.extractRef(ref,
            WikiReference.class).orNull())) {
          QueryRestrictionGroup restrGrp = createRestrictionGroup(Type.OR);
          restrGrp.add(restriction);
          restrGrp.add(createRestriction(fieldStr, exactify(ref)));
          restriction = restrGrp;
        }
      } else {
        restriction = createRestriction(fieldStr, "");
      }
    }
    return restriction;
  }

  @Override
  public QueryRestriction createRangeRestriction(String field, String from, String to) {
    return createRangeRestriction(field, from, to, true);
  }

  @Override
  public QueryRestriction createRangeRestriction(String field, String from, String to,
      boolean inclusive) {
    String value = from + " TO " + to;
    if (inclusive) {
      value = "[" + value + "]";
    } else {
      value = "{" + value + "}";
    }
    return createRestriction(field, value, false);
  }

  @Override
  public QueryRestriction createDateRestriction(String field, Date date) {
    return createRestriction(field, IndexFields.dateToString(date), false);
  }

  @Override
  public QueryRestriction createFromDateRestriction(String field, Date fromDate,
      boolean inclusive) {
    return createFromToDateRestriction(field, fromDate, null, inclusive);
  }

  @Override
  public QueryRestriction createToDateRestriction(String field, Date toDate, boolean inclusive) {
    return createFromToDateRestriction(field, null, toDate, inclusive);
  }

  @Override
  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, Date toDate,
      boolean inclusive) {
    String from = (fromDate != null) ? IndexFields.dateToString(fromDate) : DATE_LOW;
    String to = (toDate != null) ? IndexFields.dateToString(toDate) : DATE_HIGH;
    return createRangeRestriction(field, from, to, inclusive);
  }

  @Override
  public QueryRestriction createNumberRestriction(String field, Number number) {
    return createRestriction(field, IndexFields.numberToString(number), false);
  }

  @Override
  public QueryRestriction createFromToNumberRestriction(String field, Number fromNumber,
      Number toNumber, boolean inclusive) {
    String from = IndexFields.numberToString(MoreObjects.firstNonNull(fromNumber, new Integer(0)));
    String to = IndexFields.numberToString(MoreObjects.firstNonNull(toNumber, Integer.MAX_VALUE));
    return createRangeRestriction(field, from, to, inclusive);
  }

  @Override
  public QueryRestrictionGroup createAttachmentRestrictionGroup(List<String> mimeTypes,
      List<String> mimeTypesBlackList, List<String> filenamePrefs) {
    mimeTypes = MoreObjects.firstNonNull(mimeTypes, ImmutableList.<String>of());
    mimeTypesBlackList = MoreObjects.firstNonNull(mimeTypesBlackList, ImmutableList.<String>of());
    filenamePrefs = MoreObjects.firstNonNull(filenamePrefs, ImmutableList.<String>of());
    QueryRestrictionGroup attGrp = createRestrictionGroup(Type.AND);
    attGrp.add(createRestrictionGroup(Type.OR, Arrays.asList(IndexFields.MIMETYPE), Lists.transform(
        mimeTypes, FUNC_EXACTIFY)));
    attGrp.add(createRestrictionGroup(Type.OR, Arrays.asList(IndexFields.MIMETYPE), Lists.transform(
        mimeTypesBlackList, FUNC_EXACTIFY)).setNegate(true));
    attGrp.add(createRestrictionGroup(Type.OR, Arrays.asList(IndexFields.FILENAME), filenamePrefs));
    return attGrp;
  }

  @Override
  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields,
      List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, false);
  }

  @Override
  public LuceneSearchResult searchWithoutChecks(LuceneQuery query, List<String> sortFields,
      List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, true);
  }

  @Override
  public LuceneSearchResult search(String queryString, List<String> sortFields,
      List<String> languages) {
    return new LuceneSearchResult(queryString, sortFields, languages, false);
  }

  @Override
  public LuceneSearchResult searchWithoutChecks(String queryString, List<String> sortFields,
      List<String> languages) {
    return new LuceneSearchResult(queryString, sortFields, languages, true);
  }

  @Override
  public int getResultLimit() {
    return getResultLimit(false);
  }

  @Override
  public int getResultLimit(boolean skipChecks) {
    int limit = getLucenePlugin().getResultLimit(skipChecks, context.getXWikiContext());
    LOGGER.debug("getResultLimit: got '{}' for skipChecks '{}'", limit, skipChecks);
    return limit;
  }

  @Deprecated
  @Override
  public void queueForIndexing(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException {
    luceneIndexService.queueForIndexing(docRef);
  }

  @Deprecated
  @Override
  public void queueForIndexing(XWikiDocument doc) {
    luceneIndexService.queueForIndexing(doc);
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) context.getXWikiContext().getWiki().getPlugin("lucene",
        context.getXWikiContext());
  }

  @Override
  public WebSearchQueryBuilder createWebSearchBuilder(DocumentReference configDocRef)
      throws DocumentNotExistsException {
    WebSearchQueryBuilder ret = null;
    ret = Utils.getComponent(WebSearchQueryBuilder.class);
    if ((configDocRef != null) && rightsAccess.hasAccessLevel(configDocRef, EAccessLevel.VIEW)) {
      ret.setConfigDoc(modelAccess.getDocument(configDocRef));
    }
    return ret;
  }

  @Override
  public LuceneSearchResult webSearch(String searchTerm, DocumentReference configDocRef,
      List<String> languages, List<String> sortFields, QueryRestrictionGroup restrGroup)
          throws DocumentNotExistsException {
    WebSearchQueryBuilder builder = createWebSearchBuilder(configDocRef);
    builder.setSearchTerm(searchTerm);
    LuceneQuery query = builder.build();
    if ((restrGroup != null) && !restrGroup.isEmpty()) {
      query.add(restrGroup);
    }
    if (sortFields == null) {
      sortFields = new ArrayList<String>();
    }
    sortFields.addAll(modelAccess.getFieldValue(builder.getConfigDocRef(),
        WebSearchConfigClass.FIELD_SORT_FIELDS).or(Collections.<String>emptyList()));
    return search(query, sortFields, languages);
  }

}
