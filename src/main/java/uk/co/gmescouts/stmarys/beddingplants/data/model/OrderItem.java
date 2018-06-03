package uk.co.gmescouts.stmarys.beddingplants.data.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@Table(name = "orderitems")
@Data
@Builder
@EqualsAndHashCode(exclude = { "count" })
@ToString(exclude = { "order" })
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
	@JsonIgnore
	@Id
	public Long getId() {
		// key on the Sale year, Order num and Plant num
		return Long.valueOf(String.format("%d%03d%02d", order.getCustomer().getSale().getYear(), order.getNum(), plant.getNum()));
	}

	public void setId(final Long id) {
		// intentionally blank, only needed for Hibernate
	}

	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToOne
	private Order order;

	@NonNull
	@Access(AccessType.FIELD)
	@ManyToOne
	private Plant plant;

	@NonNull
	@Min(1)
	@NotNull
	private Integer count;
}
