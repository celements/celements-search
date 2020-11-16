package com.celements.search.lucene.observation;

import static com.google.common.base.Predicates.*;

import java.util.Optional;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;

import com.celements.model.util.ReferenceSerializationMode;
import com.celements.search.lucene.observation.event.LuceneQueueEvent;
import com.google.common.base.Strings;

/**
 * local helper reference for handing over the language alongside the document reference to remote
 * {@link LuceneQueueEvent}s.
 * see {@link QueueDocumentEventConverter} and {@link QueueEventListener}.
 */
class QueueLangDocumentReference extends ImmutableDocumentReference {

  private static final long serialVersionUID = -1437001199304212920L;

  private final String lang;

  QueueLangDocumentReference(DocumentReference docRef, String lang) {
    super(docRef);
    this.lang = lang;
  }

  Optional<String> getLang() {
    return Optional.ofNullable(lang).filter(not(Strings::isNullOrEmpty));
  }

  @Override
  public String toString() {
    return serialize(ReferenceSerializationMode.GLOBAL) + "-" + lang;
  }

}
