package uk.co.gmescouts.stmarys.beddingplants.exports.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.maps.StaticMapsRequest.ImageFormat;
import com.google.maps.StaticMapsRequest.Markers.MarkersSize;
import com.google.maps.StaticMapsRequest.StaticMapType;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;

import uk.co.gmescouts.stmarys.beddingplants.data.AddressRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.OrderRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.PlantRepository;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Geolocation;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderItem;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.exports.ExportHtml;
import uk.co.gmescouts.stmarys.beddingplants.geolocation.service.GeolocationService;

@Service
public class ExportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

	@Resource
	private GeolocationService geolocationService;

	@Resource
	private OrderRepository orderRepository;

	@Resource
	private PlantRepository plantRepository;

	@Resource
	private AddressRepository addressRepository;

	@Resource
	private RestTemplate restTemplate;

	@Value("${server.ssl.enabled:false}")
	private boolean httpsEnabled;

	private final String hostname;

	@Value("${server.port}")
	private int port;

	@Value("${server.servlet.context-path}")
	private String baseUri;

	public ExportService() throws UnknownHostException {
		hostname = InetAddress.getLocalHost().getHostName();
	}

	private String getExportHostUrl() {
		return String.format("%s://%s:%d", httpsEnabled ? "https" : "http", hostname, port);
	}

	public Set<Order> getSaleCustomerOrders(@NotNull final Integer saleYear, final OrderType orderType) {
		LOGGER.info("Get Customer Orders for Sale [{}]", saleYear);

		Set<Order> orders;
		if (orderType == null) {
			orders = orderRepository.findByCustomerSaleYear(saleYear);
		} else {
			orders = orderRepository.findByTypeAndCustomerSaleYear(orderType, saleYear);
		}

		return orders;
	}

	public Set<Plant> getSalePlants(@NotNull final Integer saleYear) {
		LOGGER.info("Get Plants for Sale [{}]", saleYear);

		return plantRepository.findBySaleYear(saleYear);
	}

	public Integer orderPlantCount(@NotNull final Order order) {
		return order.getOrderItems().stream().mapToInt(OrderItem::getCount).sum();
	}

	public Integer orderPlantAmount(@NotNull final Order order, @NotNull final Plant plant) {
		final Optional<OrderItem> orderItem = order.getOrderItems().stream().filter(oi -> oi.getPlant().equals(plant)).findFirst();

		Integer amount = null;
		if (orderItem.isPresent()) {
			amount = orderItem.get().getCount();
		}
		return amount;
	}

	public Set<Address> getSaleAddresses(@NotNull final Integer saleYear, final OrderType orderType, final boolean geolocatedOnly) {
		// get Addresses for specified Sale Year/OrderType
		Set<Address> addresses;
		if (orderType != null) {
			addresses = addressRepository.findAddressByCustomersSaleYearAndCustomersOrdersType(saleYear, orderType);
		} else {
			addresses = addressRepository.findAddressByCustomersSaleYear(saleYear);
		}

		// filter to geolocated addresses only if so requested
		if (geolocatedOnly) {
			// geolocate Address(es), if not already
			if (CollectionUtils.isNotEmpty(addresses)) {
				addresses.forEach(this::geolocateAddress);

				// save these to the database
				addresses = new HashSet<>(addressRepository.saveAll(addresses));
			}

			addresses = addresses.stream().filter(this::isAddressGeolocated).collect(Collectors.toSet());
		}

		return addresses;
	}

	public byte[] exportSaleCustomersToPdf(@NotNull final Integer saleYear, final OrderType orderType) throws IOException {
		LOGGER.info("Exporting Customer Orders for Sale [{}]", saleYear);

		// setup the URLs
		final String exportHostUrl = getExportHostUrl();
		final String exportHtmlUrl = String.format("%s%s%s?%s", exportHostUrl, baseUri, ExportHtml.EXPORT_CUSTOMER_ORDERS_HTML,
				orderType == null ? "" : String.format("orderType=%s", orderType.toString()));
		LOGGER.debug("Calling HTML Export URL [{}]", exportHtmlUrl);

		// get the HTML via external call
		final String html = restTemplate.getForObject(exportHtmlUrl, String.class, saleYear);

		// convert HTML to PDF
		byte[] pdf = null;

		// converter properties (image/css locations)
		final ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setBaseUri(exportHostUrl);

		// writer properties (compression)
		final WriterProperties writerProperties = new WriterProperties();
		writerProperties.setCompressionLevel(CompressionConstants.BEST_COMPRESSION);

		// converter
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			final PdfWriter pdfWriter = new PdfWriter(baos, writerProperties);
			HtmlConverter.convertToPdf(html, pdfWriter, converterProperties);
			pdf = baos.toByteArray();
		} catch (final PdfException e) {
			LOGGER.error("Unable to generate PDF from HTML: {}", e.getMessage(), e);
		}

		// return converted PDF (if any)
		return pdf;
	}

	public byte[] exportGeolocatedSaleAddressesToGoogleMap(@NotNull final Integer saleYear, final OrderType orderType,
			@NotNull final ImageFormat imageFormat, @NotNull final StaticMapType staticMapType, @NotNull final MarkersSize markersSize,
			@NotNull final String markersColour) throws ApiException, InterruptedException, IOException {
		// get the (geolocated) Addresses
		final Set<Address> addresses = getSaleAddresses(saleYear, orderType, true);

		// generate the image
		return plotAddressesOnMap(addresses, imageFormat, staticMapType, markersSize, markersColour);
	}

	private boolean isAddressGeolocated(@NotNull final Address address) {
		return address.getGeolocation() != null && StringUtils.isNotBlank(address.getGeolocation().getFormattedAddress());
	}

	private byte[] plotAddressesOnMap(@NotNull final Set<Address> addresses, @NotNull final ImageFormat imageFormat,
			@NotNull final StaticMapType staticMapType, @NotNull final MarkersSize markersSize, @NotNull final String markersColour)
			throws ApiException, InterruptedException, IOException {
		LOGGER.debug("Generating Google Map Image for [{}] Addresses", addresses.size());

		byte[] mapImg = null;
		if (CollectionUtils.isNotEmpty(addresses)) {
			// build up points to be added to the image
			final Set<LatLng> points = addresses.stream().map(Address::getGeolocation).filter(Objects::nonNull).map(geolocation -> {
				final LatLng latLng = new LatLng();
				latLng.lat = geolocation.getLatitude();
				latLng.lng = geolocation.getLongitude();
				return latLng;
			}).collect(Collectors.toSet());

			// get the image
			mapImg = geolocationService.plotPointsOnGoogleMapImage(points, imageFormat, staticMapType, markersSize, markersColour);
		}

		return mapImg;
	}

	private void geolocateAddress(@NotNull final Address address) {
		if (address.getGeolocation() == null && address.isGeolocatable()) {
			final String geolocatableAddress = address.getGeolocatableAddress();
			LOGGER.debug("Geolocatable Address [{}]", geolocatableAddress);

			final Geolocation geolocation = geolocationService.geolocateGeolocatableAddress(geolocatableAddress);
			if (geolocation != null && StringUtils.isNoneBlank(geolocation.getFormattedAddress())) {
				address.setGeolocation(geolocation);
			}
		}
	}
}
