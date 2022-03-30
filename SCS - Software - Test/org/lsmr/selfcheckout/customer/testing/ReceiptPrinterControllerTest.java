package org.lsmr.selfcheckout.customer.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashMap;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.ChipFailureException;
import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.MagneticStripeFailureException;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.customer.BaggingAreaController;
import org.lsmr.selfcheckout.customer.PaymentController;
import org.lsmr.selfcheckout.customer.ReceiptPrinterController;
import org.lsmr.selfcheckout.customer.ScanItemController;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.Numeral;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

//Mohamed

public class ReceiptPrinterControllerTest extends BaseTestClass{
	
	//declaring self checkout station
	private SelfCheckoutStation cs;
	
	//declaring controllers 
	private ReceiptPrinterController RPcontroller;
	private PaymentController PAcontroller;
	private ScanItemController SIcontroller;
	private BaggingAreaController bAcontroller;
	
	//initializing prices of items
	BigDecimal milkPrice = new BigDecimal(2.50);
	BigDecimal eggPrice = new BigDecimal(4.00);
	BigDecimal toastPrice = new BigDecimal(3.50);
	
	Numeral[] nMilk = {Numeral.one, Numeral.two, Numeral.three, Numeral.four};
	Numeral[] nEggs = {Numeral.two, Numeral.three, Numeral.four, Numeral.one};
	Numeral[] nToast = {Numeral.three, Numeral.two, Numeral.four, Numeral.one};
	
	
	//initalizing barcodes of the items
	Barcode barcodeMilk = new Barcode(nMilk);
	Barcode barcodeEggs = new Barcode(nEggs);
	Barcode barcodeToast = new Barcode(nToast);
	
	
	//declaring hashmap barcodePrice hashmap and barcodeWeight hashmap
	private final HashMap<Barcode, BigDecimal> barcodePrice = new HashMap<Barcode, BigDecimal>();
	private final HashMap<Barcode, Double> barcodeWeight = new HashMap<Barcode, Double>();
	private final HashMap<Barcode, String> barcodeDescription = new HashMap<Barcode, String>();
	
	
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
		
		//initializng barcode description 
		barcodeDescription.put(barcodeMilk, "Milk");	//milk
		barcodeDescription.put(barcodeEggs, "Eggs");	//eggs
		barcodeDescription.put(barcodeToast, "Toast");	//toast
		
		
		
		//initalizing controllers
		SIcontroller = new ScanItemController(cs, barcodePrice, barcodeWeight);
		PAcontroller = new PaymentController(cs);
		RPcontroller = new ReceiptPrinterController(cs,barcodePrice, barcodeDescription);
		bAcontroller = new BaggingAreaController(cs);
		
		
		//calling object methods
		bAcontroller.setScanItemControl(SIcontroller);
		SIcontroller.setBagAreaControl(bAcontroller);
		RPcontroller.setControllers(SIcontroller, PAcontroller);
		
	}
	//Utility function for scanning, also scans items normally despite the name.
	public void scanError(BarcodedItem item) {
		while (true) {
			cs.handheldScanner.scan(item);
			
			if(SIcontroller.numOfScannedItems() == 1 + bAcontroller.getNumOfItemsInBaggingArea()) {
				cs.baggingArea.add(item);
				break;
			}
		}
	}
	
	
	//Test if controller expected receipt match the one from the printer
	@Test
	public void test1() throws DisabledException, OverloadException {
		
	BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
	BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
	BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
	
	//Scanning Items
	scanError(milk);
	scanError(eggs);
	scanError(toast);
	
	//Total Cost of Item is 10.00
	PAcontroller.setValueOfCart(new BigDecimal(10));
	
	//Item has been paid for
	cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 10));
	
	//Print the receipt
	RPcontroller.printReceipt();
	
	cs.printer.cutPaper();
	
	//Check if controller expected receipt match the one from the printer
	Assert.assertEquals(RPcontroller.getReceipt(), cs.printer.removeReceipt());
	}
	
	@Test
	public void membershipNoTest() {
		
	BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
	BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
	BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
	
	//Making the membership card
	Card mcard = new Card("MEMBERSHIP", "1234567890123456", "Jimmy Johnson", null, null, false, false);
	
	while (!PAcontroller.getCardData())
	{
		try {
			cs.cardReader.swipe(mcard);
		} 
		catch (MagneticStripeFailureException e)
		{
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Scanning Items
	scanError(milk);
	scanError(eggs);
	scanError(toast);
	
	//Total Cost of Item is 10.00
	PAcontroller.setValueOfCart(new BigDecimal(10));
	
	//Item has been paid for
	try {
		cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 10));
	} catch (DisabledException | OverloadException e) {
		e.printStackTrace();
	}
	
	//Print the receipt
	RPcontroller.printReceipt();
	
	cs.printer.cutPaper();
	
	//Check if controller expected receipt match the one from the printer
	Assert.assertEquals(RPcontroller.getReceipt(), cs.printer.removeReceipt());
	}
	
	@Test
	public void membershipCardDisplayErrorTest() {
		
	BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
	BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
	BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
	
	//Making the membership card
	Card mcard = new Card("MEMBERSHIP", "405119", "Name", null, null, false, false);
	
	while (!PAcontroller.getCardData())
	{
		try {
			cs.cardReader.swipe(mcard);
		} 
		catch (MagneticStripeFailureException e)
		{
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Scanning Items
	scanError(milk);
	scanError(eggs);
	scanError(toast);
	
	//Total Cost of Item is 10.00
	PAcontroller.setValueOfCart(new BigDecimal(10));
	
	//Item has been paid for
	try {
		cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 10));
	} catch (DisabledException | OverloadException e) {
		e.printStackTrace();
	}
	
	//Print the receipt
	RPcontroller.printReceipt();
	
	cs.printer.cutPaper();
	
	//Check if controller expected receipt match the one from the printer
	Assert.assertTrue(PAcontroller.getShowError());
	}
	
	@Test
	public void membershipCardNoOnReceiptTest()
	{
		BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
		BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
		BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);
		
		Card mcard = new Card("MEMBERSHIP", "1234567890123456", "Name", null, null, false, false);
		
		while (!PAcontroller.getCardData())
		{
			try {
				cs.cardReader.swipe(mcard);
			} 
			catch (MagneticStripeFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Scanning Items
		scanError(milk);
		scanError(eggs);
		scanError(toast);
		
		PAcontroller.setValueOfCart(new BigDecimal(10));
			
		try {
			cs.banknoteInput.accept(new Banknote(Currency.getInstance("CAD"), 10));
		} catch (DisabledException | OverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RPcontroller.printReceipt();
		
		cs.printer.cutPaper();
		
		Assert.assertTrue(cs.printer.removeReceipt().contains(PAcontroller.getMembershipNo()));
		
	}
	
	@Test
	public void manualEntryMembershipNoTest()
	{
		BarcodedItem milk = new BarcodedItem(barcodeMilk, 3.0);
		BarcodedItem eggs = new BarcodedItem(barcodeEggs, 2.0);
		BarcodedItem toast = new BarcodedItem(barcodeToast, 5.0);	
		
		scanError(milk);
		scanError(eggs);
		scanError(toast);
		
		PAcontroller.setValueOfCart(new BigDecimal(10));
		
		PAcontroller.manualMembershipEntry("1234567890123456");
		
		RPcontroller.printReceipt();
		
		cs.printer.cutPaper();
		
		Assert.assertTrue(cs.printer.removeReceipt().contains(PAcontroller.getMembershipNo()));	
	}
	
}