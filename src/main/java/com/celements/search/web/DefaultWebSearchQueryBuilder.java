package com.celements.search.web;

import static com.celements.search.lucene.LuceneUtils.*;
import static com.celements.search.web.classes.WebSearchConfigClass.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.list.ListField;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneUtils;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultWebSearchQueryBuilder implements WebSearchQueryBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSearchQueryBuilder.class);

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private List<WebSearchPackage> webSearchPackages;

  @Requirement(WebSearchConfigClass.CLASS_DEF_HINT)
  private ClassDefinition webSearchConfigClass;

  @Requirement
  private IPageTypeClassConfig ptClassConf;

  @Requirement
  private ConfigurationSource configSrc;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IWebSearchService webSearchService;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  private WikiReference wikiRef;
  private XWikiDocument configDoc;
  private String searchTerm = "";
  private Set<WebSearchPackage> activatedPackages = new HashSet<>();

  @Override
  public WikiReference getWikiRef() {
    return firstNonNull(wikiRef, context.getWikiRef());
  }

  @Override
  public WebSearchQueryBuilder setWikiRef(WikiReference wikiRef) {
    this.wikiRef = wikiRef;
    return this;
  }

  @Override
  public DocumentReference getConfigDocRef() {
    if (configDoc != null) {
      return getConfigDoc().getDocumentReference();
    }
    return null;
  }

  private XWikiDocument getConfigDoc() {
    if (configDoc == null) {
      configDoc = getDefaultConfigDoc();
    }
    return configDoc;
  }

  private XWikiDocument getDefaultConfigDoc() {
    try {
      return modelAccess.getDocument(new DocumentReference("XWikiPreferences", new SpaceReference(
          "XWiki", context.getWikiRef())));
    } catch (DocumentNotExistsException exc) {
      throw new IllegalStateException("XWiki.XWikiPreferences should always exist", exc);
    }
  }

  private <T> Optional<T> fetch(ClassField<T> field) {
    return XWikiObjectFetcher.on(getConfigDoc()).fetchField(field).stream().findFirst();
  }

  @Override
  public WebSearchQueryBuilder setConfigDoc(XWikiDocument doc) {
    checkState(configDoc == null, "config doc already defined");
    configDoc = doc;
    return this;
  }

  @Override
  public String getSearchTerm() {
    return searchTerm;
  }

  @Override
  public DefaultWebSearchQueryBuilder setSearchTerm(String searchTerm) {
    this.searchTerm = Strings.nullToEmpty(searchTerm).trim();
    return this;
  }

  @Override
  public Collection<WebSearchPackage> getPackages() {
    Set<WebSearchPackage> ret = webSearchService.getAvailablePackages(getConfigDoc());
    if (!activatedPackages.isEmpty()) {
      ret = Sets.intersection(ret, activatedPackages);
    }
    checkState(!ret.isEmpty(), "no WebSearchPackages defined");
    return ret;
  }

  @Override
  public WebSearchQueryBuilder addPackage(WebSearchPackage searchPackage) {
    activatedPackages.add(searchPackage);
    return this;
  }

  @Override
  public WebSearchQueryBuilder addPackage(String packageName) {
    try {
      addPackage(Utils.getComponentManager().lookup(WebSearchPackage.class, packageName));
    } catch (ComponentLookupException exc) {
      LOGGER.info("addPackage: invalid package '{}'", packageName);
    }
    return this;
  }

  @Override
  public LuceneQuery build() {
    LuceneQuery query = new LuceneQuery();
    query.setWiki(getWikiRef());
    query.add(getRestrExcludeWebPref());
    query.add(getRestrSpaces(false));
    query.add(getRestrSpaces(true));
    query.add(getRestrDocs(false));
    query.add(getRestrDocs(true));
    query.add(getRestrPageTypes(false));
    query.add(getRestrPageTypes(true));
    Collection<WebSearchPackage> searchPackages = getPackages();
    query.add(getRestrPackages(searchPackages));
    query.add(getRestrLinkedDocsOnly(searchPackages));
    LOGGER.info("build: for '{}' returning '{}'", this, query);
    return query;
  }

  private IQueryRestriction getRestrExcludeWebPref() {
    return searchService.createRestriction(IndexFields.DOCUMENT_NAME, LuceneUtils.exactify(
        ModelContext.WEB_PREF_DOC_NAME)).setNegate(true);
  }

  private IQueryRestriction getRestrSpaces(boolean isBlacklist) {
    ClassField<List<SpaceReference>> field = isBlacklist ? FIELD_SPACES_BLACK_LIST : FIELD_SPACES;
    return buildRestrictionFromField(field,
        spaceRef -> searchService.createSpaceRestriction(spaceRef)).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrDocs(boolean isBlacklist) {
    ClassField<List<DocumentReference>> field = isBlacklist ? FIELD_DOCS_BLACK_LIST : FIELD_DOCS;
    return buildRestrictionFromField(field, docRef -> searchService.createDocRestriction(docRef))
        .setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrPageTypes(boolean isBlacklist) {
    ClassField<List<PageTypeReference>> field = isBlacklist ? FIELD_PAGETYPES_BLACK_LIST
        : FIELD_PAGETYPES;
    return buildRestrictionFromField(field,
        pageTypeRef -> searchService.createFieldRestriction(ptClassConf.getPageTypeClassRef(),
            IPageTypeClassConfig.PAGE_TYPE_FIELD, exactify(pageTypeRef.getConfigName())))
                .setNegate(isBlacklist);
  }

  private <T> IQueryRestriction buildRestrictionFromField(ClassField<List<T>> field,
      Function<T, IQueryRestriction> restrictionFunc) {
    List<T> values = new ArrayList<>();
    if (field instanceof ListField) {
      values.addAll(getDefaultValues((ListField<T>) field));
    }
    fetch(field).ifPresent(values::addAll);
    return buildRestrictionGroup(Type.OR, values, restrictionFunc);
  }

  private <T> List<T> getDefaultValues(ListField<T> field) {
    List<String> valueStrs = configSrc.getProperty("celements.search.web.defaultValue."
        + field.getName(), ImmutableList.<String>of());
    return FluentIterable.from(valueStrs).transform(field.getMarshaller().getResolver()).filter(
        Predicates.notNull()).toList();
  }

  private IQueryRestriction getRestrPackages(Collection<WebSearchPackage> searchPackages) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    for (WebSearchPackage searchPackage : searchPackages) {
      QueryRestrictionGroup searchPackageGrp = searchService.createRestrictionGroup(Type.AND);
      searchPackageGrp.add(searchService.createDocTypeRestriction(searchPackage.getDocType()));
      searchPackageGrp.add(searchPackage.getQueryRestriction(getConfigDoc(), getSearchTerm()));
      grp.add(searchPackageGrp);
    }
    fetch(FIELD_FUZZY_SEARCH).ifPresent(grp::setFuzzy);
    return grp;
  }

  private IQueryRestriction getRestrLinkedDocsOnly(Collection<WebSearchPackage> searchPackages) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    if (fetch(FIELD_LINKED_DOCS_ONLY).orElse(false)) {
      for (WebSearchPackage searchPackage : searchPackages) {
        grp.add(getLinkedDocsOnlyPackageRestr(searchPackage));
      }
    }
    return grp;
  }

  private IQueryRestriction getLinkedDocsOnlyPackageRestr(WebSearchPackage searchPackage) {
    if (searchPackage.getDocType() == LuceneDocType.DOC) {
      return searchService.createObjectRestriction(searchPackage.getLinkedClassRef().orNull());
    } else {
      // since objects don't exist on e.g. attachment lucene docs, this restriction makes sure
      // that these lucene docs are also found if linkedDocsOnly is activated
      return searchService.createDocTypeRestriction(searchPackage.getDocType());
    }
  }

  @Override
  public String toString() {
    return new StringBuilder().append("DefaultWebSearchQueryBuilder [configDoc=").append(
        getConfigDocRef()).append(", searchTerm=").append(getSearchTerm()).append("]").toString();
  }

}
