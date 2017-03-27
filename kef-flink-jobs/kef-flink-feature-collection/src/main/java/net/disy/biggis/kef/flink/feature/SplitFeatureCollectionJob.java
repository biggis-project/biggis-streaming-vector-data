// Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.feature;

import static net.disy.biggis.kef.flink.base.FlinkKafkaConsumerFactory.createKafkaConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;

import com.fasterxml.jackson.databind.node.ObjectNode;

import net.disy.biggis.kef.flink.base.PropertiesUtil;

public class SplitFeatureCollectionJob {

  private static final String JOB_PROPERTIES = "job.properties"; //$NON-NLS-1$

  public static void main(String[] args) throws Exception {
    List<String> topics = getTopics();
    FlinkKafkaConsumer09<ObjectNode> consumer = createKafkaConsumer(topics, args);
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    DataStream<ObjectNode> kafkaStream = env.addSource(consumer);
    kafkaStream
        .flatMap(new ExtractFeaturesFunction(new FeatureIdGenerator()))
        .keyBy(0)
        .flatMap(new DuplicateFilter())
        // .print();
        .addSink(new FeaturesTableSink());
    env.execute();

  }

  private static List<String> getTopics() throws IOException {
    Properties jobProperties = PropertiesUtil.loadPropertiesFromResource(JOB_PROPERTIES);
    String topics = jobProperties.getProperty("topics"); //$NON-NLS-1$
    return Arrays.stream(topics.split(",")).map(StringUtils::trim).collect(Collectors.toList()); //$NON-NLS-1$
  }
}
