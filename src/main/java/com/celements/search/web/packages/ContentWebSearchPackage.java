package com.celements.search.web.packages;

import static com.celements.search.lucene.LuceneUtils.*;
import static com.xpn.xwiki.plugin.lucene.IndexFields.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.ClassReference;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(ContentWebSearchPackage.NAME)
public class ContentWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "content";

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
    return true;
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
    float boost = cfgSrc.getProperty(CFGSRC_PROP_BOOST, 20f);
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    if (!Strings.nullToEmpty(searchTerm).trim().isEmpty()) {
      grp.add(searchService.createRestriction(FULLTEXT, searchTerm, true)
          .setBoost(boost));
      grp.add(searchService.createRestriction(FULLTEXT, exactify(searchTerm), false)
          .setBoost(boost * 2));
    }
    return grp;
  }

  @Override
  public Optional<ClassReference> getLinkedClassRef() {
    return Optional.absent();
  }

}
