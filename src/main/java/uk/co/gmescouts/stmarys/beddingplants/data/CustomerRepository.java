package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	@OrderBy("surname")
	Set<Customer> findByForenameAndSurname(String forename, String surname);
}
