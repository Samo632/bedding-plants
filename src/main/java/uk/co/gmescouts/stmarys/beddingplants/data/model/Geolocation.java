package uk.co.gmescouts.stmarys.beddingplants.data.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = "geolocations")
@Data
@Builder
@EqualsAndHashCode(exclude = { "address" })
@ToString(exclude = { "address" })
@NoArgsConstructor
@AllArgsConstructor
public class Geolocation {
	@Id
	private String formattedAddress;

	@NonNull
	@NotNull
	@JsonIgnore
	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Address address;

	private Double latitude;

	private Double longitude;
}
