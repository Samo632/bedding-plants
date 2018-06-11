package uk.co.gmescouts.stmarys.beddingplants.data;

import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;

public interface AddressRepository extends JpaRepository<Address, String> {
	@OrderBy("city, town, street, houseNameNumber")
	Set<Address> findAddressByCustomersSaleYear(Integer customersSaleYear);

	@OrderBy("city, town, street, houseNameNumber")
	Set<Address> findAddressByCustomersSaleYearAndCustomersOrdersType(Integer customersSaleYear, OrderType customersOrdersType);
}
