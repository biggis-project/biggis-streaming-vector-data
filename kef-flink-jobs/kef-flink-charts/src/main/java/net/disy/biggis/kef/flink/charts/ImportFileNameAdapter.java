//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.charts;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// NOT_PUBLISHED
public class ImportFileNameAdapter {

  private static final Pattern IDENTIFIER = Pattern.compile("-(\\d+)\\."); //$NON-NLS-1$
  private static final Pattern YEAR = Pattern.compile("\\\\(\\d+)\\\\"); //$NON-NLS-1$
  private String fileName;

  public ImportFileNameAdapter(String fileName) {
    this.fileName = fileName;
  }

  public int getYear() {
    return getIntForPattern(YEAR);
  }

  private int getIntForPattern(Pattern pattern) {
    Matcher matcher = pattern.matcher(fileName);
    matcher.find();
    String year = matcher.group(1);
    return Integer.parseInt(year);
  }

  public int getIdentifier() {
    return getIntForPattern(IDENTIFIER);
  }

  public SampleType getSampleType() {
    return findValueToIndex(0, SampleType.values());
  }


  private <T extends Identifiable> T findValueToIndex(int index, T[] values) {
    String type = fileName.split("\\\\")[index]; //$NON-NLS-1$
    return Stream
        .of(values)
        .filter(t -> t.getIdentifier().equalsIgnoreCase(type))
        .findFirst()
        .get();
  };

  private interface Identifiable {
    String getIdentifier();
  }

  @SuppressWarnings("nls")
  public static enum SampleType implements Identifiable {
    CHART("charts"), MAP("maps");

    private String identifier;

    private SampleType(String identifier) {
      this.identifier = identifier;
    }

    public String getIdentifier() {
      return identifier;
    }
  }

}
