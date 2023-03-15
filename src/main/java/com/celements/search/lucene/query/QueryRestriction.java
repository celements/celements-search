package com.celements.search.lucene.query;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class QueryRestriction implements IQueryRestriction {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryRestriction.class);

  private static final Pattern TOKEN_PATTERN = Pattern.compile("( ?\"[^\"]*\" ?)|[^ ]+");

  private boolean negate = false;
  private String specifier = null;
  private String query = null;
  private boolean tokenizeQuery = true;
  private Float fuzzy = null;
  private Integer proximity = null;
  private Float boost = null;

  public QueryRestriction(String specifier, String query) {
    setSpecifier(specifier);
    setQuery(query);
  }

  public QueryRestriction(String specifier, String query, boolean tokenizeQuery) {
    this(specifier, query);
    setTokenizeQuery(tokenizeQuery);
  }

  public String getSpecifier() {
    return specifier;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public boolean getNegate() {
    return negate;
  }

  @Override
  public QueryRestriction setNegate(boolean negate) {
    this.negate = negate;
    return this;
  }

  public QueryRestriction setSpecifier(String specifier) {
    this.specifier = specifier;
    return this;
  }

  public QueryRestriction setQuery(String query) {
    this.query = query;
    return this;
  }

  /**
   * Uses required/AND for multiple keywords when true, e.g. (+keyword1 +keyword2),
   * else (keyword1 keyword2)
   *
   * @param tokenizeQuery
   * @return
   */
  public QueryRestriction setTokenizeQuery(boolean tokenizeQuery) {
    this.tokenizeQuery = tokenizeQuery;
    return this;
  }

  @Override
  public Optional<Float> getFuzzy() {
    return Optional.fromNullable(fuzzy);
  }

  @Override
  public QueryRestriction setFuzzy(@Nullable Float fuzzy) {
    this.fuzzy = fuzzy;
    return this;
  }

  /**
   * Use fuzzy search to find similar words.
   *
   * @param fuzzy
   *          Allowed distance from 0 to 1 where the closer to 1 the parameter is,
   *          the higher similarity the match needs to have.
   */
  public QueryRestriction setFuzzy(String fuzzy) {
    try {
      setFuzzy(Float.parseFloat(fuzzy));
    } catch (NumberFormatException nfe) {
      LOGGER.error("Exception parsing float of '{}'", fuzzy, nfe);
    }
    return this;
  }

  public QueryRestriction setFuzzy(float fuzzy) {
    return setFuzzy((Float) fuzzy);
  }

  public QueryRestriction setFuzzy() {
    return setFuzzy(-1f); // use Lucene's default (which is 0.5)
  }

  /**
   * The term's words have to be in the given proximity e.g. at most 8 words apart.
   *
   * @param proximity
   *          How many words apart can the words be.
   */
  public QueryRestriction setProximity(String proximity) {
    try {
      setProximity(Integer.parseInt(proximity));
    } catch (NumberFormatException nfe) {
      LOGGER.error("Exception parsing float of '{}'", proximity, nfe);
    }
    return this;
  }

  public QueryRestriction setProximity(int proximity) {
    this.proximity = proximity;
    return this;
  }

  /**
   * A boost factor for the term. The higher the boost factor, the more relevant the term.
   * The boost factor has to be positive, can be < 1 though. The default is 1.
   *
   * @param boost
   *          The factor to boost the term.
   * @return
   */
  public QueryRestriction setBoost(String boost) {
    try {
      setBoost(Float.parseFloat(boost));
    } catch (NumberFormatException nfe) {
      LOGGER.error("Exception parsing float of '{}'", boost, nfe);
    }
    return this;
  }

  public QueryRestriction setBoost(float boost) {
    this.boost = boost;
    return this;
  }

  /**
   * @deprecated instead use {@link #getQueryString()}
   * @return
   */
  @Deprecated
  public String getRestriction() {
    return getQueryString();
  }

  @Override
  public String getQueryString() {
    String ret = "";
    if (StringUtils.isNotBlank(specifier) && StringUtils.isNotBlank(query)) {
      ret = tokenizeQuery ? getTokenizedQuery() : query;
      DecimalFormat formater = getDecimalFormater();
      ret = makeRestrictionFuzzy(ret, formater);
      if ((proximity != null) && (proximity > 1)) {
        ret = "\"" + ret + "\"~" + formater.format(proximity);
      }
      ret = specifier + ":(" + ret + ")";
      if ((boost != null) && (boost > 0)) {
        ret += "^" + formater.format(boost);
      }
      if (negate) {
        ret = "NOT " + ret;
      }
    }
    return ret;
  }

  private String getTokenizedQuery() {
    Matcher m = TOKEN_PATTERN.matcher(query);
    StringBuilder tokenizedQuery = new StringBuilder();
    while (m.find()) {
      String token = m.group(0).trim();
      if (!token.matches("[\"+-]")) {
        token = QueryParser.escape(token);
        token = token.replaceAll("^(\\\\([+-]))?(\\\\(\"))?(.*?)(\\\\(\"))?$", "$2$4$5$7");
        token = token.replaceAll("^(.+)\\\\([\\?\\*].*)$", "$1$2");
        if (!token.matches("^[+-].*")) {
          token = "+" + token;
        }
        if (!token.matches("^.*[\\*\\\"]$") && ((proximity == null) || (proximity <= 1))) {
          token += "*";
        }
        tokenizedQuery.append(" " + token);
      }
    }
    return tokenizedQuery.toString().trim();
  }

  DecimalFormat getDecimalFormater() {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
    symbols.setDecimalSeparator('.');
    return new DecimalFormat("#.###", symbols);
  }

  String makeRestrictionFuzzy(String queryString, DecimalFormat formater) {
    String finalQuery = queryString;
    if (fuzzy != null) {
      String fuzzyStr = "~";
      if ((fuzzy >= 0) && (fuzzy <= 1)) {
        fuzzyStr += formater.format(fuzzy);
      }
      finalQuery = "";
      for (String term : queryString.split(" ")) {
        String termString = term + fuzzyStr;
        if (term.endsWith("*")) {
          termString = "(" + term.replaceAll("\\+?(.*)", "$1") + " OR " + term.replaceAll(
              "\\+?(.*)\\*$", "$1" + fuzzyStr) + ")";
        }
        finalQuery += termString + " AND ";
      }
      finalQuery = finalQuery.replaceAll("(.*) AND $", "$1");
    }
    return finalQuery;
  }

  @Override
  public QueryRestriction copy() {
    QueryRestriction copy = new QueryRestriction(specifier, query, tokenizeQuery);
    copy.fuzzy = fuzzy;
    copy.proximity = proximity;
    copy.boost = boost;
    copy.negate = negate;
    return copy;
  }

  @Override
  public boolean isEmpty() {
    return StringUtils.isBlank(getQueryString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(boost, fuzzy, negate, proximity, query, specifier, tokenizeQuery);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QueryRestriction) {
      QueryRestriction other = (QueryRestriction) obj;
      return Objects.equals(specifier, other.specifier)
          && Objects.equals(query, other.query)
          && Objects.equals(boost, other.boost)
          && Objects.equals(fuzzy, other.fuzzy)
          && Objects.equals(negate, other.negate)
          && Objects.equals(proximity, other.proximity)
          && Objects.equals(tokenizeQuery, other.tokenizeQuery);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "QueryRestriction [queryString=" + getQueryString() + "]";
  }

}
