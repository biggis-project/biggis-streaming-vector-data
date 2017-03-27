package net.disy.biggis.kef.flink.feature;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureIdGeneratorTest {

  @Test
  public void correctIdForFallenfaenge() throws Exception {
    JsonNode feature = readJsonSample("sample_fallenfaenge.json");

    String identifier = new FeatureIdGenerator().apply(feature);

    assertThat(identifier, is("ff-743"));
  }

  @Test
  public void correctIdForEiablage() throws Exception {
    JsonNode feature = readJsonSample("sample_eiablage.json");

    String identifier = new FeatureIdGenerator().apply(feature);

    assertThat(identifier, is("ei-1-2"));
  }

  private JsonNode readJsonSample(String resource) throws IOException, JsonProcessingException {
    InputStream sample = getClass().getResourceAsStream(resource);
    return new ObjectMapper().readTree(sample);
  }
}
