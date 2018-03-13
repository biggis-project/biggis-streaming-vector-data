// Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.feature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import org.apache.flink.api.java.tuple.Tuple2;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.util.PGobject;


import net.disy.biggis.kef.flink.db.DbStreamTableSink;

// NOT_PUBLISHED
public class FeaturesTableSink extends DbStreamTableSink<Tuple2<String, KefFeature>> {

  private static final long serialVersionUID = 6248407471911131794L;

  private static final String INSERT_SERIES_DATA = "INSERT INTO kef_features (geom, properties, realgeom) VALUES (?, " + "?, ST_GeomFromText(?, 4326))";
  //private static final String INSERT_SERIES_DATA = "INSERT INTO kef_features (geom, properties, realgeom) VALUES (?, " + "?, ST_GeomFromWKT(?))";

  @Override
  protected void execute(Connection connection, Tuple2<String, KefFeature> series)
      throws Exception {
    System.out.println("Execute function");
    try (PreparedStatement insertData = connection.prepareStatement(INSERT_SERIES_DATA)) {
      String geomText = series.f1.getGeometry().toString();
      JsonNode coords = series.f1.getGeometry().get("coordinates");
      String realGeomText = "POINT(" + coords.get(0) + " " + coords.get(1) + ")";
      insertData.setObject(1, wrapJson(geomText));
      insertData.setObject(2, wrapJson(series.f1.getProperties().toString()));
      insertData.setString(3, realGeomText);
      System.out.println("Insert data: " + insertData);
      insertData.execute();
    }
  }

  private PGobject wrapJson(String json) throws SQLException {
    PGobject wrappedJson = new PGobject();
    wrappedJson.setType("json");
    wrappedJson.setValue(json);
    return wrappedJson;
  }
}
