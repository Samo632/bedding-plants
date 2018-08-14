package uk.co.gmescouts.stmarys.beddingplants.exports.data.model;

import lombok.Data;
import lombok.NonNull;
import uk.co.gmescouts.stmarys.beddingplants.geolocation.data.model.MapMarkerColour;
import uk.co.gmescouts.stmarys.beddingplants.geolocation.data.model.MapMarkerSize;

@Data
public class GeolocatedPoint {
	@NonNull
	private Double lat;

	@NonNull
	private Double lng;

	@NonNull
	private String description;

	private MapMarkerSize mapMarkerSize;

	private MapMarkerColour mapMarkerColour;
}
