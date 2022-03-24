
/**
 * Test Suite for Store class.
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

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;

import Control.Store;

public class StoreTests {

	@Before
	public void setUp() throws Exception {

	}

	@Test(expected = SimulationException.class)
	public void item_count_must_be_non_negative() {
		int barcodeItemCount = -1;
		int pluItemCount = 5;
		new Store(barcodeItemCount, pluItemCount);
	}

	@Test(expected = SimulationException.class)
	public void plu_item_count_must_be_non_negative() {
		int barcodeItemCount = 1;
		int pluItemCount = -1;
		new Store(barcodeItemCount, pluItemCount);
	}

	@Test
	public void store_construction() {
		new Store(4, 4);
	}

	@Test(expected = SimulationException.class)
	public void improper_length_of_barcodedItems() {
		BarcodedItem[] barcodedItems = new BarcodedItem[0];
		BarcodedProduct[] barcodedProducts = new BarcodedProduct[1];
		Store s = new Store(1, 1);
		s.barcodeConfigure(barcodedItems, barcodedProducts);
	}

	@Test(expected = SimulationException.class)
	public void improper_length_of_barcodedProducts() {
		BarcodedItem[] barcodedItems = new BarcodedItem[1];
		BarcodedProduct[] barcodedProducts = new BarcodedProduct[0];
		Store s = new Store(1, 1);
		s.barcodeConfigure(barcodedItems, barcodedProducts);
	}

	@Test
	public void basic_passing_config_call() {
		BarcodedItem[] barcodedItems = new BarcodedItem[1];

		Numeral[] numerals = new Numeral[1];
		numerals[0] = Numeral.valueOf((byte) 1);
		barcodedItems[0] = new BarcodedItem(new Barcode(numerals), 10.0);

		BarcodedProduct[] barcodedProducts = new BarcodedProduct[1];
		barcodedProducts[0] = new BarcodedProduct(new Barcode(numerals), "foo", BigDecimal.ONE);

		Store s = new Store(1, 1);
		s.barcodeConfigure(barcodedItems, barcodedProducts);
	}

	@Test
	public void duplicated_barcode() {
		BarcodedItem[] barcodedItems = new BarcodedItem[2];

		Numeral[] numerals = new Numeral[1];
		numerals[0] = Numeral.valueOf((byte) 1);
		barcodedItems[0] = new BarcodedItem(new Barcode(numerals), 10.0);
		barcodedItems[1] = new BarcodedItem(new Barcode(numerals), 6.0);

		BarcodedProduct[] barcodedProducts = new BarcodedProduct[2];
		barcodedProducts[0] = new BarcodedProduct(new Barcode(numerals), "foo", BigDecimal.ONE);
		barcodedProducts[1] = new BarcodedProduct(new Barcode(numerals), "foo", BigDecimal.TEN);

		Store s = new Store(2, 2);
		s.barcodeConfigure(barcodedItems, barcodedProducts);
	}

	@Test(expected = SimulationException.class)
	public void product_with_barcode_not_in_items() {
		Numeral[] item_numerals = new Numeral[1];
		item_numerals[0] = Numeral.valueOf((byte) 1);

		Numeral[] product_numerals = new Numeral[2];
		product_numerals[0] = Numeral.valueOf((byte) 1);
		product_numerals[1] = Numeral.valueOf((byte) 1);

		BarcodedItem[] barcodedItems = new BarcodedItem[1];
		barcodedItems[0] = new BarcodedItem(new Barcode(item_numerals), 10.0);

		BarcodedProduct[] barcodedProducts = new BarcodedProduct[1];
		barcodedProducts[0] = new BarcodedProduct(new Barcode(product_numerals), "foo", BigDecimal.ONE);

		Store s = new Store(1, 1);
		s.barcodeConfigure(barcodedItems, barcodedProducts);
	}

	@Test
	public void get_barcode_list() {
		BarcodedItem[] barcodedItems = new BarcodedItem[2];

		Numeral[] foo_numerals = new Numeral[1];
		foo_numerals[0] = Numeral.valueOf((byte) 1);

		Numeral[] bar_numerals = new Numeral[1];
		bar_numerals[0] = Numeral.valueOf((byte) 2);

		barcodedItems[0] = new BarcodedItem(new Barcode(foo_numerals), 50.0);
		barcodedItems[1] = new BarcodedItem(new Barcode(bar_numerals), 2.0);

		BarcodedProduct[] barcodedProducts = new BarcodedProduct[2];
		barcodedProducts[0] = new BarcodedProduct(new Barcode(foo_numerals), "foo", BigDecimal.ONE);
		barcodedProducts[1] = new BarcodedProduct(new Barcode(bar_numerals), "bar", BigDecimal.TEN);

		Store s = new Store(2, 2);
		s.barcodeConfigure(barcodedItems, barcodedProducts);

		assertEquals(s.getBarcodeItemCount(), 2);
		Map<Barcode, Map<String, String>> test_result = s.getBarcodeList();

		if (test_result == null) {
			fail("barcode list is null");
		}

		double returned_price = Double.parseDouble((test_result.get(new Barcode(foo_numerals)).get("price")));
		double original_price = barcodedProducts[0].getPrice().doubleValue();
		assertEquals(returned_price, original_price, 0.01);
		assertEquals(Double.parseDouble(test_result.get(new Barcode(foo_numerals)).get("weight")),
				barcodedItems[0].getWeight(), 0.01);

		returned_price = Double.parseDouble((test_result.get(new Barcode(bar_numerals)).get("price")));
		original_price = barcodedProducts[1].getPrice().doubleValue();
		assertEquals(returned_price, original_price, 0.01);
		assertEquals(Double.parseDouble(test_result.get(new Barcode(bar_numerals)).get("weight")),
				barcodedItems[1].getWeight(), 0.01);
	}
}
