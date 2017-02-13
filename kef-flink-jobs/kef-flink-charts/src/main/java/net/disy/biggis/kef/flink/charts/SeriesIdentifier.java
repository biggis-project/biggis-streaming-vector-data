//Copyright (c) 2016 by Disy Informationssysteme GmbH
package net.disy.biggis.kef.flink.charts;

// NOT_PUBLISHED
public class SeriesIdentifier {

  private int laufendeNummer;
  private String name;
  private Measurement dataType;

  public SeriesIdentifier(int laufendeNummer, String name, Measurement dataType) {
    this.laufendeNummer = laufendeNummer;
    this.name = name;
	this.dataType = dataType;
  }

  public int getLaufendeNummer() {
    return laufendeNummer;
  }

  public String getName() {
    return name;
  }
  
  public Measurement getDataType() {
	return dataType;
}

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "[" + getLaufendeNummer() + ":" + getName() + "(" + getDataType().toString().toLowerCase()+")]";
  }
}
