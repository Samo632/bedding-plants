package uk.co.gmescouts.stmarys.beddingplants.data.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "geolocations")
@Data
@Builder
@EqualsAndHashCode(exclude = { "address" })
@NoArgsConstructor
@AllArgsConstructor
public class Geolocation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@JsonIgnore
	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Address address;

	private String formattedAddress;

	private Double latitude;

	private Double longitude;
}
