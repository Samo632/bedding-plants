package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = "addresses")
@Data
@Builder
@EqualsAndHashCode(exclude = { "customers" })
@ToString(exclude = { "customers" })
@NoArgsConstructor
@AllArgsConstructor
public class Address {
	@JsonIgnore
	@Id
	public String getGeolocatableAddress() {
		final StringBuilder geo = new StringBuilder(200);

		// house name/number
		geo.append(StringUtils.defaultIfEmpty(houseNameNumber, ""));

		// street
		if (StringUtils.isNotBlank(street)) {
			if (geo.length() > 0) {
				geo.append(" ");
			}
			geo.append(street);
		}

		// town
		if (StringUtils.isNotBlank(town)) {
			if (geo.length() > 0) {
				geo.append(", ");
			}
			geo.append(town);
		}

		// city
		if (StringUtils.isNotBlank(city)) {
			if (geo.length() > 0) {
				geo.append(", ");
			}
			geo.append(city);
		}

		// postcode
		if (StringUtils.isNotBlank(postcode)) {
			if (geo.length() > 0) {
				geo.append(", ");
			}
			geo.append(postcode);
		}

		return geo.toString().toUpperCase();
	}

	public void setGeolocatableAddress(final String geolocatableAddress) {
		// intentionally blank, for Entity/Jackson construction only
	}

	@JsonIgnore
	@Transient
	public boolean isGeolocatable() {
		return (StringUtils.isNotBlank(street) && StringUtils.isNotBlank(town)) || StringUtils.isNotBlank(postcode);
	}

	private String houseNameNumber;

	private String street;

	private String town;

	private String city;

	private String postcode;

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@NonNull
	@JsonIgnore
	@Builder.Default
	@OrderBy("surname, forename")
	@Access(AccessType.FIELD)
	// FIXME: problems with items being "MERGE"d at the EntityManger level?
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "address")
	private Set<Customer> customers = new TreeSet<>(Comparator.comparing(Customer::getForename));

	@Access(AccessType.FIELD)
	// FIXME: problems with items being "MERGE"d at the EntityManger level?
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
