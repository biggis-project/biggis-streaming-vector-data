// Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.feature;

import static net.disy.biggis.kef.flink.base.FlinkKafkaConsumerFactory.createKafkaConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


import org.apache.commons.lang3.StringUtils;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


import net.disy.biggis.kef.flink.base.PropertiesUtil;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

public class SplitFeatureCollectionJob {

  private static final String JOB_PROPERTIES = "job.properties"; //$NON-NLS-1$

  public static void main(String[] args) throws Exception {
    List<String> topics = getTopics();
    FlinkKafkaConsumer010<ObjectNode> consumer = createKafkaConsumer(topics, args);
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    DataStream<ObjectNode> kafkaStream = env.addSource(consumer);

    kafkaStream
        .flatMap(new ExtractFeaturesFunction(new FeatureIdGenerator()))
        .keyBy(0)
        .flatMap(new DuplicateFilter())
        .addSink(new FeaturesTableSink());
    env.execute("Split Feature Collection Job @KEF");

  }

  private static List<String> getTopics() throws IOException {
    Properties jobProperties = PropertiesUtil.loadPropertiesFromResource(JOB_PROPERTIES);
    String topics = jobProperties.getProperty("topics"); //$NON-NLS-1$
    return Arrays.stream(topics.split(",")).map(StringUtils::trim).collect(Collectors.toList()); //$NON-NLS-1$
  }
}
