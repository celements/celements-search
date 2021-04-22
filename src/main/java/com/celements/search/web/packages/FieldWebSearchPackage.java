package com.celements.search.web.packages;

import static com.celements.search.lucene.LuceneUtils.*;

import java.util.stream.Stream;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.WebSearchFieldConfigClass;
import com.xpn.xwiki.doc.XWikiDocument;

@ThreadSafe
@Singleton
@Component(FieldWebSearchPackage.NAME)
public class FieldWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "field";

  @Requirement
  private ILuceneSearchService searchService;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isDefault() {
    return false;
  }

  @Override
  public boolean isRequired(XWikiDocument cfgDoc) {
    return getConfigObjFetcher(cfgDoc).exists();
  }

  @Override
  public LuceneDocType getDocType() {
    return LuceneDocType.DOC;
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    getConfigObjFetcher(cfgDoc).fetchField(WebSearchFieldConfigClass.FIELD_NAME).stream()
        .flatMap(fieldName -> Stream.of(
            searchService.createRestriction(fieldName, searchTerm, true).setBoost(1),
            searchService.createRestriction(fieldName, exactify(searchTerm), false).setBoost(2)))
        .forEach(grp::add);
    return grp;
  }

  @Override
  public com.google.common.base.Optional<ClassReference> getLinkedClassRef() {
    return com.google.common.base.Optional.absent();
  }

  private XWikiObjectFetcher getConfigObjFetcher(XWikiDocument cfgDoc) {
    return (cfgDoc != null) ? XWikiObjectFetcher.on(cfgDoc)
        .filter(WebSearchFieldConfigClass.CLASS_REF)
        : XWikiObjectFetcher.empty();
  }

}
