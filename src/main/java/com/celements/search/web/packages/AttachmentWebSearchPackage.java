package com.celements.search.web.packages;

import static com.celements.search.lucene.LuceneUtils.*;
import static com.celements.search.web.classes.WebAttachmentSearchConfigClass.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.WebAttachmentSearchConfigClass;
import com.google.common.net.MediaType;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(AttachmentWebSearchPackage.NAME)
public class AttachmentWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "attachment";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement(ContentWebSearchPackage.NAME)
  private WebSearchPackage contentModule;

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
    return hasConfigObj(cfgDoc);
  }

  @Override
  public LuceneDocType getDocType() {
    return LuceneDocType.ATT;
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.AND);
    if (hasConfigObj(cfgDoc)) {
      grp.add(getRestrMimeTypes(cfgDoc, false));
      grp.add(getRestrMimeTypes(cfgDoc, true));
      grp.add(getRestrFilenamePrefixes(cfgDoc));
    }
    grp.add(contentModule.getQueryRestriction(cfgDoc, searchTerm));
    return grp;
  }

  private IQueryRestriction getRestrMimeTypes(XWikiDocument cfgDoc, boolean isBlacklist) {
    ClassField<List<MediaType>> field = isBlacklist ? FIELD_MIMETYPES_BLACK_LIST : FIELD_MIMETYPES;
    return buildRestrictionFromField(cfgDoc, field,
        mediaType -> searchService.createRestriction(IndexFields.MIMETYPE,
            exactify(mediaType.toString()))).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrFilenamePrefixes(XWikiDocument cfgDoc) {
    return buildRestrictionFromField(cfgDoc, FIELD_FILENAME_PREFIXES,
        str -> searchService.createRestriction(IndexFields.FILENAME, str));
  }

  private <T> IQueryRestriction buildRestrictionFromField(XWikiDocument cfgDoc,
      ClassField<List<T>> field, Function<T, IQueryRestriction> restrictionFunc) {
    List<T> values = XWikiObjectFetcher.on(cfgDoc).fetchField(field)
        .stream().flatMap(List::stream)
        .collect(Collectors.toList());
    return buildRestrictionGroup(Type.OR, values, restrictionFunc);
  }

  @Override
  public com.google.common.base.Optional<ClassReference> getLinkedClassRef() {
    return com.google.common.base.Optional.absent();
  }

  private boolean hasConfigObj(XWikiDocument cfgDoc) {
    return (cfgDoc != null) && XWikiObjectFetcher.on(cfgDoc)
        .filter(WebAttachmentSearchConfigClass.CLASS_REF)
        .exists();
  }

}
