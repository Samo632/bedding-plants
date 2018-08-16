package uk.co.gmescouts.stmarys.beddingplants.sales.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class CustomerSummary {
	@NonNull
	private Integer orderCount;

	@NonNull
	private Double ordersCostTotal;

	@NonNull
	private Double ordersIncomeTotal;
}
