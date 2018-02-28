// Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.charts;

import static java.text.MessageFormat.format;
import static java.util.OptionalInt.of;
import static net.disy.biggis.kef.flink.charts.ChartsJsonConstants.TIME_SERIES_DATE;
import static net.disy.biggis.kef.flink.charts.ChartsJsonConstants.TIME_SERIES_VALUE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalInt;


import org.apache.flink.api.java.tuple.Tuple2;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.disy.biggis.kef.flink.db.DbStreamTableSink;

// NOT_PUBLISHED
public class TimeSeriesTableSink extends DbStreamTableSink<Tuple2<SeriesIdentifier, JsonNode>> {

  private static final String MSG_INSERTED_SERIES_DESCRIPTION = "Inserted series description with id {0} for lfd nr {1}"; //$NON-NLS-1$
  private static final String MSG_FOUND_SERIES_DESCRIPTION = "Found series description with id {0} for lfd nr {1}"; //$NON-NLS-1$
  private static final String MSG_BULK_INSERT_DATA = "Inserted {0} values for series {1}"; //$NON-NLS-1$
  private static final String MSG_FAILURE_INSERT_DESCRIPTION = "Cannot insert time series description for series "; //$NON-NLS-1$

  private static final String SELECT_SERIES_DESCRIPTION_ID = "SELECT IDENTIFIER FROM kef_series_description WHERE LFD_NUMMER = ? AND NAME = ? AND SERIES_TYPE = ?"; //$NON-NLS-1$
  private static final String INSERT_SERIES_DESCTRIPTION = "INSERT INTO kef_series_description (LFD_NUMMER, NAME, SERIES_TYPE) VALUES (?, ?, ?) ON CONFLICT (LFD_NUMMER, NAME, SERIES_TYPE) DO NOTHING"; //$NON-NLS-1$
  private static final String INSERT_SERIES_DATA = "INSERT INTO kef_series_data (SERIES_ID, VALUE, DATE) VALUES (?,?, to_date(?, 'YYYY-MM-DD')) ON CONFLICT (SERIES_ID, DATE) DO UPDATE SET VALUE = excluded.VALUE"; //$NON-NLS-1$

  private static final long serialVersionUID = -7313701162117923308L;

  private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesTableSink.class);

  @Override
  protected void execute(Connection connection, Tuple2<SeriesIdentifier, JsonNode> series)
      throws Exception {
    int seriesId = ensureSeriesDescriptionIsPresent(connection, series);

    insertSeriesData(connection, seriesId, series.f1.iterator());
  }

  private int ensureSeriesDescriptionIsPresent(
      Connection connection,
      Tuple2<SeriesIdentifier, JsonNode> series) throws SQLException, Exception {
    SeriesIdentifier seriesIdentifier = series.f0;
    OptionalInt identifier = findSeriesDescription(connection, seriesIdentifier);
    if (!identifier.isPresent()) {
      identifier = insertSeriesDescription(connection, seriesIdentifier);
    }

    return identifier.orElseThrow(() -> new Exception(MSG_FAILURE_INSERT_DESCRIPTION
        + seriesIdentifier));
  }

  private void insertSeriesData(Connection connection, int seriesId, Iterator<JsonNode> points)
      throws SQLException {
    try (PreparedStatement insertData = connection.prepareStatement(INSERT_SERIES_DATA)) {
      while (points.hasNext()) {
        JsonNode current = points.next();
        insertData.setInt(1, seriesId);
        insertData.setInt(2, current.get(TIME_SERIES_VALUE).asInt());
        insertData.setString(3, current.get(TIME_SERIES_DATE).asText());
        insertData.addBatch();
      }
      int[] inserted = insertData.executeBatch();
      if (LOG.isDebugEnabled()) {
        int sum = Arrays.stream(inserted).filter(i -> i >= 0).sum();
        String message = format(MSG_BULK_INSERT_DATA, sum, seriesId);
        LOG.debug(message);
      }
    }
  }

  private OptionalInt findSeriesDescription(
      Connection connection,
      SeriesIdentifier seriesIdentifier)
      throws SQLException {
    try (PreparedStatement selectSeriesIdentifier = connection
        .prepareStatement(SELECT_SERIES_DESCRIPTION_ID)) {
      selectSeriesIdentifier.setInt(1, seriesIdentifier.getLaufendeNummer());
      selectSeriesIdentifier.setString(2, seriesIdentifier.getName());
      selectSeriesIdentifier.setInt(3, seriesIdentifier.getDataType().getIdentifier());
      try (ResultSet foundSeries = selectSeriesIdentifier.executeQuery()) {
        if (foundSeries.next()) {
          OptionalInt identifier = of(foundSeries.getInt("IDENTIFIER"));
          String message = format(
              MSG_FOUND_SERIES_DESCRIPTION,
              identifier.getAsInt(),
              seriesIdentifier.getLaufendeNummer());
          LOG.debug(message);
          return identifier;
        }
        return OptionalInt.empty();
      }
    }
  }

  private OptionalInt insertSeriesDescription(
      Connection connection,
      SeriesIdentifier seriesIdentifier) throws SQLException {
    try (PreparedStatement insertSeries = connection.prepareStatement(
        INSERT_SERIES_DESCTRIPTION,
        Statement.RETURN_GENERATED_KEYS)) {
      insertSeries.setInt(1, seriesIdentifier.getLaufendeNummer());
      insertSeries.setString(2, seriesIdentifier.getName());
      insertSeries.setInt(3, seriesIdentifier.getDataType().getIdentifier());
      insertSeries.executeUpdate();
      OptionalInt generatedKey = getGeneratedKey(insertSeries, 1);
      if (!generatedKey.isPresent()) {
        return findSeriesDescription(connection, seriesIdentifier);
      }
      generatedKey.ifPresent(key -> logInsertedDescription(seriesIdentifier, key));
      return generatedKey;
    }

  }

  private OptionalInt getGeneratedKey(PreparedStatement statement, int index) throws SQLException {
    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
      if (generatedKeys.next()) {
        return OptionalInt.of(generatedKeys.getInt(index));
      }
      return OptionalInt.empty();
    }
  }

  private void logInsertedDescription(SeriesIdentifier seriesIdentifier, int key) {
    LOG.debug(format(MSG_INSERTED_SERIES_DESCRIPTION, key, seriesIdentifier.getLaufendeNummer()));
  }
}
