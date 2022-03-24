import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.Numeral;

import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;

import Control.SelfCheckoutStationLogic;
import Control.Store;

/**
 * Test Suite for Self-Checkout Station Logic.
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
public class SelfCheckoutStationLogicTests {

	private Currency validCurrency;
	private Currency invalidCurrency;
	private int banknoteDenominations[] = { 5, 10, 20, 50, 100 };
	private BigDecimal coinDenominations[] = { BigDecimal.valueOf(0.05), BigDecimal.valueOf(0.10),
			BigDecimal.valueOf(0.25), BigDecimal.valueOf(1.00), BigDecimal.valueOf(2.00) };
	private SelfCheckoutStation selfCheckoutStation;
	private SelfCheckoutStationLogic selfCheckoutStationLogic;
	private Store store;

	private ElectronicScale testES;
	private Item heavyItem;
	private Item normalItem;
	private Item lightItem;

	BarcodedItem barcodedLightItem;
	BarcodedItem barcodedNormalItem;
	BarcodedItem barcodedHeavyItem;

	private class ItemStub extends Item {

		protected ItemStub(double weightInGrams) {
			super(weightInGrams);
		}

	}

	@Before
	public void setUp() {
		validCurrency = Currency.getInstance("CAD");
		invalidCurrency = Currency.getInstance("USD");
		selfCheckoutStation = new SelfCheckoutStation(validCurrency, banknoteDenominations, coinDenominations, 100, 1);
		store = new Store(3, 3);

		Numeral numerals1[] = { Numeral.valueOf((byte) 1) };
		Numeral numerals2[] = { Numeral.valueOf((byte) 2) };
		Numeral numerals3[] = { Numeral.valueOf((byte) 3) };

		Barcode barcode1 = new Barcode(numerals1);
		Barcode barcode2 = new Barcode(numerals2);
		Barcode barcode3 = new Barcode(numerals3);

		barcodedLightItem = new BarcodedItem(barcode1, 1);
		barcodedNormalItem = new BarcodedItem(barcode2, 1);
		barcodedHeavyItem = new BarcodedItem(barcode3, 1);

		BarcodedItem barcodedItems[] = { barcodedLightItem, barcodedNormalItem, barcodedHeavyItem };

		BarcodedProduct barcodedProducts[] = { new BarcodedProduct(barcode1, "foo", BigDecimal.ONE),
				new BarcodedProduct(barcode1, "bar", BigDecimal.ONE),
				new BarcodedProduct(barcode1, "foobar", BigDecimal.ONE) };

		store.barcodeConfigure(barcodedItems, barcodedProducts);

		selfCheckoutStationLogic = new SelfCheckoutStationLogic(selfCheckoutStation, store);

		testES = new ElectronicScale(5, 2);
		heavyItem = new ItemStub(10);
		normalItem = new ItemStub(2);
		lightItem = new ItemStub(1);
	}

	///////////////////////////////////////////////////////
	// GENERAL TESTS
	///////////////////////////////////////////////////////

	@Test(expected = SimulationException.class)
	public void constructor_error_test() {
		SelfCheckoutStationLogic scsl = new SelfCheckoutStationLogic(null, null);
	}

	@Test(expected = SimulationException.class)
	public void constructor_error_test_2() {
		SelfCheckoutStationLogic scsl = new SelfCheckoutStationLogic(selfCheckoutStation, null);
	}

	@Test
	public void enabled_disabled_test() throws DisabledException {
		selfCheckoutStation.banknoteValidator.enable();
		selfCheckoutStation.banknoteValidator.disable();
		// success
	}

	///////////////////////////////////////////////////////
	// PAYMENT TESTS
	///////////////////////////////////////////////////////

	@Test
	public void validBanknoteDetected_test() throws DisabledException, OverloadException {
		Banknote banknote = new Banknote(validCurrency, 5);
		selfCheckoutStation.banknoteInput.accept(banknote);
		assertTrue(selfCheckoutStationLogic.getTotalPayment() == 5);
	}

	@Test
	public void invalidBanknoteDetected_test() throws DisabledException, OverloadException {
		Banknote banknote = new Banknote(invalidCurrency, 5);
		selfCheckoutStation.banknoteInput.accept(banknote);
		assertTrue(selfCheckoutStationLogic.getTotalPayment() == 0);

	}

	@Test
	public void validCoinDetected_test() throws DisabledException, OverloadException {
		Coin coin = new Coin(validCurrency, BigDecimal.valueOf(0.05));
		selfCheckoutStation.coinSlot.accept(coin);
		assertTrue(selfCheckoutStationLogic.getTotalPayment() == 0.05);

	}

	@Test
	public void invalidCoinDetected_test() throws DisabledException, OverloadException {
		Coin coin = new Coin(invalidCurrency, BigDecimal.valueOf(0.05));
		selfCheckoutStation.coinSlot.accept(coin);
		assertTrue(selfCheckoutStationLogic.getTotalPayment() == 0);

	}

	@Test
	public void printRecipt_test() throws DisabledException, OverloadException {
		selfCheckoutStationLogic.printReceipt();
	}

	////////////////
	// BAGGING TESTS
	///////////////

	@Test(expected = OverloadException.class)
	public void overload_test() throws OverloadException {
		heavyItem = new ItemStub(999);
		selfCheckoutStation.scale.add(heavyItem);

		selfCheckoutStation.scale.getCurrentWeight();

	}

	@Test
	public void outOfOverload_test() throws OverloadException {
		heavyItem = new ItemStub(999);
		selfCheckoutStation.scale.add(heavyItem);

		selfCheckoutStation.scale.remove(heavyItem);

		assertTrue(selfCheckoutStation.scale.getCurrentWeight() == 0);
	}

	@Test
	public void normalItemAdded_noItemExpected() throws OverloadException {
		selfCheckoutStation.scale.add(normalItem);

		assertTrue(selfCheckoutStation.scale.getCurrentWeight() == normalItem.getWeight());
	}

	@Test
	public void scanPlaceMismatchWithinSensitivity() throws OverloadException {

		selfCheckoutStation.scanner.scan(barcodedLightItem);

		selfCheckoutStation.scale.add(normalItem);

		assertFalse(selfCheckoutStationLogic.getWaitForAttendant());
	}

	@Test
	public void printRecipt() {
		selfCheckoutStation.scanner.scan(barcodedLightItem);

		selfCheckoutStation.scale.add(lightItem);

		selfCheckoutStation.printer.addPaper(100);
		selfCheckoutStation.printer.addInk(100);

		selfCheckoutStationLogic.printReceipt();

		// success
	}

	@Test
	public void removingItem() throws OverloadException {

		selfCheckoutStation.scale.add(normalItem);

		selfCheckoutStation.scanner.scan(barcodedLightItem);

		selfCheckoutStation.scale.remove(normalItem);

		assertTrue(selfCheckoutStationLogic.getWaitForAttendant());

	}

	@Test(expected = SimulationException.class)
	public void barcodeScannedBarcodeNull() {
		Currency Banknote = Currency.getInstance(Locale.US);
		int[] validBanknoteDenomination = { 5 };
		BigDecimal[] ValidCoinDenomination = { BigDecimal.valueOf(0.05) };
		SelfCheckoutStation SCSTest = new SelfCheckoutStation(Banknote, validBanknoteDenomination,
				ValidCoinDenomination, 5, 2);
		Store s = new Store(1, 1);
		SelfCheckoutStationLogic TestSM = new SelfCheckoutStationLogic(SCSTest, s);

		Barcode testBarcode = null;
		BarcodeScanner testBS = null;

		TestSM.barcodeScanned(testBS, testBarcode);

	}

	@Test
	public void barcodeScannedContainsBarcode() {

		BarcodedItem[] barcodedItems = new BarcodedItem[1];

		Numeral[] numerals = new Numeral[1];
		numerals[0] = Numeral.valueOf((byte) 1);
		Barcode testBarcode = new Barcode(numerals);
		barcodedItems[0] = new BarcodedItem(testBarcode, 10.0);

		BarcodedProduct[] barcodedProducts = new BarcodedProduct[1];
		barcodedProducts[0] = new BarcodedProduct(testBarcode, "foo", BigDecimal.ONE);

		Currency Banknote = Currency.getInstance(Locale.US);
		int[] validBanknoteDenomination = { 5 };
		BigDecimal[] ValidCoinDenomination = { BigDecimal.valueOf(0.05) };
		SelfCheckoutStation SCSTest = new SelfCheckoutStation(Banknote, validBanknoteDenomination,
				ValidCoinDenomination, 5, 2);

		Store s = new Store(1, 1);

		s.barcodeConfigure(barcodedItems, barcodedProducts);

		SelfCheckoutStationLogic TestSM = new SelfCheckoutStationLogic(SCSTest, s);

		BarcodeScanner testBS = null;

		double originalTotalCost = Double.parseDouble((TestSM).getBarcodeList().get(testBarcode).get("price"));

		double originalExpectedWeight = Double.parseDouble(TestSM.getBarcodeList().get(testBarcode).get("weight"));

		// Causes scannedItemList to put testBarcode in
		TestSM.barcodeScanned(testBS, testBarcode);

		double afterTotalCost = Double.parseDouble(TestSM.getBarcodeList().get(testBarcode).get("price"));

		double afterExpectedWeight = Double.parseDouble(TestSM.getBarcodeList().get(testBarcode).get("weight"));

		System.out.println(originalExpectedWeight + " " + afterExpectedWeight);
		System.out.println(originalTotalCost + " " + afterTotalCost);
		// scannedItemList already has testBarcode
		// forcing scannedItemList to contain testBarcode
		TestSM.barcodeScanned(testBS, testBarcode);

		assertEquals(TestSM.getTotalCost(), afterTotalCost + originalTotalCost, 0.01);
		assertEquals(TestSM.getTotalExpectedWeight(), afterExpectedWeight, 0.01);

	}
}
