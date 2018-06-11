package uk.co.gmescouts.stmarys.beddingplants.exports.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.StaticMapsApi;
import com.google.maps.StaticMapsRequest.ImageFormat;
import com.google.maps.StaticMapsRequest.Markers;
import com.google.maps.StaticMapsRequest.Markers.MarkersSize;
import com.google.maps.StaticMapsRequest.StaticMapType;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.LocationType;
import com.google.maps.model.Size;
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

@Service
public class ExportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

	@Resource
	private OrderRepository orderRepository;

	@Resource
	private PlantRepository plantRepository;

	@Resource
	private AddressRepository addressRepository;

	@Resource
	private RestTemplate restTemplate;

	@Resource
	private GeoApiContext geoApiContext;

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

	public Set<Address> getSaleAddresses(@NotNull final Integer saleYear, final OrderType orderType) {
		// get Addresses for specified Sale Year/OrderType
		Set<Address> addresses;
		if (orderType != null) {
			addresses = addressRepository.findAddressByCustomersSaleYearAndCustomersOrdersType(saleYear, orderType);
		} else {
			addresses = addressRepository.findAddressByCustomersSaleYear(saleYear);
		}
		return addresses;
	}

	public byte[] exportSaleCustomersToPdf(@NotNull final Integer saleYear, final OrderType orderType) throws IOException {
		LOGGER.info("Exporting Customer Orders for Sale [{}]", saleYear);

		// setup the URLs
		final String exportHostUrl = getExportHostUrl();
		final String exportHtmlUrl = String.format("%s%s%s%s", exportHostUrl, baseUri,
				ExportHtml.EXPORT_CUSTOMER_ORDERS_HTML.replace("{saleYear}", saleYear.toString()),
				orderType == null ? "" : String.format("?orderType=%s", orderType.toString()));
		LOGGER.debug("Calling HTML Export URL [{}]", exportHtmlUrl);

		// get the HTML via external call
		final String html = restTemplate.getForObject(exportHtmlUrl, String.class, saleYear);

		// convert HTML to PDF
		byte[] pdf = null;
		try {
			// converter properties (image/css locations)
			final ConverterProperties converterProperties = new ConverterProperties();
			converterProperties.setBaseUri(exportHostUrl);

			// writer properties (compression)
			final WriterProperties writerProperties = new WriterProperties();
			writerProperties.setCompressionLevel(CompressionConstants.BEST_COMPRESSION);

			// converter
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PdfWriter pdfWriter = new PdfWriter(baos, writerProperties);
			HtmlConverter.convertToPdf(html, pdfWriter, converterProperties);
			pdf = baos.toByteArray();
		} catch (final PdfException e) {
			LOGGER.error("Unable to generate PDF from HTML: {}", e.getMessage(), e);
		}

		// return converted PDF (if any)
		return pdf;
	}

	public byte[] exportSaleAddressesToGoogleMap(@NotNull final Integer saleYear, final OrderType orderType)
			throws ApiException, InterruptedException, IOException {
		// get the Addresses
		Set<Address> addresses = getSaleAddresses(saleYear, orderType);

		// geolocate Address(es), if not already
		if (CollectionUtils.isNotEmpty(addresses)) {
			addresses.forEach(address -> {
				geolocateAddress(address);
			});

			// save these to the database
			addresses = new HashSet<>(addressRepository.saveAll(addresses));
		}

		// generate the image
		return getGoogleMapsImage(addresses);
	}

	private byte[] getGoogleMapsImage(@NotNull final Set<Address> addresses) throws ApiException, InterruptedException, IOException {
		// TODO: separate (Geolocation?) Service

		LOGGER.debug("Generating Google Map for [{}] Addresses", addresses.size());

		byte[] mapImg = null;
		if (CollectionUtils.isNotEmpty(addresses)) {
			// determine viewer bounds (max of all Addresses)
			// TODO: needed anywhere? (delete the data.model.Bounds class too if not)
			// @formatter:off@
//			final Bounds maxBounds = addresses.stream().map(Address::getGeolocation).map(Geolocation::getBounds).reduce(new Bounds(), (b1, b2) -> {
//				// north-east
//				if (b1.getNorth() < b2.getNorth()) {
//					b1.setNorth(b2.getNorth());
//				}
//				if (b1.getEast() < b2.getEast()) {
//					b1.setEast(b2.getEast());
//				}
//
//				// south-west
//				if (b1.getSouth() > b2.getSouth()) {
//					b1.setSouth(b2.getSouth());
//				}
//				if (b1.getWest() > b2.getWest()) {
//					b1.setWest(b2.getWest());
//				}
//
//				return b1;
//			});
			// @formatter:on@

			// build up request to Google Maps for Markers
			final Markers markers = new Markers();
			// FIXME: use config for settings
			markers.size(MarkersSize.small);
			markers.color("yellow");

			addresses.stream().map(Address::getGeolocation).filter(Objects::nonNull).forEach(geolocation -> {
				final LatLng latLng = new LatLng();
				latLng.lat = geolocation.getLatitude();
				latLng.lng = geolocation.getLongitude();

				markers.addLocation(latLng);
			});

			// FIXME: use configuration for settings
			mapImg = StaticMapsApi.newRequest(geoApiContext, new Size(640, 435))//
					.scale(2) //
					.maptype(StaticMapType.roadmap)//
					.format(ImageFormat.png)//
					.language("en-GB")//
					.markers(markers)//
					.await().imageData;
		}

		return mapImg;
	}

	private void geolocateAddress(@NotNull final Address address) {
		// TODO: do this in a separate GeolocationService?
		if (address.getGeolocation() == null && address.isGeolocatable()) {
			final String geolocatableAddress = address.getGeolocatableAddress();
			LOGGER.debug("Geolocation Address [{}]", geolocatableAddress);

			final Geolocation geolocation = new Geolocation();
			try {
				final GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, geolocatableAddress).await();

				if (ArrayUtils.isNotEmpty(results)) {
					// look for a ROOFTOP match first
					Optional<GeocodingResult> result = Arrays.stream(results)
							.filter(r -> r.geometry != null && LocationType.ROOFTOP.equals(r.geometry.locationType)).findFirst();

					// then look for non-partial matches
					if (!result.isPresent()) {
						result = Arrays.stream(results).filter(r -> !r.partialMatch).findFirst();
					}

					// fall back to the first entry in the result list
					if (!result.isPresent()) {
						result = Optional.of(results[0]);
					}

					final GeocodingResult selectedResult = result.get();

					// set the Geolocation on the Address, assuming something found (otherwise Address not updated)
					if (StringUtils.isNotEmpty(selectedResult.formattedAddress)) {
						geolocation.setFormattedAddress(selectedResult.formattedAddress);

						geolocation.setLatitude(selectedResult.geometry.location.lat);
						geolocation.setLongitude(selectedResult.geometry.location.lng);

						// TODO: needed anywhere?
						// @formatter:off@
//						geolocation.setBounds( //
//								new Bounds( //
//										selectedResult.geometry.bounds.northeast.lat, // north
//										selectedResult.geometry.bounds.northeast.lng, // east
//										selectedResult.geometry.bounds.southwest.lat, // south
//										selectedResult.geometry.bounds.southwest.lng // west
//								)//
//						);
						// @formatter:on@

						address.setGeolocation(geolocation);
					}
				}
			} catch (IllegalStateException | ApiException | InterruptedException | IOException e) {
				LOGGER.warn(String.format("Unable to geocode address [%s]: %s", geolocatableAddress, e.getMessage()), e);
			}
		}
	}
}
