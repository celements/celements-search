package com.celements.search.web.classes;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Immutable
@Singleton
@Component(WebSearchFieldConfigClass.CLASS_DEF_HINT)
public class WebSearchFieldConfigClass extends AbstractClassDefinition implements
    WebSearchClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "WebSearchFieldConfigClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_NAME = new StringField.Builder(
      CLASS_REF, "fieldName").build();

  public WebSearchFieldConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
