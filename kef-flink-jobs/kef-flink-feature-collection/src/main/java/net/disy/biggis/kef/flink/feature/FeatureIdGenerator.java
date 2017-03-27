package net.disy.biggis.kef.flink.feature;

import static net.disy.biggis.kef.flink.feature.MapJsonConstants.FEATURE_PROPERTIES;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class FeatureIdGenerator implements Function<JsonNode, String>, Serializable {

  private static final long serialVersionUID = 9082908574945058933L;
  private static final Pattern EXTRACT_BEFALLART = Pattern.compile("&befallart=([0,1])");

  @Override
  public String apply(JsonNode feature) {
    return getProperties(feature)
        .map(p -> p.get(MapJsonConstants.FEATURE_PROPERTY_LF_ID))
        .map(id -> "ff-" + id.asText())
        .orElseGet(() -> createEiablageId(feature));
  }

  private Optional<JsonNode> getProperties(JsonNode feature) {
    return Optional.of(feature).map(f -> f.get(FEATURE_PROPERTIES));
  }

  private String createEiablageId(JsonNode feature) {
    Optional<JsonNode> properties = getProperties(feature);
    String seriesType = properties
        .map(p -> p.get(MapJsonConstants.FEATURE_PROPERTY_URL))
        .map(JsonNode::asText)
        .map(EXTRACT_BEFALLART::matcher)
        .filter(Matcher::find)
        .map(m -> m.group(1))
        .orElse("unknown");
    return properties
        .map(p -> p.get(MapJsonConstants.FEATURE_PROPERTY_PS_ID))
        .map(id -> "ei-" + seriesType + "-" + id.asText())
        .orElseGet(() -> getDefaultIdentifier(feature));
  }

  private String getDefaultIdentifier(JsonNode feature) {
    LoggerFactory
        .getLogger(FeatureIdGenerator.class)
        .error("Cannot determine identifier for feature: " + feature.asText());
    return "missing";
  }

}
