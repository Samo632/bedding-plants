package uk.co.gmescouts.stmarys.beddingplants.imports.data.excel;

import org.apache.commons.lang3.StringUtils;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelRow;

import lombok.Data;

@Data
public class ExcelPlant {
	@ExcelRow
	private int rowIndex;

	@ExcelCellName("Id")
	private String id;

	@ExcelCellName("Plant Name")
	private String name;

	@ExcelCellName("Variety")
	private String variety;

	@ExcelCellName("No of Plants in tray")
	private String details;

	@ExcelCellName("Price inc VAT")
	private String price;

	@ExcelCellName("Cost ex VAT")
	private String cost;

	public boolean isValid() {
		return StringUtils.isNotBlank(id);
	}
}
