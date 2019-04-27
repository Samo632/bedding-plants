package uk.co.gmescouts.stmarys.beddingplants.data.model;

public interface PlantSummary {
	/**
	 * @return total number {@link Plant}s
	 */
	Integer getCount();

	/**
	 * @return total price of {@link Plant}s
	 */
	Double getPrice();

	/**
	 * @return total cost of {@link Plant}s
	 */
	Double getCost();
}
