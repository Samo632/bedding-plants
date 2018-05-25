package uk.co.gmescouts.stmarys.beddingplants.imports.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.poiji.bind.Poiji;
import com.poiji.exception.PoijiExcelType;
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder;

import uk.co.gmescouts.stmarys.beddingplants.data.model.Address;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Customer;
import uk.co.gmescouts.stmarys.beddingplants.data.model.DeliveryDay;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Geolocation;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Order;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderItem;
import uk.co.gmescouts.stmarys.beddingplants.data.model.OrderType;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Plant;
import uk.co.gmescouts.stmarys.beddingplants.data.model.Sale;
import uk.co.gmescouts.stmarys.beddingplants.imports.configuration.ImportConfiguration;
import uk.co.gmescouts.stmarys.beddingplants.imports.data.excel.OrderImport;
import uk.co.gmescouts.stmarys.beddingplants.imports.data.excel.PlantImport;

public class ImportService {
	@Resource
	private PoijiOptionsBuilder poijiOptionsBuilder;

	@Resource
	private ImportConfiguration importConfiguration;

	@Resource
	private GeoApiContext geoApiContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportService.class);

	private final static Map<String, String> ADDRESS_CONTRACTIONS;
	static {
		final Map<String, String> contractions = new HashMap<>(10, 1);
		contractions.put(" st", " Street");
		contractions.put(" ave", " Avenue");
		contractions.put(" rd", " Road");
		contractions.put(" dv", " Drive");
		contractions.put(" cres", " Crescent");
		contractions.put(" cl", " Close");
		contractions.put(" ln", " Lane");
		contractions.put(" terr", " Terrace");
		contractions.put(" gv", " Grove");

		ADDRESS_CONTRACTIONS = Collections.unmodifiableMap(contractions);
	}

	private final static Map<Method, Method> IMPORTED_METHOD_CACHE = new HashMap<>(100, 1);

	public Sale importSaleFromExcelFile(final MultipartFile file, final int year, final float vat)
			throws InvalidFormatException, EncryptedDocumentException, IOException {
		LOGGER.info("Importing Sale from file [{}] for Order Year [{}] with VAT [{}]", file.getOriginalFilename(), year, vat);

		// create the overall Sale
		final Sale sale = Sale.builder().year(year).vat(vat).build();

		// Plants (add to Sale)
		final Set<Plant> plants = importPlantsFromExcelFile(file);
		if (plants != null) {
			plants.stream().forEach(plant -> sale.addPlant(plant));

			// Orders (geolocate and add to Sale)
			final Set<Order> orders = importOrdersFromExcelFile(file, plants);
			if (orders != null) {
				orders.stream().forEach(order -> {
					geolocateOrderAddress(order);
					sale.addOrder(order);
				});
			}
		}

		return sale;

	}

	public Set<Order> importOrdersFromExcelFile(final MultipartFile file, final Set<Plant> plants)
			throws InvalidFormatException, EncryptedDocumentException, IOException {
		LOGGER.info("Importing Orders from file [{}]", file.getOriginalFilename());

		// get Workbook from file (ensure we can read it and determine the type)
		final Workbook workbook = WorkbookFactory.create(file.getInputStream());

		// Order Imports
		final List<OrderImport> importedOrders = readDataFromExcelFile(file.getInputStream(), workbook, importConfiguration.getOrderImportsName(),
				OrderImport.class);

		// convert to Orders
		final Set<Order> orders = importedOrders.stream().filter(OrderImport::isValid).map(order -> createOrder(order, plants))
				.collect(Collectors.toSet());
		LOGGER.info("Imported [{}] valid orders", orders.size());

		return orders;
	}

	public Set<Plant> importPlantsFromExcelFile(final MultipartFile file) throws InvalidFormatException, EncryptedDocumentException, IOException {
		LOGGER.info("Importing Plants from file [{}]", file.getOriginalFilename());

		// get Workbook from file (ensure we can read it and determine the type)
		final Workbook workbook = WorkbookFactory.create(file.getInputStream());

		// Plant Imports
		final List<PlantImport> importedPlants = readDataFromExcelFile(file.getInputStream(), workbook, importConfiguration.getPlantImportsName(),
				PlantImport.class);

		// convert to Plants
		final Set<Plant> plants = importedPlants.stream().filter(PlantImport::isValid).map(plant -> createPlant(plant)).collect(Collectors.toSet());
		LOGGER.info("Imported [{}] valid Plants", plants.size());

		return plants;
	}

	private Plant createPlant(final PlantImport plantImport) {
		LOGGER.trace("Convert imported Plant: [{}]", plantImport);

		final Float price = StringUtils.isNotBlank(plantImport.getPrice()) ? Float.valueOf(plantImport.getPrice().replaceFirst("£", "")) : 0f;
		final Float cost = StringUtils.isNotBlank(plantImport.getCost()) ? Float.valueOf(plantImport.getCost().replaceFirst("£", "")) : 0f;

		return Plant.builder().num(Integer.valueOf(plantImport.getId())).name(plantImport.getName()).variety(plantImport.getVariety())
				.details(plantImport.getDetails()).price(price).cost(cost).build();
	}

	private Order createOrder(final OrderImport orderImport, final Set<Plant> plants) {
		LOGGER.trace("Convert imported Order: [{}]", orderImport);

		// create Customer
		final Customer customer = createCustomer(orderImport);

		// determine DeliveryDay (default is Saturday if not present)
		final DeliveryDay deliveryDay = StringUtils.isNotBlank(orderImport.getDeliveryDay())
				? DeliveryDay.valueOf(StringUtils.capitalize(orderImport.getDeliveryDay()))
				: DeliveryDay.Saturday;

		// create Order (without Customer or OrderItems)
		final Order order = Order.builder().num(Integer.valueOf(orderImport.getOrderNumber())).deliveryDay(deliveryDay)
				.orderType(OrderType.valueOf(orderImport.getCollectDeliver().toUpperCase().charAt(0))).build();

		// link the Order with the Customer
		customer.addOrder(order);

		// determine requested number of each plant and create OrderItems on the Order
		for (final Plant plant : plants) {
			final int plantId = plant.getNum();
			try {
				// for each available (imported) Plant, check how many the imported order wants
				final String plantCountStr = (String) orderImport.getClass().getMethod(String.format("getNumberPlants%d", plantId))
						.invoke(orderImport);

				final Integer numPlants = StringUtils.isNotBlank(plantCountStr) ? Integer.valueOf(plantCountStr) : 0;

				if (numPlants > 0) {
					// add the OrderItem to the Order
					order.addOrderItem(OrderItem.builder().plant(plant).count(numPlants).build());
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalStateException(String.format("Unable to determine requested number of plants [%d]: %s", plantId, e.getMessage()), e);
			}
		}

		return order;
	}

	protected String normaliseTelephoneNumber(final String telephone) {
		String normalised = null;

		// normalise the telephone format, e.g. "0161 370 3070", "07867 123 456"
		if (StringUtils.isNotBlank(telephone)) {
			// remove any existing spaces
			final StringBuilder telephoneBuilder = new StringBuilder(telephone.replaceAll("\\s+", ""));

			// 7 digits -> local landline, prepend "0161" (4-3-4)
			if (telephoneBuilder.length() == 7) {
				telephoneBuilder.insert(0, "0161");
			}
			// add leading "0" if not present
			else if (!telephone.startsWith("0")) {
				telephoneBuilder.insert(0, "0");
			}

			// 11 digits -> mobile/landline (4-3-4)
			if (telephoneBuilder.length() == 11) {
				telephoneBuilder.insert(7, " ").insert(4, " ");
			}
			// other -> unknown,
			else {
				// just add a space somewhere in the middle so leading 0 digits aren't lost if re-imported to Excel
				telephoneBuilder.insert(telephone.length() / 2, " ");
			}
			normalised = telephoneBuilder.toString();
		}

		return normalised;
	}

	private Customer createCustomer(final OrderImport orderImport) {
		// create Address
		final Address address = createAddress(orderImport);

		// create Customer (without Address)
		final Customer customer = Customer.builder().forename(orderImport.getForename()).surname(orderImport.getSurname())
				.emailAddress(orderImport.getEmailAddress()).telephone(normaliseTelephoneNumber(orderImport.getTelephone())).build();

		// link Customer with Address
		address.addCustomer(customer);

		return customer;
	}

	private Address createAddress(final OrderImport orderImport) {
		String street = orderImport.getStreet();
		if (StringUtils.isNotBlank(orderImport.getStreet())) {
			street = ADDRESS_CONTRACTIONS.entrySet().stream()
					// find if any contractions match the end of the imported street
					.filter(contraction -> orderImport.getStreet().toLowerCase().endsWith(contraction.getKey()))
					// replace the contraction with the full street ending
					.map(contraction -> orderImport.getStreet().replaceFirst(String.format("%s$", contraction.getKey()), contraction.getValue()))
					// there can be only one (or none, in which case stick with the original value)...
					.findFirst().orElse(street);
		}

		// normalise postcode
		final StringBuilder postcodeBuilder = new StringBuilder(orderImport.getPostcode().replaceAll("\\s+", ""));
		postcodeBuilder.insert(postcodeBuilder.length() > 3 ? postcodeBuilder.length() - 3 : 0, " ");

		return Address.builder().houseNameNumber(orderImport.getHouseNameNumber()).street(street).town(orderImport.getTown())
				.postcode(postcodeBuilder.toString()).build();
	}

	private boolean isAddressGeolocatable(final Address address) {
		return address != null && ((StringUtils.isNotBlank(address.getStreet()) && StringUtils.isNotBlank(address.getTown()))
				|| StringUtils.isNotBlank(address.getPostcode()));
	}

	private String getGeolocatableAddress(final Address address) {
		final StringBuilder geo = new StringBuilder(100);

		// house name/number
		if (StringUtils.isNotBlank(address.getHouseNameNumber())) {
			geo.append(address.getHouseNameNumber());
		}

		// street
		if (StringUtils.isNotBlank(address.getStreet())) {
			if (geo.length() > 0) {
				geo.append(" ");
			}
			geo.append(address.getStreet());
		}

		// town
		if (StringUtils.isNotBlank(address.getTown())) {
			if (geo.length() > 0) {
				geo.append(", ");
			}
			geo.append(address.getTown());
		}

		// postcode
		if (StringUtils.isNotBlank(address.getPostcode())) {
			if (geo.length() > 0) {
				geo.append(", ");
			}
			geo.append(address.getPostcode());
		}

		return geo.toString();
	}

	private void geolocateOrderAddress(final Order order) {
		// TODO: do this in a separate GeolocationService?

		final Address address = order.getCustomer().getAddress();
		if (isAddressGeolocatable(address)) {
			// TODO: check for existing, geolocate if none
			final String geolocatableAddress = getGeolocatableAddress(address);
			final Geolocation geolocation = Geolocation.builder().address(address).build();
			try {
				final GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, geolocatableAddress).await();

				if (results != null & results.length > 0) {
					// TODO: some logic as maybe we don't want the first result every time?
					final GeocodingResult result = results[0];
					geolocation.setFormattedAddress(result.formattedAddress);
					geolocation.setLatitude(result.geometry.location.lat);
					geolocation.setLongitude(result.geometry.location.lng);
				}
			} catch (IllegalStateException | ApiException | InterruptedException | IOException e) {
				LOGGER.warn(String.format("Unable to geocode address [%s] for order [%d]: %s", geolocatableAddress, order.getNum(), e.getMessage()),
						e);
			}
			address.setGeolocation(geolocation);
		}
	}

	private String normaliseField(final String field) {
		// convert blank strings to nulls
		String normalised = null;

		// trim multiple spaces (anywhere in a String)
		if (StringUtils.isNotBlank(field)) {
			normalised = field.replaceAll("\\s{2,}", " ");
		}

		return normalised;
	}

	private Method findSetter(final Object imported, final Method getter) {
		Method setter = IMPORTED_METHOD_CACHE.get(getter);

		if (setter == null) {
			final String setterName = getter.getName().replaceFirst("^get", "set");
			try {
				setter = imported.getClass().getMethod(setterName, String.class);
				IMPORTED_METHOD_CACHE.put(getter, setter);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalStateException(String.format("Unable to find setter [%s] for imported object [%s]: %s", setterName,
						imported.getClass().getSimpleName(), e.getMessage()), e);
			}
		}

		return setter;
	}

	private <T> void normaliseImportedFields(final T imported) {
		Arrays.stream(imported.getClass().getDeclaredMethods())
				// find the getter methods on the import object (public accessible returning Strings)
				.filter(method -> method.getName().startsWith("get") && method.canAccess(imported) && String.class.equals(method.getReturnType()))
				// normalise the value and set back on the object
				.forEach(getter -> {
					try {
						// get original value
						final String str = (String) getter.invoke(imported);

						// normalise the value
						final String normalised = normaliseField(str);

						// if the value was actually changed, set the value back on the imports object
						if (!StringUtils.equals(str, normalised)) {
							// find the equivalent setter method on the imports object
							final Method setter = findSetter(imported, getter);

							setter.invoke(imported, normalised);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new IllegalStateException(String.format("Unable to normalise field value [%s] for imported object [%s]: %s",
								getter.getName().replaceFirst("^get", ""), imported.getClass().getSimpleName(), e.getMessage()), e);
					}
				});
	}

	private <T> List<T> readDataFromExcelFile(final InputStream inputStream, final Workbook workbook, final String sheetName, final Class<T> dataType)
			throws InvalidFormatException {
		// determine Excel Type (if valid)
		final PoijiExcelType excelType = getPoijiExcelType(workbook);

		// get sheet index from name
		final int index = getSheetIndexByName(workbook, sheetName);

		// read the data from the file
		final List<T> data = Poiji.fromExcel(inputStream, excelType, dataType, poijiOptionsBuilder.sheetIndex(index).build());
		LOGGER.info("Read [{}] records of type [{}]", data.size(), dataType.getSimpleName());

		// normalise all fields for each imported datum
		data.stream().forEach(datum -> normaliseImportedFields(datum));

		return data;
	}

	private PoijiExcelType getPoijiExcelType(final Workbook workbook) throws InvalidFormatException {
		PoijiExcelType poijiExcelType;

		final SpreadsheetVersion sv = workbook.getSpreadsheetVersion();
		switch (sv) {
		case EXCEL2007:
			poijiExcelType = PoijiExcelType.XLSX;
			break;
		case EXCEL97:
			poijiExcelType = PoijiExcelType.XLS;
			break;
		default:
			throw new InvalidFormatException(String.format("Unable to determine ExcelType from Spreadsheet Version %s", sv));
		}
		LOGGER.debug("Workbook POI Excel Type [{}]", poijiExcelType);

		return poijiExcelType;
	}

	private int getSheetIndexByName(final Workbook workbook, final String name) {
		final int index = workbook.getSheetIndex(name);

		if (index < 0) {
			throw new IllegalStateException(String.format("Cannot locate worksheet with name %s", name));
		}
		LOGGER.debug("Worksheet [{}] Index [{}]", name, index);

		return index;
	}
}
