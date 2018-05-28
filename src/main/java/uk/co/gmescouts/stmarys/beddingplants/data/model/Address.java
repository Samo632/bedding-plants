package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "addresses")
@Data
@Builder
@EqualsAndHashCode(exclude = { "customers" })
@NoArgsConstructor
@AllArgsConstructor
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String houseNameNumber;

	private String street;

	private String town;

	private String postcode;

	@NonNull
	@JsonIgnore
	@Builder.Default
	@OrderBy("surname, forename")
	@Access(AccessType.FIELD)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "address")
	private Set<Customer> customers = new TreeSet<>(Comparator.comparing(Customer::getForename));

	@Access(AccessType.FIELD)
	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "address")
	private Geolocation geolocation;

	public void addCustomer(final Customer customer) {
		if (customer != null) {
			// replace existing Customer, if present
			customers.remove(customer);
			customers.add(customer);
			customer.setAddress(this);
		}
	}
}
