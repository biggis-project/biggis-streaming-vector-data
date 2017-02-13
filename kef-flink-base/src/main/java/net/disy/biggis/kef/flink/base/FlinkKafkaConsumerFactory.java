//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.base;

import static net.disy.biggis.kef.flink.base.PropertiesUtil.loadPropertiesFromResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;

import com.fasterxml.jackson.databind.node.ObjectNode;

// NOT_PUBLISHED
public final class FlinkKafkaConsumerFactory {

  private static final String KAFKA_PROPERTIES = "kafka.properties"; //$NON-NLS-1$

  private FlinkKafkaConsumerFactory() {
  }

  public static FlinkKafkaConsumer09<ObjectNode> createKafkaConsumer(
      List<String> topics,
      String[] args) throws IOException {
    ParameterTool parameters = getParameters(args);
    return new FlinkKafkaConsumer09<>(
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
    @SuppressWarnings("unchecked")
    ParameterTool fileParameters = ParameterTool.fromMap((Map) fileProperties);
    return fileParameters;
  }

}
