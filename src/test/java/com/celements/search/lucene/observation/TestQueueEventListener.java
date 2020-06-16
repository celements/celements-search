package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AttachmentData;
import com.xpn.xwiki.plugin.lucene.DeleteData;
import com.xpn.xwiki.plugin.lucene.DocumentData;
import com.xpn.xwiki.plugin.lucene.WikiData;

@Component(QueueEventListener.NAME)
public class TestQueueEventListener extends QueueEventListener {

  final WikiData wikiDataMock = createMockAndAddToDefault(WikiData.class);
  final List<WikiReference> wikisIndex = new ArrayList<>();
  final DocumentData docDataMock = createMockAndAddToDefault(DocumentData.class);
  final List<XWikiDocument> docsIndex = new ArrayList<>();
  final AttachmentData attDataMock = createMockAndAddToDefault(AttachmentData.class);
  final List<XWikiAttachment> attsIndex = new ArrayList<>();
  final DeleteData deleteDataMock = createMockAndAddToDefault(DeleteData.class);
  final List<String> delete = new ArrayList<>();

  public int allCapturedSize() {
    return docsIndex.size() + attsIndex.size() + wikisIndex.size() + delete.size();
  }

  @Override
  WikiData newWikiData(WikiReference wiki, boolean delete) {
    if (delete) {
      this.delete.add(super.newDeleteData(wiki).getId());
    } else {
      wikisIndex.add(wiki);
    }
    return wikiDataMock;
  }

  @Override
  DocumentData newDocumentData(XWikiDocument doc) {
    docsIndex.add(doc);
    return docDataMock;
  }

  @Override
  AttachmentData newAttachmentData(XWikiAttachment att) {
    attsIndex.add(att);
    return attDataMock;
  }

  @Override
  DeleteData newDeleteData(EntityReference ref) {
    delete.add(super.newDeleteData(ref).getId());
    return deleteDataMock;
  }

}
