package com.celements.search.web.classes;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.list.CustomListField;
import com.celements.model.classes.fields.list.StaticListField;
import com.google.common.net.MediaType;

@Immutable
@Singleton
@Component(WebAttachmentSearchConfigClass.CLASS_DEF_HINT)
public class WebAttachmentSearchConfigClass extends AbstractClassDefinition implements
    WebSearchClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "WebAttachmentSearchConfigClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<List<MediaType>> FIELD_MIMETYPES = new CustomListField.Builder<>(
      CLASS_REF, "mimeTypes", new MediaTypeMarshaller()).multiSelect(true).build();

  public static final ClassField<List<MediaType>> FIELD_MIMETYPES_BLACK_LIST = new CustomListField.Builder<>(
      CLASS_REF, "mimeTypesBlackList", new MediaTypeMarshaller()).multiSelect(true).build();

  public static final ClassField<List<String>> FIELD_FILENAME_PREFIXES = new StaticListField.Builder(
      CLASS_REF, "fileNamePrefixes").multiSelect(true).build();

  public WebAttachmentSearchConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
