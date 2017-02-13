//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.feature;

import static net.disy.biggis.kef.flink.feature.MapJsonConstants.*;

import java.util.stream.StreamSupport;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExtractFeaturesFunction implements FlatMapFunction<ObjectNode, Tuple2<Integer, KefFeature>> {
	private static final long serialVersionUID = -2522168019108009712L;

	@Override
	public void flatMap(ObjectNode json, Collector<Tuple2<Integer, KefFeature>> out) throws Exception {
		JsonNode features = json.get(FEATURES_ROOT);
		StreamSupport.stream(features.spliterator(), false)
				.map(feature -> Tuple2.of(createIdentifier(feature), getData(feature))).forEach(out::collect);
	}

	private Integer createIdentifier(JsonNode feature) {
		JsonNode psId = feature.get(FEATURE_PROPERTIES).get(FEATURE_PROPERTY_ID);
		if (psId == null) {
			System.out.println("Oh NO!");
			return -1;
		} else {
			int id = psId.asInt(-1);
			return Integer.valueOf(id);
		}
	}

	private KefFeature getData(JsonNode feature) {
		return new KefFeature(feature.get(FEATURE_GEOMETRY), feature.get(FEATURE_PROPERTIES));
	}
}