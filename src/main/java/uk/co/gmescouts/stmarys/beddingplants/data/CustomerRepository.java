package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	List<Customer> findByForename(String forename);

	List<Customer> findBySurname(String surname);

	List<Customer> findByAddress(Address address);
}
