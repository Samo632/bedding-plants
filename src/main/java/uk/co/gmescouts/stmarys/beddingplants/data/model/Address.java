package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@EqualsAndHashCode(exclude = { "customers", "geolocation" })
@ToString(exclude = { "customers" })
@NoArgsConstructor
@AllArgsConstructor
public class Address {
	@JsonIgnore
	@Id
	public String getId() {
		return this.getGeolocatableAddress().toUpperCase();
	}

	@SuppressWarnings("EmptyMethod")
    public void setId(final String id) {
		// intentionally blank, for Entity/Jackson construction only
	}

	@JsonIgnore
	@Transient
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

		return geo.toString();
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
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "address")
	private Set<Customer> customers = new TreeSet<>(Comparator.comparing(Customer::getForename));

	private Geolocation geolocation;

	public void addCustomer(final Customer customer) {
		if (customer != null) {
			// link Address to Customer
			customer.setAddress(this);

			// replace existing Customer, if present
			customers.remove(customer);
			customers.add(customer);
		}
	}
}
