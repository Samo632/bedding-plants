package uk.co.gmescouts.stmarys.beddingplants.data.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "orders")
@Data
@Builder
@EqualsAndHashCode(of = { "customer", "num" })
@ToString(exclude = { "customer" })
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	@JsonIgnore
	@Id
	public Long getId() {
		// key on the Sale year and Order num
		return Long.valueOf(String.format("%04d%03d", customer.getSale().getYear(), this.num));
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
	private Customer customer;

	@NonNull
	@NotNull
	@Enumerated(EnumType.STRING)
	private DeliveryDay deliveryDay;

	@NonNull
	@NotNull
	@Enumerated(EnumType.STRING)
	private OrderType type;

	private String courtesyOfName;

	private String notes;

	private Float paid;

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@NonNull
	@Builder.Default
	@OrderBy("plant")
	@Access(AccessType.FIELD)
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "order")
	private Set<OrderItem> orderItems = new TreeSet<>(Comparator.comparingInt(oi -> oi.getPlant().getNum()));

	public void addOrderItem(final OrderItem orderItem) {
		if (orderItem != null) {
			// link Order to OrderItem
			orderItem.setOrder(this);

			// replace existing OrderItem, if present
			orderItems.add(orderItem);
		}
	}
}
