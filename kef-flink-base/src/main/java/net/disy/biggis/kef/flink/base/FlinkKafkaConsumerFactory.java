//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.base;

import static net.disy.biggis.kef.flink.base.PropertiesUtil.loadPropertiesFromResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import org.apache.flink.api.java.utils.ParameterTool;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;


// NOT_PUBLISHED
public final class FlinkKafkaConsumerFactory {

  private static final String KAFKA_PROPERTIES = "kafka.properties"; //$NON-NLS-1$

   private static final String kafkaHost = System.getenv("KAFKA_HOST");
  // not needed anymore (only required for Kafka 0.8)
  // private static final String zkHost = System.getenv("ZK_HOST");

  private FlinkKafkaConsumerFactory() {
  }

  public static FlinkKafkaConsumer010<ObjectNode> createKafkaConsumer(
      List<String> topics,
      String[] args) throws IOException {
    ParameterTool parameters = getParameters(args);
    return new FlinkKafkaConsumer010<>(
        topics,
        new KeyedJsonDeserializationSchema(),
        parameters.getProperties());
  }

  private static ParameterTool getParameters(String[] args) throws IOException {
    ParameterTool fileParameters = loadParametersFromConfigFile();
    ParameterTool argsParameters = ParameterTool.fromArgs(args);
    return fileParameters.mergeWith(argsParameters);
  }

  private static ParameterTool loadParametersFromConfigFile() throws IOException {
    Properties fileProperties = loadPropertiesFromResource(KAFKA_PROPERTIES);

    // Manipulate default properties
    if (kafkaHost != null && !kafkaHost.isEmpty()) fileProperties.setProperty("bootstrap.servers", kafkaHost);
    // not needed anymore (only required for Kafka 0.8)
    // if (zkHost != null && !zkHost.isEmpty()) fileProperties.setProperty("zookeeper.connect", zkHost);

    @SuppressWarnings("unchecked")
    ParameterTool fileParameters = ParameterTool.fromMap((Map) fileProperties);
    return fileParameters;
  }

}
