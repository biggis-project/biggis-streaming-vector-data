//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// NOT_PUBLISHED
public final class PropertiesUtil {

  private PropertiesUtil() {
  }

  public static Properties loadPropertiesFromResource(String resource) throws IOException {
    InputStream configFile = Thread
        .currentThread()
        .getContextClassLoader()
        .getResourceAsStream(resource);
    Properties fileProperties = new Properties();
    fileProperties.load(configFile);
    return fileProperties;
  }

}
