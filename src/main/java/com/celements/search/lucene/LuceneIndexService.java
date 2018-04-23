package com.celements.search.lucene;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

@Component
public class LuceneIndexService implements ILuceneIndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public void queueForIndexing(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException {
    XWikiDocument doc = modelAccess.getDocument(docRef);
    queueForIndexing(doc);
  }

  @Override
  public void queueForIndexing(XWikiDocument doc) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("adding to index queue '{}'", modelUtils.serializeRef(
          doc.getDocumentReference()));
    }
    getLucenePlugin().queueDocument(doc, getContext());
    getLucenePlugin().queueAttachment(doc, getContext());
  }

  @Override
  public boolean rebuildIndexForAllWikis() {
    LOGGER.info("rebuildIndexForAllWikis start '{}'");
    return getLucenePlugin().rebuildIndex();
  }

  @Override
  public boolean rebuildIndex(Collection<WikiReference> wikiRefs) {
    LOGGER.info("rebuildIndex start for wikiRefs '{}'", wikiRefs);
    return getLucenePlugin().rebuildIndex(new ArrayList<>(wikiRefs), false);
  }

  @Override
  public boolean rebuildIndex(EntityReference entityRef) {
    LOGGER.info("rebuildIndex start for entityRef '{}'", entityRef);
    return getLucenePlugin().rebuildIndex(entityRef, false);
  }

  @Override
  public boolean rebuildIndexWithWipe() {
    return getLucenePlugin().rebuildIndexWithWipe(null, false);
  }

  @Override
  public void optimizeIndex() {
    getLucenePlugin().optimizeIndex();
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) getContext().getWiki().getPlugin("lucene", getContext());
  }

}
