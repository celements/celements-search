package com.celements.search.web.classes;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.marshalling.PageTypeMarshaller;
import com.celements.marshalling.ReferenceMarshaller;
import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.ComponentListField;
import com.celements.model.classes.fields.list.CustomListField;
import com.celements.model.classes.fields.list.StringListField;
import com.celements.model.classes.fields.number.FloatField;
import com.celements.pagetype.PageTypeReference;
import com.celements.search.web.packages.WebSearchPackage;

@Immutable
@Singleton
@Component(WebSearchConfigClass.CLASS_DEF_HINT)
public class WebSearchConfigClass extends AbstractClassDefinition
    implements WebSearchClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "WebSearchConfigClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<List<WebSearchPackage>> FIELD_PACKAGES = new ComponentListField.Builder<>(
      CLASS_REF, "packages", WebSearchPackage.class).multiSelect(true).separator(",").build();

  public static final ClassField<Boolean> FIELD_LINKED_DOCS_ONLY = new BooleanField.Builder(
      CLASS_REF, "linkedDocsOnly").displayType("yesno").build();

  public static final ClassField<Float> FIELD_FUZZY_SEARCH = new FloatField.Builder(CLASS_REF,
      "fuzzySearch").build();

  public static final ClassField<List<DocumentReference>> FIELD_DOCS = new CustomListField.Builder<>(
      CLASS_REF, "docs", new ReferenceMarshaller<>(DocumentReference.class)).multiSelect(true)
          .separator(",").build();

  public static final ClassField<List<DocumentReference>> FIELD_DOCS_BLACK_LIST = new CustomListField.Builder<>(
      CLASS_REF, "docsBlackList", new ReferenceMarshaller<>(DocumentReference.class))
          .multiSelect(true).separator(",").build();

  public static final ClassField<List<SpaceReference>> FIELD_SPACES = new CustomListField.Builder<>(
      CLASS_REF, "spaces", new ReferenceMarshaller<>(SpaceReference.class)).multiSelect(true)
          .separator(",").build();

  public static final ClassField<List<SpaceReference>> FIELD_SPACES_BLACK_LIST = new CustomListField.Builder<>(
      CLASS_REF, "spacesBlackList", new ReferenceMarshaller<>(SpaceReference.class))
          .multiSelect(true).separator(",").build();

  public static final ClassField<List<PageTypeReference>> FIELD_PAGETYPES = new CustomListField.Builder<>(
      CLASS_REF, "pageTypes", new PageTypeMarshaller()).multiSelect(true).separator(",").build();

  public static final ClassField<List<PageTypeReference>> FIELD_PAGETYPES_BLACK_LIST = new CustomListField.Builder<>(
      CLASS_REF, "pageTypesBlackList", new PageTypeMarshaller()).multiSelect(true).separator(",")
          .build();

  public static final ClassField<List<String>> FIELD_SORT_FIELDS = new StringListField.Builder<>(
      CLASS_REF, "sortFields").multiSelect(true).separator(",").build();

  public static final ClassField<String> FIELD_RESULT_ITEM_RENDER_SCRIPT = new StringField.Builder(
      CLASS_REF, "resultItemRenderScript").build();

  public static final ClassField<Boolean> FIELD_ADVANCED_SEARCH = new BooleanField.Builder(
      CLASS_REF, "advancedSearch").displayType("yesno").defaultValue(0).build();

  public WebSearchConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
