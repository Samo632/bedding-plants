package uk.co.gmescouts.stmarys.beddingplants.data.model;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bounds {
	@NonNull
	private Double north;

	@NonNull
	private Double east;

	@NonNull
	private Double south;

	@NonNull
	private Double west;
}
