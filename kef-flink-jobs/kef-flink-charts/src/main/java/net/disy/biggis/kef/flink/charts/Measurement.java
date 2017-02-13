package net.disy.biggis.kef.flink.charts;

import java.util.Arrays;

public enum Measurement {
	
	FALLENFAENGE(1, "kefcharts-ff"), EIABLAGE_BEEREN(2, "kefcharts-ei-beeren"), EIABLAGE_FUNDE(3, "kefcharts-ei-funde");

	private int identifier;
	private String startsWith;
	
	private Measurement(int identifier, String startsWith) {
		this.identifier = identifier;
		this.startsWith = startsWith;
	}
	
	public int getIdentifier() {
		return identifier;
	}

	public static Measurement parse(String topic) {
		return Arrays.stream(values()).filter(m -> topic.startsWith(m.startsWith)).findFirst().orElse(null);
	}
}
