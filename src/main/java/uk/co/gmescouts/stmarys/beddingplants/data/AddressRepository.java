package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
	List<Address> findByTown(String town);

	List<Address> findByPostcode(String postcode);
}
