//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.db;

import java.sql.Connection;
import java.sql.SQLException;

// NOT_PUBLISHED
public interface IConnectionFactory {
  Connection getConnection() throws SQLException;
}
