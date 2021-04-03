package com.celements.search.web.packages;

import static com.celements.search.lucene.LuceneUtils.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.ClassReference;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(TitleWebSearchPackage.NAME)
public class TitleWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "title";

  public static final String CFGSRC_PROP_BOOST = "celements.search.web." + NAME + ".boost";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ConfigurationSource cfgSrc;

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
    return false;
  }

  @Override
  public LuceneDocType getDocType() {
    return LuceneDocType.DOC;
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    float boost = cfgSrc.getProperty(CFGSRC_PROP_BOOST, 50f);
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    if (!Strings.nullToEmpty(searchTerm).trim().isEmpty()) {
      grp.add(searchService.createRestriction(
          IndexFields.DOCUMENT_TITLE, exactify(searchTerm), false)
          .setBoost(boost));
      grp.add(searchService.createRestriction(
          IndexFields.DOCUMENT_TITLE, searchTerm, true)
          .setBoost(boost / 2));
    }
    return grp;

  }

  @Override
  public com.google.common.base.Optional<ClassReference> getLinkedClassRef() {
    return com.google.common.base.Optional.absent();
  }

}
