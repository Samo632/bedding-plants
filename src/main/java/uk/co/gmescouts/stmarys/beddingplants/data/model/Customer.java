package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "customers")
@Data
@Builder
@EqualsAndHashCode(exclude = { "orders" })
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(nullable = false)
	private String forename;

	@NonNull
	@Column(nullable = false)
	private String surname;

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Address address;

	private String emailAddress;

	private String telephone;

	@NonNull
	@JsonIgnore
	@Builder.Default
	@OrderBy("num")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "customer")
	private Set<Order> orders = new TreeSet<>(Comparator.comparingInt(Order::getNum));

	public void addOrder(final Order order) {
		if (order != null) {
			// replace existing Order, if present
			orders.remove(order);
			orders.add(order);
			order.setCustomer(this);
		}
	}
}
