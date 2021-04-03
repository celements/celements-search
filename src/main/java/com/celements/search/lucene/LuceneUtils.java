package com.celements.search.lucene;

import static com.celements.model.util.ReferenceSerializationMode.*;
import static com.google.common.base.MoreObjects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.util.ModelUtils;
import com.celements.model.util.ReferenceSerializationMode;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

public final class LuceneUtils {

  private LuceneUtils() {}

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneUtils.class);

  private static final String QUOTE = "\"";
  private static final Splitter SPLITTER = Splitter.on("[,;\\| ]+");

  public static String exactify(String str) {
    str = Strings.nullToEmpty(str);
    if (!str.isEmpty()) {
      if (!str.startsWith(QUOTE)) {
        str = QUOTE + str;
      }
      if (!str.endsWith(QUOTE)) {
        str = str + QUOTE;
      }
    }
    return str;
  }

  public static List<String> exactify(List<String> strs) {
    List<String> ret = new ArrayList<>();
    for (String str : firstNonNull(strs, Collections.<String>emptyList())) {
      ret.add(exactify(str));
    }
    return ret;
  }

  public static String asFieldName(DocumentReference classRef, String field) {
    return serialize(classRef) + "." + field;
  }

  public static String exactify(EntityReference ref) {
    return exactify(ref, LOCAL);
  }

  @Deprecated
  public static String exactify(EntityReference ref, boolean local) {
    return exactify(ref, local ? LOCAL : GLOBAL);
  }

  public static String exactify(EntityReference ref, ReferenceSerializationMode mode) {
    String ret = serialize(ref, mode);
    if (!spaceEndsWithDigit(ref)) {
      ret = exactify(ret);
    }
    return ret;
  }

  // XXX workaround issue CELDEV-35
  private static boolean spaceEndsWithDigit(EntityReference ref) {
    EntityReference spaceRef = ref.extractReference(EntityType.SPACE);
    boolean isAtLeastDocRef = (ref.getType().ordinal() > EntityType.SPACE.ordinal());
    return isAtLeastDocRef && Character.isDigit(spaceRef.getName().charAt(
        spaceRef.getName().length() - 1));
  }

  public static String serialize(EntityReference ref) {
    return serialize(ref, LOCAL);
  }

  @Deprecated
  public static String serialize(EntityReference ref, boolean local) {
    return serialize(ref, local ? LOCAL : GLOBAL);
  }

  public static String serialize(EntityReference ref, ReferenceSerializationMode mode) {
    return (ref != null) ? getModelUtils().serializeRef(ref, mode) : "";
  }

  public static IQueryRestriction buildRestrictionGroup(Type type, String valuesStr,
      Function<String, IQueryRestriction> restrictionFunc) {
    return buildRestrictionGroup(type, SPLITTER.split(valuesStr), restrictionFunc);
  }

  public static <T> IQueryRestriction buildRestrictionGroup(Type type, Iterable<T> values,
      Function<T, IQueryRestriction> restrictionFunc) {
    QueryRestrictionGroup grp = new QueryRestrictionGroup(type);
    for (T obj : firstNonNull(values, Collections.<T>emptyList())) {
      if (obj != null) {
        try {
          grp.add(restrictionFunc.apply(obj));
        } catch (IllegalArgumentException iae) {
          LOGGER.warn("building restriction failed for value '{}' ", obj);
        }
      }
    }
    return grp;
  }

  private static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
