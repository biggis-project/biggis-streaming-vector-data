//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.base;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


import org.apache.flink.api.common.typeinfo.TypeInformation;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;


// NOT_PUBLISHED
public class KeyedJsonDeserializationSchema implements KeyedDeserializationSchema<ObjectNode> {

  private static final long serialVersionUID = 5746056209563369492L;

  public static final String KAFKA_CONTEXT = "kafkaContext"; //$NON-NLS-1$
  public static final String KAFKA_MESSAGE_KEY = "messageKey"; //$NON-NLS-1$
  public static final String KAFKA_TOPIC = "topic"; //$NON-NLS-1$
  public static final String KAFKA_PARTITION = "partition"; //$NON-NLS-1$
  public static final String KAFKA_OFFSET = "offset"; //$NON-NLS-1$

  private ObjectMapper mapper;

  private synchronized ObjectMapper getObjectMapper() {
    if (mapper == null) {
      mapper = new ObjectMapper();
    }
    return mapper;
  }

  @Override
  public boolean isEndOfStream(ObjectNode nextElement) {
    return false;
  }

  @Override
  public TypeInformation<ObjectNode> getProducedType() {
    return TypeInformation.of(ObjectNode.class);
  }

  @Override
  public ObjectNode deserialize(
      byte[] messageKey,
      byte[] message,
      String topic,
      int partition,
      long offset) throws IOException {
    ObjectNode readNode = getObjectMapper().readValue(message, ObjectNode.class);
    ObjectNode kafkaContext = createKafkaContext(messageKey, topic, partition, offset);
    readNode.set(KAFKA_CONTEXT, kafkaContext);
    return readNode;
  }

  private ObjectNode createKafkaContext(byte[] messageKey, String topic, int partition, long offset) {
    ObjectNode kafkaContext = JsonNodeFactory.instance.objectNode();
    kafkaContext.put(KAFKA_MESSAGE_KEY, new String(messageKey, StandardCharsets.UTF_8));
    kafkaContext.put(KAFKA_TOPIC, topic);
    kafkaContext.put(KAFKA_PARTITION, partition);
    kafkaContext.put(KAFKA_OFFSET, offset);
    return kafkaContext;
  }

}
