//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.charts;

import static net.disy.biggis.kef.flink.charts.ChartsJsonConstants.CHART_ROOT;
import static net.disy.biggis.kef.flink.charts.ChartsJsonConstants.TIME_SERIES_DATA;
import static net.disy.biggis.kef.flink.charts.ChartsJsonConstants.TIME_SERIES_NAME;
import static net.disy.biggis.kef.flink.charts.ChartsJsonConstants.TIME_SERIES_ROOT;

import java.util.stream.StreamSupport;

import net.disy.biggis.kef.flink.base.KeyedJsonDeserializationSchema;

import org.apache.commons.math3.distribution.KolmogorovSmirnovDistribution;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExtractDataSeriesFunction implements FlatMapFunction<ObjectNode, Tuple2<SeriesIdentifier, JsonNode>> {
	private static final long serialVersionUID = -2522168019108009712L;

	@Override
	public void flatMap(ObjectNode json, Collector<Tuple2<SeriesIdentifier, JsonNode>> out) throws Exception {

		JsonNode charts = json.get(CHART_ROOT);
		StreamSupport.stream(charts.spliterator(), false)
				.map(series -> Tuple2.of(createIdentifier(json, series), getData(series))).forEach(out::collect);
	}

	private SeriesIdentifier createIdentifier(ObjectNode node, JsonNode series) {
		int laufendeNummer = getLaufendeNummer(node);
		String name = getName(series);
		String topic = getKafkaContext(node).get(KeyedJsonDeserializationSchema.KAFKA_TOPIC).asText();
		return new SeriesIdentifier(laufendeNummer, name, Measurement.parse(topic));
	}

	private JsonNode getKafkaContext(ObjectNode node) {
		return node.get(KeyedJsonDeserializationSchema.KAFKA_CONTEXT);
	}

	private String getName(JsonNode series) {
		return series.get(TIME_SERIES_ROOT).get(TIME_SERIES_NAME).asText();
	}

	private int getLaufendeNummer(ObjectNode node) {
		String sourceFileName = getKafkaContext(node).get(KeyedJsonDeserializationSchema.KAFKA_MESSAGE_KEY).asText();
		ImportFileNameAdapter fileNameAdapter = new ImportFileNameAdapter(sourceFileName);
		return fileNameAdapter.getIdentifier();
	}

	private JsonNode getData(JsonNode series) {
		return series.get(TIME_SERIES_ROOT).get(TIME_SERIES_DATA);
	}
}