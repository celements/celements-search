package com.celements.search.web.packages;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.ClassReference;

import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface WebSearchPackage {

  @NotNull
  String getName();

  boolean isDefault();

  boolean isRequired(@NotNull XWikiDocument cfgDoc);

  @NotNull
  LuceneDocType getDocType();

  @NotNull
  IQueryRestriction getQueryRestriction(@NotNull XWikiDocument cfgDoc, @NotNull String searchTerm);

  @NotNull
  Optional<ClassReference> getLinkedClassRef();

}
