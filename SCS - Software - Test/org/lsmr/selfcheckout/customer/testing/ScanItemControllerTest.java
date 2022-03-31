package org.lsmr.selfcheckout.customer.testing;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashMap;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.customer.BaggingAreaController;
import org.lsmr.selfcheckout.customer.PaymentController;
import org.lsmr.selfcheckout.customer.ScanItemController;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.Numeral;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ScanItemControllerTest extends BaseTestClass{
	
	//declaring self checkout station
	private SelfCheckoutStation cs;
	
	//declaring controllers 
	private ScanItemController SIcontroller;
	private BaggingAreaController bAcontroller;
	private PaymentController pController;
	
	//initializing prices of items
	BigDecimal milkPrice = new BigDecimal(2.50);
	BigDecimal eggPrice = new BigDecimal(4.05);
	BigDecimal toastPrice = new BigDecimal(3.50);
	
	Numeral[] nMilk = {Numeral.one, Numeral.two, Numeral.three, Numeral.four};
	Numeral[] nEggs = {Numeral.two, Numeral.three, Numeral.four, Numeral.one};
	Numeral[] nToast = {Numeral.three, Numeral.two, Numeral.four, Numeral.one};
	
	
	//initalizing barcodes of the items
	Barcode barcodeMilk = new Barcode(nMilk);
	Barcode barcodeEggs = new Barcode(nEggs);
	Barcode barcodeToast = new Barcode(nToast);
	
	
	//declaring hashmap barcodePrice hashmap and barcodeWeight hashmap
	private HashMap<Barcode, BigDecimal> barcodePrice = new HashMap<Barcode, BigDecimal>();
	private final HashMap<Barcode, Double> barcodeWeight = new HashMap<Barcode, Double>();
	
	//Initialize checkoutStation
	//Create a hasMap of bar code to price
	@Before
	public void setup() {
		
		
		super.setup();
		cs = checkoutStation;
		
		
		//initalizing price hashmap		
		barcodePrice.put(barcodeMilk, milkPrice);	//milk
		barcodePrice.put(barcodeEggs, eggPrice);	//eggs
		barcodePrice.put(barcodeToast, toastPrice);	//toast
		
		//initalizing weight hashmap
		barcodeWeight.put(barcodeMilk, 3.0);	//milk
		barcodeWeight.put(barcodeEggs, 2.0);	//eggs
		barcodeWeight.put(barcodeToast, 5.0);	//toast
		
		
		//initalizing the BaggingAreaController and scanItemController
		bAcontroller = new BaggingAreaController(cs);
		SIcontroller = new ScanItemController(cs, barcodePrice, barcodeWeight);

		pController = new PaymentController(cs);
		
		//calling object methods
		bAcontroller.setScanItemControl(SIcontroller);
		SIcontroller.setBagAreaControl(bAcontroller);
		
	}
	
	public void scanError(BarcodedItem item) {
		while (true) {
			cs.handheldScanner.scan(item);
			
			if(SIcontroller.numOfScannedItems() == 1 + bAcontroller.getNumOfItemsInBaggingArea()) {
				cs.baggingArea.add(item);
				break;
			}
		}
	}
	
	
	//Expected value of cart = 10.05
	//Compare with the actual value of cart from the ScanItemController
	//Initialize a scanItemController
	@Test
	public void test1() {
		
	BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
	BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
	BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
	
	
	scanError(milk);
	scanError(eggs);
	scanError(toast);
	

	BigDecimal expectedValueOfCart = new BigDecimal(0);
	expectedValueOfCart = expectedValueOfCart.add(milkPrice);
	expectedValueOfCart = expectedValueOfCart.add(eggPrice);
	expectedValueOfCart = expectedValueOfCart.add(toastPrice);

	Assert.assertEquals(expectedValueOfCart, SIcontroller.getValueOfCart());
	}
	
	
	//Disable the scanner
	//Expected result - disable exception
	//Try and scan an item while disabled
	@Test
	public void test2() {
		
		BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
		Double expectedWeightOfCart = 0.0;
		BigDecimal expectedValueOfCart = new BigDecimal(0);
		
		cs.handheldScanner.disable();
		cs.handheldScanner.scan(milk);
		
		
		Assert.assertEquals(new BigDecimal(0), SIcontroller.getValueOfCart());
		Assert.assertEquals(expectedWeightOfCart, SIcontroller.getWeightOfCart());
		
	}
		
		
	
	//scan a valid item
	@Test
	public void test3() {
		//initializing barcodedItems
		BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
		BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
		BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
	
		
		Double expectedWeightOfCart = 10.0;
		BigDecimal expectedValueOfCart = new BigDecimal(0);
		expectedValueOfCart = expectedValueOfCart.add(milkPrice);
		expectedValueOfCart = expectedValueOfCart.add(eggPrice);
		expectedValueOfCart = expectedValueOfCart.add(toastPrice);
		
		scanError(milk);
		scanError(eggs);
		scanError(toast);

		Assert.assertEquals(expectedWeightOfCart, SIcontroller.getWeightOfCart());
		Assert.assertEquals(expectedValueOfCart, SIcontroller.getValueOfCart());
		
	}
	
	//unscans a valid item
		@Test
		public void unScan1() {
			//initializing barcodedItems
			BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
			BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
			BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
		
			
			Double expectedWeightOfCart = 10.0;
			BigDecimal expectedValueOfCart = new BigDecimal(0);
			expectedValueOfCart = expectedValueOfCart.add(milkPrice);
			expectedValueOfCart = expectedValueOfCart.add(eggPrice);
			expectedValueOfCart = expectedValueOfCart.add(toastPrice);
			
			scanError(milk);
			scanError(eggs);
			scanError(toast);
			
			cs.baggingArea.remove(eggs);
			SIcontroller.unScanItem(eggs.getBarcode());
			
			
			expectedValueOfCart = expectedValueOfCart.subtract(eggPrice);
			expectedWeightOfCart -= eggs.getWeight();
			
			Assert.assertEquals(expectedWeightOfCart, SIcontroller.getWeightOfCart());
			Assert.assertEquals(expectedValueOfCart, SIcontroller.getValueOfCart());
			Assert.assertFalse(cs.mainScanner.isDisabled());
			Assert.assertFalse(cs.handheldScanner.isDisabled());
		}
		
		//unscans a item not in the cart
		@Test
		public void unScan2() {
			//initializing barcodedItems
			BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
			BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
			BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
					
			Double expectedWeightOfCart = 5.0;
			BigDecimal expectedValueOfCart = new BigDecimal(0);
			expectedValueOfCart = expectedValueOfCart.add(milkPrice);
			expectedValueOfCart = expectedValueOfCart.add(eggPrice);

			scanError(milk);
			scanError(eggs);

					
			SIcontroller.unScanItem(toast.getBarcode());
					
					

			Assert.assertEquals(expectedWeightOfCart, SIcontroller.getWeightOfCart());
			Assert.assertEquals(expectedValueOfCart, SIcontroller.getValueOfCart());
					
		}
				
		@Test
		public void scanItemAfterPartialPaymentTest() throws DisabledException, OverloadException
		{
			BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
			BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
			BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
			
			BigDecimal expectedValueOfCart = new BigDecimal(0);
			expectedValueOfCart = expectedValueOfCart.add(milkPrice);
			expectedValueOfCart = expectedValueOfCart.add(eggPrice);
			
			pController.initiateStart();
			
			scanError(milk);
			scanError(eggs);
			
			pController.setValueOfCart(expectedValueOfCart);
			
			pController.inititateCheckout();
			
			cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 5));
			
			BigDecimal expectedValueOfCartAfterPartialPayment;
			
			expectedValueOfCartAfterPartialPayment = expectedValueOfCart.subtract(new BigDecimal(5));

			Assert.assertEquals(expectedValueOfCartAfterPartialPayment, pController.getValueOfCart());
			Assert.assertTrue(cs.mainScanner.isDisabled());
			Assert.assertTrue(cs.handheldScanner.isDisabled());
			
			pController.addItemsWithPartialPayment();

			Assert.assertFalse(cs.mainScanner.isDisabled());
			Assert.assertFalse(cs.handheldScanner.isDisabled());
			
			expectedValueOfCart = expectedValueOfCartAfterPartialPayment;
			expectedValueOfCart = expectedValueOfCart.add(toastPrice);
			
			scanError(toast);
			
			pController.setValueOfCart(expectedValueOfCart);
			
			Assert.assertEquals(3, SIcontroller.numOfScannedItems());
			
			try {
				cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 10));
				fail("Disabled Exception to be thrown");
			}
			catch (DisabledException e)
			{
				Assert.assertTrue(e instanceof DisabledException);
			}
			catch (Exception e)
			{
				fail("Disabled Exception to be thrown instead of " + e);
			}
			
			Assert.assertEquals(expectedValueOfCart, pController.getValueOfCart());
			
		}
		
		@Test
		public void fullPaymentAfterPartialPaymentTest() throws DisabledException, OverloadException
		{
			BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
			BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
			BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
			
			BigDecimal expectedValueOfCart = new BigDecimal(0);
			expectedValueOfCart = expectedValueOfCart.add(milkPrice);
			expectedValueOfCart = expectedValueOfCart.add(eggPrice);
			
			pController.initiateStart();
			
			scanError(milk);
			scanError(eggs);
			
			pController.setValueOfCart(expectedValueOfCart);
			
			pController.inititateCheckout();
			
			cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 5));
			
			BigDecimal expectedValueOfCartAfterPartialPayment;
			
			expectedValueOfCartAfterPartialPayment = expectedValueOfCart.subtract(new BigDecimal(5));

			Assert.assertTrue(cs.mainScanner.isDisabled());
			Assert.assertTrue(cs.handheldScanner.isDisabled());
			
			pController.addItemsWithPartialPayment();

			Assert.assertFalse(cs.mainScanner.isDisabled());
			Assert.assertFalse(cs.handheldScanner.isDisabled());
			
			expectedValueOfCart = expectedValueOfCartAfterPartialPayment;
			expectedValueOfCart = expectedValueOfCart.add(toastPrice);
			
			scanError(toast);
			
			pController.setValueOfCart(expectedValueOfCart);
			
			pController.inititateCheckout();
			
			cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 10));
			
			Assert.assertTrue(cs.mainScanner.isDisabled());
			Assert.assertTrue(cs.handheldScanner.isDisabled());
			
		}
	
}