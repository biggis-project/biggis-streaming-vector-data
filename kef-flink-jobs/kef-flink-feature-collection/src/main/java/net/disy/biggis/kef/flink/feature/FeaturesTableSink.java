//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.feature;

import static java.text.MessageFormat.format;
import static java.util.OptionalInt.of;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalInt;

import net.disy.biggis.kef.flink.db.DbStreamTableSink;

import org.apache.flink.api.java.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

// NOT_PUBLISHED
public class FeaturesTableSink extends DbStreamTableSink<Tuple2<Integer, KefFeature>> {

	private static final long serialVersionUID = 6248407471911131794L;

	private static final String INSERT_SERIES_DATA = "INSERT INTO kef_features (geom, properties, realgeom) VALUES (?, ?, ST_GeomFromText(?))";

	private static final Logger LOG = LoggerFactory.getLogger(FeaturesTableSink.class);

	@Override
	protected void execute(Connection connection, Tuple2<Integer, KefFeature> series) throws Exception {
		try (PreparedStatement insertData = connection.prepareStatement(INSERT_SERIES_DATA)) {
			String geomText = series.f1.getGeometry().toString();
			JsonNode coords = series.f1.getGeometry().get("coordinates");
			String realGeomText = "POINT(" + coords.get(0) + " " + coords.get(1) + ")";
			insertData.setString(1, geomText);
			insertData.setString(2, series.f1.getProperties().toString());
			insertData.setString(3, realGeomText);
			insertData.execute();
		}
	}

}
