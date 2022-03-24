package Control;

import java.util.HashMap;
import java.util.Map;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.PLUCodedItem;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

/**
 * Simulate a store database that allows user to configure, add, update, remove
 * product information (weight, price, name).
 * 
 * @author Fu-Yin Lin
 * @author Ryan McHale
 * @author Karim Kassouri
 * @author Munhib Saad
 * @author Parker Wieck
 * @author Muhammad Ali
 * @author Ayomide Alabi
 *
 */
public class Store {
	private int barcodeItemCount;
	private int pluItemCount;
	private Map<Barcode, Map<String, String>> barcodeList = new HashMap<>();
	private Map<PriceLookupCode, Map<String, String>> PLUList = new HashMap<>();

	/**
	 * Creates a product database for the self-checkout station.
	 * 
	 * @param barcodeItemCount The number of the barcoded items to be configured.
	 *                         Can be changed with item addition or removal. Must be
	 *                         positive. Zero count allowed.
	 * @param pluItemCount     The number of the PLU coded items to be configured.
	 *                         Can be changed with item addition or removal. Must be
	 *                         positive. Zero count allowed.
	 */
	public Store(int barcodeItemCount, int pluItemCount) {
		if (barcodeItemCount < 0 || pluItemCount < 0)
			throw new SimulationException("Counts must be non-negative");
		this.barcodeItemCount = barcodeItemCount;
		this.pluItemCount = pluItemCount;
	}

	/**
	 * Initial configuration of the store database to record barcoded products.
	 * 
	 * @param barcodedItems    A list of barcoded items to be recorded. No item
	 *                         barcode can be null or weight is not positive.
	 * @param barcodedProducts A list of barcoded products to be recorded. No
	 *                         product barcode can be null or price is not positive.
	 */
	public void barcodeConfigure(BarcodedItem[] barcodedItems, BarcodedProduct[] barcodedProducts) {
		if (barcodedItems.length != barcodeItemCount || barcodedProducts.length != barcodeItemCount) {
			throw new SimulationException(
					"The number of barcoded items and products must be identical to the barcode item count in the store");
		}

		for (BarcodedItem item : barcodedItems) {
			Map<String, String> itemDetails = new HashMap<>();
			itemDetails.put("weight", String.valueOf(item.getWeight()));
			if (barcodeList.containsKey(item.getBarcode())) {
				// Should not happen in initial configuration (barcode should be unique for each
				// input item)
				throw new SimulationException("duplicate barcoded item");
			} else {
				barcodeList.put(item.getBarcode(), itemDetails);
			}
		}

		for (BarcodedProduct product : barcodedProducts) {
			if (barcodeList.containsKey(product.getBarcode())) {
				Map<String, String> productDetails = barcodeList.get(product.getBarcode());
				productDetails.put("price", String.valueOf(product.getPrice()));
				productDetails.put("name", product.getDescription());
				barcodeList.replace(product.getBarcode(), productDetails);
			} else {
				throw new SimulationException("this barcode product does not match any of the barcode items on list");
			}
		}
	}

	/**
	 * Initial configuration of the store database to record PLU coded products.
	 * 
	 * @param PLUItems A list of PLU coded items to be recorded. No item PLU code
	 *                 can be null or weight is not positive.
	 * @param PLUItems A list of PLU coded products to be recorded. No product PLU
	 *                 code can be null or price is not positive.
	 */
	public void pluConfigure(PLUCodedItem[] PLUItems, PLUCodedProduct[] PLUProducts) {
		if (PLUItems.length != pluItemCount || PLUProducts.length != pluItemCount) {
			throw new SimulationException(
					"The number of PLU coded items and products must be identical to the PLU coded item count in the store");
		}

		for (PLUCodedItem item : PLUItems) {
			Map<String, String> itemDetails = new HashMap<>();
			itemDetails.put("weight", String.valueOf(item.getWeight()));
			if (PLUList.containsKey(item.getPLUCode())) {
				// Should not happen in initial configuration (PLU code should be unique for
				// each input item)
				throw new SimulationException("duplicate PLU coded item");
			} else {
				PLUList.put(item.getPLUCode(), itemDetails);
			}
		}

		for (PLUCodedProduct product : PLUProducts) {
			if (PLUList.containsKey(product.getPLUCode())) {
				Map<String, String> productDetails = PLUList.get(product.getPLUCode());
				productDetails.put("price", String.valueOf(product.getPrice()));
				productDetails.put("name", product.getDescription());
				PLUList.replace(product.getPLUCode(), productDetails);
			} else {
				throw new SimulationException(
						"this PLU code product does not match any of the PLU coded items on list");
			}
		}
	}

	/**
	 * Add product to the store database. Input item and product must be of the same
	 * type (barcode / PLU code).
	 * 
	 * @param item    Item to be added to the database.
	 * @param product Product to be added to the database.
	 */
	public void addProduct(Item item, Product product) {
		if (item == null || product == null) {
			throw new SimulationException(new NullPointerException());
		}
		if (item instanceof BarcodedItem && product instanceof BarcodedProduct) {
			Barcode barcode = ((BarcodedItem) item).getBarcode();
			Map<String, String> details = new HashMap<>();
			details.put("weight", String.valueOf(item.getWeight()));
			details.put("price", String.valueOf(product.getPrice()));
			details.put("name", ((BarcodedProduct) product).getDescription());
			if (barcodeList.containsKey(barcode)) {
				barcodeList.replace(barcode, details);
			} else {
				barcodeList.put(barcode, details);
				barcodeItemCount++;
			}
		} else if (item instanceof PLUCodedItem && product instanceof PLUCodedProduct) {
			PriceLookupCode PLUcode = ((PLUCodedItem) item).getPLUCode();
			Map<String, String> details = new HashMap<>();
			details.put("weight", String.valueOf(item.getWeight()));
			details.put("price", String.valueOf(product.getPrice()));
			details.put("name", ((PLUCodedProduct) product).getDescription());
			if (PLUList.containsKey(PLUcode)) {
				PLUList.replace(PLUcode, details);
			} else {
				PLUList.put(PLUcode, details);
				pluItemCount++;
			}
		} else {
			throw new SimulationException("invalid item/product input");
		}
	}

	/**
	 * Update product information in the database. Input item and product must be of
	 * the same type (barcode / PLU code).
	 * 
	 * @param item    Item to be updated in the database.
	 * @param product Product to be updated in the database.
	 */
	public void updateProduct(Item item, Product product) {
		if (item == null && product == null) {
			throw new SimulationException("invalid item/product input");
		}

		if (item != null && product == null) {
			if (item instanceof BarcodedItem) {
				Barcode barcode = ((BarcodedItem) item).getBarcode();
				Map<String, String> itemDetails = barcodeList.get(barcode);
				itemDetails.replace("weight", String.valueOf(item.getWeight()));
			} else if (item instanceof PLUCodedItem) {
				PriceLookupCode PLUcode = ((PLUCodedItem) item).getPLUCode();
				Map<String, String> itemDetails = PLUList.get(PLUcode);
				itemDetails.replace("weight", String.valueOf(item.getWeight()));
			} else {
				throw new SimulationException("unknown item");
			}
		} else if (item == null && product != null) {
			if (product instanceof BarcodedProduct) {
				Barcode barcode = ((BarcodedProduct) product).getBarcode();
				Map<String, String> productDetails = barcodeList.get(barcode);
				productDetails.replace("price", String.valueOf(product.getPrice()));
				productDetails.replace("name", ((BarcodedProduct) product).getDescription());
			} else if (product instanceof PLUCodedProduct) {
				PriceLookupCode PLUcode = ((PLUCodedProduct) product).getPLUCode();
				Map<String, String> productDetails = PLUList.get(PLUcode);
				productDetails.replace("price", String.valueOf(product.getPrice()));
				productDetails.replace("name", ((PLUCodedProduct) product).getDescription());
			} else {
				throw new SimulationException("unknown product");
			}
		} else if (item != null && product != null) {
			if (item instanceof BarcodedItem) {
				Barcode barcode = ((BarcodedItem) item).getBarcode();
				Map<String, String> details = barcodeList.get(barcode);
				details.replace("weight", String.valueOf(item.getWeight()));
				details.replace("price", String.valueOf(product.getPrice()));
				details.replace("name", ((BarcodedProduct) product).getDescription());
			} else if (item instanceof PLUCodedItem) {
				PriceLookupCode PLUcode = ((PLUCodedItem) item).getPLUCode();
				Map<String, String> details = PLUList.get(PLUcode);
				details.replace("weight", String.valueOf(item.getWeight()));
				details.replace("price", String.valueOf(product.getPrice()));
				details.replace("name", ((PLUCodedProduct) product).getDescription());
			} else {
				throw new SimulationException("unknown item");
			}
		}
	}

	/**
	 * Remove product from the database. Input item and product must be of the same
	 * type (barcode / PLU code).
	 * 
	 * @param item    Item to be removed from the database.
	 * @param product Product to be removed from the database.
	 */
	public void removeProduct(Item item, Product product) {
		if (item == null && product == null) {
			throw new SimulationException("invalid item/product input");
		}

		if (item instanceof BarcodedItem || product instanceof BarcodedProduct) {
			Barcode barcode;
			if (item != null) {
				barcode = ((BarcodedItem) item).getBarcode();
			} else {
				barcode = ((BarcodedProduct) product).getBarcode();
			}
			barcodeList.remove(barcode);
			barcodeItemCount--;
		} else if (item instanceof PLUCodedItem || product instanceof PLUCodedProduct) {
			PriceLookupCode PLUcode;
			if (item != null) {
				PLUcode = ((PLUCodedItem) item).getPLUCode();
			} else {
				PLUcode = ((PLUCodedProduct) product).getPLUCode();
			}
			PLUList.remove(PLUcode);
			pluItemCount--;
		} else {
			throw new SimulationException("unknown item");
		}
	}

	/**
	 * Accesses the number of barcoded items.
	 * 
	 * @return The number of barcoded items in the database.
	 */
	public int getBarcodeItemCount() {
		return barcodeItemCount;
	}

	/**
	 * Accesses the number of PLU coded items.
	 * 
	 * @return The number of PLU coded items in the database.
	 */
	public int getPluItemCount() {
		return pluItemCount;
	}

	/**
	 * Accesses the current barcode item list.
	 * 
	 * @return The current barcode item list in the store.
	 */
	public Map<Barcode, Map<String, String>> getBarcodeList() {
		return barcodeList;
	}

	/**
	 * Accesses the current PLU code item list.
	 * 
	 * @return The current PLU code item list in the store.
	 */
	public Map<PriceLookupCode, Map<String, String>> getPLUList() {
		return PLUList;
	}
}
