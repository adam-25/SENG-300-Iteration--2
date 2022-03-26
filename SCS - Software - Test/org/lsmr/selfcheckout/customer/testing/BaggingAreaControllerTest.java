package org.lsmr.selfcheckout.customer.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.customer.BaggingAreaController;
import org.lsmr.selfcheckout.customer.PaymentController;
import org.lsmr.selfcheckout.customer.ScanItemController;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;

public class BaggingAreaControllerTest extends BaseTestClass {
	private BaggingAreaController BACController;
	private ScanItemController SICController;
	private HashMap<Barcode, BigDecimal> barcodePrice = new HashMap<Barcode, BigDecimal>();
	private final HashMap<Barcode, Double> barcodeWeight = new HashMap<Barcode, Double>();
	private SelfCheckoutStation cs;
	
	//initializing prices of items
	BigDecimal item1Price = new BigDecimal(2.50);
	BigDecimal item2Price = new BigDecimal(4.05);
	BigDecimal item3Price = new BigDecimal(3.50);
			
	Numeral[] nItem1 = {Numeral.one, Numeral.two, Numeral.three, Numeral.four};
	Numeral[] nItem2 = {Numeral.two, Numeral.three, Numeral.four, Numeral.one};
	Numeral[] nItem3 = {Numeral.three, Numeral.two, Numeral.four, Numeral.one};
			
			
	//initalizing barcodes of the items
	Barcode barcodeItem1 = new Barcode(nItem1);
	Barcode barcodeItem2 = new Barcode(nItem2);
	Barcode barcodeItem3 = new Barcode(nItem3);
	
	//Method to ensure that the item scanned is actually added
	public void handheldScanError(BarcodedItem item) {
		while (true) {
			cs.handheldScanner.scan(item);
			
			if(SICController.numOfScannedItems() == (1+BACController.getNumOfItemsInBaggingArea())) {
				cs.baggingArea.add(item);
				break;
			}
		}
	}
	
	public void mainScanError(BarcodedItem item) {
		while (true) {
			cs.mainScanner.scan(item);
			
			if(SICController.numOfScannedItems() == (1+BACController.getNumOfItemsInBaggingArea())) {
				cs.baggingArea.add(item);
				break;
			}
		}
	}
	
	//Initialize selfcheckout station 
	//Bagging area controller
	//Scan item controller
	@Before
	public void setup()  {
		
		//Use checkout station from base test class
		super.setup();
		cs = checkoutStation;
		
		
		
		//initalizing price hashmap		
		barcodePrice.put(barcodeItem1, item1Price);	//Item1
		barcodePrice.put(barcodeItem2, item2Price);	//Item2
		barcodePrice.put(barcodeItem3, item3Price);	//item3
				
		//initalizing expected weight hashmap
		barcodeWeight.put(barcodeItem1, 300.0);
		barcodeWeight.put(barcodeItem2, 100.0);
		barcodeWeight.put(barcodeItem3, 1000.0);
		
		SICController = new ScanItemController(checkoutStation, barcodePrice, barcodeWeight);
		BACController = new BaggingAreaController(checkoutStation);
		
		//Setting the Controllers of each Controller
		BACController.setScanItemControl(SICController);
		SICController.setBagAreaControl(BACController);

	}
	
	//Test if scanner is disabled if scale is overload
	//Enter overload by adding heavy item to scale
	@Test
	public void testScannerDisabledOverLoad1() {
		BarcodedItem item1 = new BarcodedItem(barcodeItem1, 300.0);
		BarcodedItem item2 = new BarcodedItem(barcodeItem2, 100.0);
		BarcodedItem item3 = new BarcodedItem(barcodeItem3, 1000.0);
		BarcodedItem item3Dup1 = new BarcodedItem(barcodeItem3, 1000.0);

		Assert.assertFalse(checkoutStation.mainScanner.isDisabled());
		Assert.assertFalse(checkoutStation.handheldScanner.isDisabled());
		
		
		try {
			mainScanError(item3);

			mainScanError(item1);

			mainScanError(item2);
	
			mainScanError(item3Dup1);


			
		} catch(Exception e) {
			//shouldn't each here
			System.out.println("not supposed to occur");
			e.printStackTrace();
			fail();
		}
		
		Assert.assertTrue(checkoutStation.mainScanner.isDisabled());
		Assert.assertTrue(checkoutStation.handheldScanner.isDisabled());
		
	
	}
	
	//Test if the scale goes out of overload, the scanner is enabled
		@Test
		public void testScannerDisabledOverLoad2() {
			BarcodedItem item1 = new BarcodedItem(barcodeItem1, 300.0);
			BarcodedItem item2 = new BarcodedItem(barcodeItem2, 100.0);
			BarcodedItem item3 = new BarcodedItem(barcodeItem3, 1000.0);
			BarcodedItem item3Dup1 = new BarcodedItem(barcodeItem3, 1000.0);

			Assert.assertFalse(checkoutStation.mainScanner.isDisabled());
			Assert.assertFalse(checkoutStation.handheldScanner.isDisabled());
			
			try {
				mainScanError(item3);
				mainScanError(item1);
				mainScanError(item2);
				mainScanError(item3Dup1);
			} catch(Exception e) {
				//shouldn't each here
				System.out.println("not supposed to occur");
				e.printStackTrace();
				fail();
			}
			
			
			Assert.assertTrue(checkoutStation.mainScanner.isDisabled());
			Assert.assertTrue(checkoutStation.handheldScanner.isDisabled());
			
			try {
				checkoutStation.baggingArea.remove(item3);
			} catch(Exception e) {
				//shouldn't each here
				System.out.println("not supposed to occur");
				e.printStackTrace();
				fail();
			}
			Assert.assertFalse(checkoutStation.mainScanner.isDisabled());
			Assert.assertFalse(checkoutStation.handheldScanner.isDisabled());
			Assert.assertEquals(3, BACController.getNumOfItemsInBaggingArea());
		}
	
	
	//Test if scanner is disabled if there is a weight discrepancy
	//test one item with a discrepancy in the weight that is larger

	//double check to see if test logic is sound
	@Test
	public void testWeightChanged1() {
		BarcodedItem item1 = new BarcodedItem(barcodeItem1, 400.0);
		
		try {
			mainScanError(item1);
		} catch (Exception e) {
			System.out.println("Shouldn't happen");
			fail();
		}
		
		Assert.assertTrue(checkoutStation.mainScanner.isDisabled());
		Assert.assertTrue(checkoutStation.handheldScanner.isDisabled());
	}
	
	//Test if scanner is disabled if there is a weight discrepancy
	//test 1 item with a discrepancy in the weight that is less
	
	//double check to see if test logic is sound
	@Test
	public void testWeightChanged2() {
		BarcodedItem item1 = new BarcodedItem(barcodeItem1, 200.0);

		try {
			mainScanError(item1);
		} catch (Exception e) {
			System.out.println("Shouldn't happen");
			fail();
		}
		
		Assert.assertTrue(checkoutStation.mainScanner.isDisabled());
		Assert.assertTrue(checkoutStation.handheldScanner.isDisabled());
	}
	
	//Test if scanner is disabled if there is a weight discrepancy
		//test 2 items with one having discrepancy in the weight that is correct weight and one that is greater than the expected
		@Test
		public void testWeightChanged3() {
			BarcodedItem item1 = new BarcodedItem(barcodeItem1, 300.0);
			BarcodedItem item1Dup1 = new BarcodedItem(barcodeItem1, 400.0);

			try {
				mainScanError(item1);
				mainScanError(item1Dup1);
			} catch (Exception e) {
				System.out.println("Shouldn't happen");
				fail();
			}
			
			Assert.assertTrue(checkoutStation.mainScanner.isDisabled());
			Assert.assertTrue(checkoutStation.handheldScanner.isDisabled());
		}
	
		//Test to see that when an item is removed the controller knows and adjusts the number in the cart
		@Test
		public void testWeightChanged4() {
			BarcodedItem item1 = new BarcodedItem(barcodeItem1, 300.0);
			BarcodedItem item2 = new BarcodedItem(barcodeItem2, 100.0);
			BarcodedItem item3 = new BarcodedItem(barcodeItem3, 1000.0);
			BarcodedItem item3Dup1 = new BarcodedItem(barcodeItem3, 1000.0);

			Assert.assertFalse(checkoutStation.mainScanner.isDisabled());
			
			try {
				mainScanError(item3);
				mainScanError(item1);
				mainScanError(item2);
				mainScanError(item3Dup1);
				
			} catch(Exception e) {
				//shouldn't each here
				System.out.println("not supposed to occur");
				e.printStackTrace();
				fail();
			}
			Assert.assertEquals(BACController.getNumOfItemsInBaggingArea(), 3);
			
			try {
				checkoutStation.baggingArea.remove(item3Dup1);
			} catch (Exception e) {
				fail();
			}
			
			Assert.assertFalse(checkoutStation.mainScanner.isDisabled());
			Assert.assertFalse(checkoutStation.handheldScanner.isDisabled());
			Assert.assertEquals(BACController.getNumOfItemsInBaggingArea(), 3);
		}
		
		//Test to see that when all items are removed the controller knows and adjusts the number in the cart
				@Test
				public void testWeightChanged5() {
					BarcodedItem item1 = new BarcodedItem(barcodeItem1, 300.0);
					BarcodedItem item2 = new BarcodedItem(barcodeItem2, 100.0);
					BarcodedItem item3 = new BarcodedItem(barcodeItem3, 1000.0);


					Assert.assertFalse(checkoutStation.mainScanner.isDisabled());
					Assert.assertFalse(checkoutStation.handheldScanner.isDisabled());
					
					try {
						mainScanError(item3);
						mainScanError(item1);
						mainScanError(item2);

						
					} catch(Exception e) {
						//shouldn't each here
						System.out.println("not supposed to occur");
						e.printStackTrace();
						fail();
					}
					
					Assert.assertEquals(3, BACController.getNumOfItemsInBaggingArea());
					
					try {
						checkoutStation.baggingArea.remove(item2);
						checkoutStation.baggingArea.remove(item3);
						checkoutStation.baggingArea.remove(item1);
					} catch (Exception e) {
						fail();
					}
					

					Assert.assertEquals(BACController.getNumOfItemsInBaggingArea(), 0);
				}
	
}
