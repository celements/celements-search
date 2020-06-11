package com.celements.search.lucene.observation;

import static com.celements.common.test.CelementsTestUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AttachmentData;
import com.xpn.xwiki.plugin.lucene.DocumentData;

@Component(QueueEventListener.NAME)
public class TestQueueEventListener extends QueueEventListener {

  final DocumentData docDataMock = createMockAndAddToDefault(DocumentData.class);
  final List<XWikiDocument> docDatas = new ArrayList<>();
  final AttachmentData attDataMock = createMockAndAddToDefault(AttachmentData.class);
  final List<XWikiAttachment> attDatas = new ArrayList<>();

  @Override
  DocumentData newDocumentData(XWikiDocument doc) {
    docDatas.add(doc);
    return docDataMock;
  }

  @Override
  AttachmentData newAttachmentData(XWikiAttachment att) {
    attDatas.add(att);
    return attDataMock;
  }

}
