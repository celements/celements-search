package com.celements.search.lucene;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.ref.ReferenceField;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.lucene.query.QueryRestrictionString;
import com.google.common.collect.Range;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@ComponentRole
public interface ILuceneSearchService {

  /**
   * @deprecated NOT THREAD SAFE! instead use {@link #getDateFormatter()}
   */
  @Deprecated
  public static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");
  public static final String DATE_LOW = IndexFields.DATE_LOW;
  public static final String DATE_HIGH = IndexFields.DATE_HIGH;

  public Version getVersion();

  /**
   * @return LuceneQuery object for current wiki with {@link LuceneDocType#DOC} only
   */
  public LuceneQuery createQuery();

  /**
   * @deprecated instead use {@link #createQuery()} with
   *             {@link LuceneQuery#setDocTypes(java.util.Collection)}
   * @param types
   *          e.g. 'wikipage' or 'attachment'
   */
  @Deprecated
  public LuceneQuery createQuery(List<String> types);

  /**
   * @deprecated since 4.3 instead use {@link #getDateFormatter()}
   */
  @Deprecated
  public DateFormat getSDF();

  public DateTimeFormatter getDateFormatter();

  public QueryRestrictionGroup createRestrictionGroup(Type type);

  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields,
      List<String> values);

  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields,
      List<String> values, boolean tokenize, boolean fuzzy);

  public QueryRestrictionString createRestriction(String query) throws ParseException;

  public QueryRestriction createRestriction(String field, String value);

  public QueryRestriction createRestriction(String field, String value, boolean tokenize);

  public QueryRestriction createRestriction(String field, String value, boolean tokenize,
      boolean fuzzy);

  public QueryRestriction createDocTypeRestriction(LuceneDocType docType);

  public QueryRestriction createSpaceRestriction(SpaceReference spaceRef);

  public QueryRestriction createDocRestriction(DocumentReference docRef);

  /**
   * @deprecated instead use {@link #createObjectRestriction(ClassReference)}
   */
  @Deprecated
  public QueryRestriction createObjectRestriction(DocumentReference classRef);

  public QueryRestriction createObjectRestriction(ClassReference classRef);

  /**
   * @deprecated instead use {@link #createRestriction(ClassField, Object)}
   */
  @Deprecated
  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field,
      String value);

  public <T> QueryRestriction createRestriction(ClassField<T> field, T value);

  /**
   * @deprecated instead use {@link #createRestriction(ClassField, Object, boolean)}
   */
  @Deprecated
  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field,
      String value, boolean tokenize);

  public <T> QueryRestriction createRestriction(ClassField<T> field, T value, boolean tokenize);

  /**
   * @deprecated instead use {@link #createRestriction(ClassField, Object)} with a
   *             {@link ReferenceField}
   */
  @Deprecated
  public IQueryRestriction createFieldRefRestriction(DocumentReference classRef, String field,
      EntityReference ref);

  /**
   * @deprecated since 6.0 instead use {@link #createRangeRestriction(String, Range)}
   */
  @Deprecated
  public QueryRestriction createRangeRestriction(String field, String from, String to);

  /**
   * @deprecated since 6.0 instead use {@link #createRangeRestriction(String, Range)}
   */
  @Deprecated
  public QueryRestriction createRangeRestriction(String field, String from, String to,
      boolean inclusive);

  @NotNull
  public QueryRestriction createRangeRestriction(@Nullable String field,
      @NotNull Range<String> range);

  /**
   * @deprecated since 6.0 instead use {@link #createDateRestriction(String, LocalDateTime)}
   */
  @Deprecated
  public QueryRestriction createDateRestriction(String field, Date date);

  @NotNull
  public QueryRestriction createDateRestriction(@Nullable String field,
      @NotNull LocalDateTime date);

  /**
   * @deprecated since 6.0 instead use {@link #createDateRangeRestriction(String, Range)}
   */
  @Deprecated
  public QueryRestriction createFromDateRestriction(String field, Date fromDate, boolean inclusive);

  /**
   * @deprecated since 6.0 instead use {@link #createDateRangeRestriction(String, Range)}
   */
  @Deprecated
  public QueryRestriction createToDateRestriction(String field, Date toDate, boolean inclusive);

  /**
   * @deprecated since 6.0 instead use {@link #createDateRangeRestriction(String, Range)}
   */
  @Deprecated
  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, Date toDate,
      boolean inclusive);

  @NotNull
  public QueryRestriction createDateRangeRestriction(@Nullable String field,
      @NotNull Range<LocalDateTime> range);

  @NotNull
  public QueryRestrictionGroup createDateRangeRestriction(
      @Nullable String startField, @Nullable String endField,
      @NotNull Range<LocalDateTime> range);

  public QueryRestriction createNumberRestriction(String field, Number number);

  /**
   * @deprecated since 6.0 instead use {@link #createNumberRangeRestriction(String, Range)}
   */
  @Deprecated
  public QueryRestriction createFromToNumberRestriction(String field, Number fromNumber,
      Number toNumber, boolean inclusive);

  @NotNull
  public QueryRestriction createNumberRangeRestriction(@Nullable String field,
      @NotNull Range<? extends Number> range);

  public QueryRestrictionGroup createAttachmentRestrictionGroup(List<String> mimeTypes,
      List<String> mimeTypesBlackList, List<String> filenamePrefs);

  public LuceneSearchResult search(LuceneQuery query);

  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields,
      List<String> languages);

  public LuceneSearchResult searchWithoutChecks(LuceneQuery query);

  public LuceneSearchResult searchWithoutChecks(LuceneQuery query, List<String> sortFields,
      List<String> languages);

  public LuceneSearchResult search(String queryString, List<String> sortFields,
      List<String> languages);

  public LuceneSearchResult searchWithoutChecks(String queryString, List<String> sortFields,
      List<String> languages);

  public int getResultLimit();

  public int getResultLimit(boolean skipChecks);

  /**
   * @deprecated instead use {@link ILuceneIndexService}
   */
  @Deprecated
  public void queueForIndexing(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException;

  /**
   * @deprecated instead use {@link ILuceneIndexService}
   */
  @Deprecated
  public void queueForIndexing(XWikiDocument doc);

}
