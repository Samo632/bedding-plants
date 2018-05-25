package uk.co.gmescouts.stmarys.beddingplants.data;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Geolocation;

public interface GeolocationRepository extends JpaRepository<Geolocation, String> {
	Geolocation findByAddress(Address address);
}
