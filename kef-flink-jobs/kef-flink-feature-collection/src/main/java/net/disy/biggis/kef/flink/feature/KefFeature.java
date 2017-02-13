package net.disy.biggis.kef.flink.feature;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

public class KefFeature implements Serializable {
	private static final long serialVersionUID = -7994428048977785889L;
	private final JsonNode geometry;
	private final JsonNode properties;

	public KefFeature(JsonNode geometry, JsonNode properties) {
		this.geometry = geometry;
		this.properties = properties;
	}

	public JsonNode getGeometry() {
		return geometry;
	}

	public JsonNode getProperties() {
		return properties;
	}
	
	@Override
	public String toString() {
		return "Geom: " + geometry.toString() + " / Properties: " + properties.toString();
	}
}
