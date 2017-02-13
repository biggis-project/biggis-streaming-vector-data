//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.db;

import static net.disy.biggis.kef.flink.base.PropertiesUtil.loadPropertiesFromResource;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

// NOT_PUBLISHED
public abstract class DbStreamTableSink<T> extends RichSinkFunction<T> {

  private static final String DATA_SOURCE_PROPERTIES = "dataSource.properties"; //$NON-NLS-1$

  private static final long serialVersionUID = -2276739022931366223L;
  private IConnectionFactory connectionFactory;

  @Override
  public void invoke(T value) throws Exception {
    try (Connection connection = connectionFactory.getConnection()) {
      execute(connection, value);
    }
  }

  protected abstract void execute(Connection connection, T value) throws Exception;

  @Override
  public void open(Configuration parameters) throws Exception {
    Properties properties = loadPropertiesFromResource(DATA_SOURCE_PROPERTIES);
    DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
    connectionFactory = dataSource::getConnection;
  }

  @Override
  public void close() throws Exception {

  }
}
