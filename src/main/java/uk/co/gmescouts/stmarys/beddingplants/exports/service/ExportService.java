package uk.co.gmescouts.stmarys.beddingplants.exports.service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;
import uk.co.gmescouts.stmarys.beddingplants.sales.service.SalesService;

@Service
public class ExportService {
	@Resource
	private SalesService salesService;

	public byte[] exportSaleCustomersToPdf(@NotNull final Integer saleYear) {
		final Sale sale = salesService.findSaleByYear(saleYear);

		// TODO process Sale's Customer Orders into PDFs

		return null;
	}
}
