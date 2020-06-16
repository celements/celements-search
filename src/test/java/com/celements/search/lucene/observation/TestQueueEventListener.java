package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AttachmentData;
import com.xpn.xwiki.plugin.lucene.DocumentData;
import com.xpn.xwiki.plugin.lucene.WikiData;

@Component(QueueEventListener.NAME)
public class TestQueueEventListener extends QueueEventListener {

  final DocumentData docDataMock = createMockAndAddToDefault(DocumentData.class);
  final List<XWikiDocument> docsIndex = new ArrayList<>();
  final List<XWikiDocument> docsDelete = new ArrayList<>();
  final AttachmentData attDataMock = createMockAndAddToDefault(AttachmentData.class);
  final List<XWikiAttachment> attsIndex = new ArrayList<>();
  final List<XWikiAttachment> attsDelete = new ArrayList<>();
  final WikiData wikiDataMock = createMockAndAddToDefault(WikiData.class);
  final List<WikiReference> wikisIndex = new ArrayList<>();
  final List<WikiReference> wikisDelete = new ArrayList<>();

  public int allCapturedSize() {
    return docsIndex.size() + docsDelete.size()
        + attsIndex.size() + attsDelete.size()
        + wikisIndex.size() + wikisDelete.size();
  }

  @Override
  DocumentData newDocumentData(XWikiDocument doc, boolean delete) {
    (delete ? docsDelete : docsIndex).add(doc);
    return docDataMock;
  }

  @Override
  AttachmentData newAttachmentData(XWikiAttachment att, boolean delete) {
    (delete ? attsDelete : attsIndex).add(att);
    return attDataMock;
  }

  @Override
  WikiData newWikiData(WikiReference wiki, boolean delete) {
    (delete ? wikisDelete : wikisIndex).add(wiki);
    return wikiDataMock;
  }

}
