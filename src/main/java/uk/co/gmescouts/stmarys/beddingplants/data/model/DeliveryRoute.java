package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = "deliveryRoutes")
@Data
@Builder
@EqualsAndHashCode(of = { "sale", "num" })
@ToString(exclude = { "sale" })
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRoute {
	@JsonIgnore
	@Id
	public Long getId() {
		// key on the Sale year and DeliveryRoute num
		return Long.valueOf(String.format("%04d%03d", sale.getYear(), this.num));
	}

	@SuppressWarnings("EmptyMethod")
	public void setId(final Long id) {
		// intentionally blank, only needed for Hibernate
	}

	@NonNull
	@NotNull
	@Min(1)
	private Integer num;

	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToOne
	private Sale sale;

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@NonNull
	@Builder.Default
	@OrderBy("order")
	@Access(AccessType.FIELD)
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "deliveryRoute")
	private Set<Order> orders = new TreeSet<>(Comparator.comparingInt(Order::getNum));

	public void addOrder(final Order order) {
		if (order != null) {
			// link DeliveryRoute to Order
			order.setDeliveryRoute(this);

			// replace existing Order, if present
			orders.add(order);
		}
	}
}
