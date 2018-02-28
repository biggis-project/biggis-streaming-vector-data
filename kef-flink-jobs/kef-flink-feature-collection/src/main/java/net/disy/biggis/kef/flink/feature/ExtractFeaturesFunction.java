// Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.feature;

import static net.disy.biggis.kef.flink.feature.MapJsonConstants.FEATURES_ROOT;
import static net.disy.biggis.kef.flink.feature.MapJsonConstants.FEATURE_GEOMETRY;
import static net.disy.biggis.kef.flink.feature.MapJsonConstants.FEATURE_PROPERTIES;


import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;


import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.util.Collector;


public class ExtractFeaturesFunction
    implements
    FlatMapFunction<ObjectNode, Tuple2<String, KefFeature>> {
  private static final long serialVersionUID = -2522168019108009712L;

  private final Function<JsonNode, String> featureIdGenerator;

  public ExtractFeaturesFunction(Function<JsonNode, String> featureIdGenerator) {
    this.featureIdGenerator = featureIdGenerator;
  }

  @Override
  public void flatMap(ObjectNode json, Collector<Tuple2<String, KefFeature>> out)
      throws Exception {
    JsonNode features = json.get(FEATURES_ROOT);
    StreamSupport
        .stream(features.spliterator(), false)
        .map(feature -> Tuple2.of(featureIdGenerator.apply(feature), getData(feature)))
        .forEach(out::collect);
  }

  private KefFeature getData(JsonNode feature) {
    return new KefFeature(feature.get(FEATURE_GEOMETRY), feature.get(FEATURE_PROPERTIES));
  }

}