package org.lsmr.selfcheckout.customer.testing;


import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;

import java.util.Currency;
import java.util.HashMap;
import java.util.List;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.MagneticStripeFailureException;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.customer.BaggingAreaController;
import org.lsmr.selfcheckout.customer.PaymentController;
import org.lsmr.selfcheckout.customer.ReceiptPrinterController;
import org.lsmr.selfcheckout.customer.ScanItemController;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.ChipFailureException;
import org.lsmr.selfcheckout.devices.SimulationException;

public class PaymentControllerTest extends BaseTestClass {


	//declaring self checkout station
	private SelfCheckoutStation cs;
	
	//declaring controller
	private PaymentController pController;
	
	private ReceiptPrinterController rController;
	
	private ScanItemController SIcontroller;
	
	private BaggingAreaController bAcontroller;
	
	private ReceiptPrinterController RPcontroller;
	

	
	BigDecimal milkPrice = new BigDecimal(2.50);
	Numeral[] nMilk = {Numeral.one, Numeral.two, Numeral.three, Numeral.four};
	Barcode barcodeMilk = new Barcode(nMilk);
	HashMap<Barcode, BigDecimal> barcodePrice = new HashMap<Barcode, BigDecimal>();
	HashMap<Barcode, Double> barcodeWeight = new HashMap<Barcode, Double>();
	HashMap<Barcode, String> barcodeDescription = new HashMap<Barcode, String>();
	
	//Payment Controller
	@Before
	public void setup() {
		
		super.setup();

		//Initialize self checkout station
		cs = checkoutStation;

		//initializing parameters for PaymentController
		BigDecimal totalCost = new BigDecimal(20);

		//initializing payment controller
		pController = new PaymentController(cs);
		
		SIcontroller = new ScanItemController(cs, barcodePrice, barcodeWeight);
		bAcontroller = new BaggingAreaController(cs);
		RPcontroller = new ReceiptPrinterController(cs,barcodePrice, barcodeDescription);
		
		bAcontroller.setScanItemControl(SIcontroller);
		SIcontroller.setBagAreaControl(bAcontroller);
		RPcontroller.setControllers(SIcontroller, pController);

	}
	
	//Test if payment can be made with all coins
	@Test
	public void test1() {
		//initialize a new total cost
		BigDecimal totalCost = new BigDecimal(2);
		
		//initialize payment controller
		pController.setValueOfCart(totalCost);

		Coin coin = new Coin(Currency.getInstance("CAD"), dec3);
		
		try {
			//accept valid coin
			cs.coinSlot.accept(coin);
		} catch (DisabledException e) {
			e.printStackTrace();
		}
		
		//See if cost was paid
		//Value of cart at the start of test is $2
		//After coin slot accepts and validates, it should be $0
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());
	}
	
	
	//Test if payment can be made in all bank notes
	@Test
	public void test2() {
		//initialize a new total cost
		BigDecimal totalCost = new BigDecimal(20);

		//Change the value of cart 
		pController.setValueOfCart(totalCost);

		Banknote banknote = new Banknote(Currency.getInstance("CAD"), 20);
		
		while (pController.isAcceptBanknote() == false) {
		try {
			//accept valid banknote
			cs.banknoteInput.accept(banknote);
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
		
		}
		
		//See if cost was paid
		//Value of cart at the start of test is $20
		//After banknote slot accepts and validates, it should be $0
		assertEquals(new BigDecimal(0), pController.getValueOfCart());

	}
	
	//Test if payment can be made with mix of bank note and coins 
	@Test
	public void test3() {
		//initialize a new total cost
		BigDecimal totalCost = new BigDecimal(17);
	
		//Change the value of cart 
		pController.setValueOfCart(totalCost);
		
		Banknote banknote = new Banknote(Currency.getInstance("CAD"), 15);
		Coin coin = new Coin(Currency.getInstance("CAD"), dec3);
		try{
			//accept payment
			cs.banknoteInput.accept(banknote);
			cs.coinSlot.accept(coin);
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
		
		//See if cost was paid
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());
	}
	
	//Test if the coin/banknote slot throw disable exception 
	//After valueOfCart is paid for and you want to make another payment
	@Test
	public void test4() {
		//Initialize a new total cost
		BigDecimal totalCost = new BigDecimal(15);
		
		//Change the value of cart 
		pController.setValueOfCart(totalCost);
		
		//This banknote covers the payment of the cart
		Banknote banknote = new Banknote(Currency.getInstance("CAD"), 15);
		
		//Extra payment you want to make
		Coin coin = new Coin(Currency.getInstance("CAD"), dec3);
		
		try{
			//accept payment
			cs.banknoteInput.accept(banknote);
			//Causes DisabledException
			cs.coinSlot.accept(coin);
		} catch (DisabledException e) {
			Assert.assertTrue(true);
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	
	//Test if invalid coin does not reduce the valueOfCart
	//But instead goes to coin tray
	@Test
	public void test5() {
		//Initialize a new total cost
		BigDecimal totalCost = new BigDecimal(15);
		
		//Change the value of cart 
		pController.setValueOfCart(totalCost);
		
		Coin coin = new Coin(Currency.getInstance("USD"), dec2);
		try {
			pController.setValueOfCart(totalCost);
			//try to accept invalid coin
			cs.coinSlot.accept(coin);
		} catch (DisabledException e) {
			e.printStackTrace();
		}
		
		//Value of Cart stays the same
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		//Since invalid coin are placed in coin tray
		//The coin inserted is identical to the invalid coin in the coin tray 
		Assert.assertEquals(coin, pController.getCoinTrayList().get(0));
	}
	
	//Test if invalid banknote does not reduce the valueOfCart
	//But instead is a dangling banknote
	@Test
	public void test6() {
		
		//Initialize a new total cost
		BigDecimal totalCost = new BigDecimal(15);
		
		//Change the value of cart 
		pController.setValueOfCart(totalCost);
		
		Banknote banknote = new Banknote(Currency.getInstance("CAD"), 25);
		Banknote banknote2 = new Banknote(Currency.getInstance("CAD"), 10);
		try {
			//try to accept invalid note
			cs.banknoteInput.accept(banknote);
		} catch (DisabledException | OverloadException e) {
			e.printStackTrace();
		}
		
		//Value of Cart stays the same
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		
		//Dangling banknote leads to an overload exception due to accepting another banknote
		try {
			cs.banknoteInput.accept(banknote2);
		}catch(OverloadException e){
			Assert.assertTrue(true);
		}catch(DisabledException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDebitPaymentTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", true, true);
		
		while (!pController.getCardData())
		{
			
			try {
				cs.cardReader.tap(card);
			}
			catch(ChipFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());
	}

	@Test
	public void testDebitVerifyNumberFailInTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "12345", "Name", "123", "1234", true, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(ChipFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}
	
	@Test
	public void testDebitVerifyDataFailInTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", true, true);
		pController.verified = false;
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(ChipFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testDebitVerifyNumberFailInSwipe()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "12345", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.swipe(card);
			}
			catch (MagneticStripeFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertTrue(pController.getShowError());
		Assert.assertEquals(totalCost, pController.getValueOfCart());
	}

	@Test
	public void testDebitVerifyDataFailInSwipe()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", false, true);
		pController.verified = false;
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.swipe(card);
			}
			catch (MagneticStripeFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testDebitVerifyNumberFailInInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "12345", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(ChipFailureException e)
				{
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testDebitVerifyDataFailInInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", false, true);
		pController.verified = false;
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(ChipFailureException e)
				{
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}
	
	@Test
	public void testDebitPaymentTapFail()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", false, true);
		
		try {
			cs.cardReader.tap(card);
		}
		catch(ChipFailureException e)
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
	}
	
	@Test
	public void testDebitPaymentInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(ChipFailureException e)
				{
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());
	}
	
	@Test
	public void testDebitPaymentSwipe()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.swipe(card);
			}
			catch(MagneticStripeFailureException e)
			{
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());
	}

	@Test
	public void testCreditVerifyNumberFailInTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "12345", "Name", "123", "1234", true, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(ChipFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}
	
	@Test
	public void testCreditVerifyDataFailInTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", true, true);
		pController.verified = false;
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(ChipFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testCreditVerifyNumberFailInSwipe()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "12345", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.swipe(card);
			}
			catch(MagneticStripeFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertTrue(pController.getShowError());
		Assert.assertEquals(totalCost, pController.getValueOfCart());
	}

	@Test
	public void testCreditVerifyDataFailInSwipe()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", false, true);
		pController.verified = false;
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.swipe(card);
			}
			catch (MagneticStripeFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testCreditVerifyNumberFailInInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "12345", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(ChipFailureException e)
				{
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testCreditVerifyDataFailInInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", false, true);
		pController.verified = false;
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(ChipFailureException e)
				{
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue(pController.getShowError());
	}

	@Test
	public void testCreditPaymentTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", true, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(ChipFailureException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testCreditPaymentInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(ChipFailureException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());
	}
	
	@Test
	public void testCreditPaymentTapFail()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", false, true);
		
		try {
			cs.cardReader.tap(card);
		}
		catch(ChipFailureException e)
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
	}
	
	@Test
	public void testCreditPaymentSwipe()
	{
		
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "123", "1234", false, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.swipe(card);
			}
			catch(MagneticStripeFailureException e)
			{
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Assert.assertEquals(new BigDecimal(0), pController.getValueOfCart());		
	}
	
	@Test
	public void cardTypeNullSwipe()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		try {
			Card card = new Card(null, "1234567890123456", "Name", "123", "1234", false, true);
			cs.cardReader.swipe(card);
		}
		catch(SimulationException e)
		{
			Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
		}
		catch(Exception e)	{
			fail("Expected NullPointerException");
		}
		
	}
	
	@Test
	public void cardTypeNullTap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
				
		try {
			Card card = new Card(null, "1234567890123456", "Name", "123", "1234", false, true);
			cs.cardReader.tap(card);
		}
		catch(SimulationException e)
		{
			Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
		}
		catch(Exception e)	{
			fail("Expected NullPointerException");
		}
		
	}
		
	@Test
	public void cardTypeNullInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
			
		try {
			Card card = new Card(null, "1234567890123456", "Name", "123", "1234", false, true);
			cs.cardReader.insert(card, null);
		}
		catch(SimulationException e)
		{
			Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
		}
		catch(Exception e)	{
			fail("Expected NullPointerException");
		}
	}
	
	@Test
	public void debitVerifyCVVfailtap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "1234", "1234", true, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(SimulationException e)
			{
				Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
			}
			catch(Exception e)	{
				fail("Expected NullPointerException");
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue("Expected Display an error.", pController.getShowError());
		
	}
	
	@Test
	public void debitVerifyCVVfailInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("DEBIT", "1234567890123456", "Name", "1234", "1234", true, true);
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(SimulationException e)
				{
					Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
				}
				catch(Exception e)	{
					fail("Expected NullPointerException");
				}
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue("Expected Display an error.", pController.getShowError());
		
	}
	
	@Test
	public void creditVerifyCVVfailtap()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "1234", "1234", true, true);
		
		while (!pController.getCardData())
		{
			try {
				cs.cardReader.tap(card);
			}
			catch(SimulationException e)
			{
				Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
			}
			catch(Exception e)	{
				fail("Expected NullPointerException");
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue("Expected Display an error.", pController.getShowError());
		
	}
	
	@Test
	public void creditVerifyCVVfailInsert()
	{
		BigDecimal totalCost = new BigDecimal(20);
		
		pController.setValueOfCart(totalCost);
		
		Card card = new Card("CREDIT", "1234567890123456", "Name", "1234", "1234", true, true);
		
		while (!pController.getCardData())
		{
			if (pController.getCardInserted())
				cs.cardReader.remove();
			else
			{
				try {
					cs.cardReader.insert(card, "1234");
				}
				catch(SimulationException e)
				{
					Assert.assertTrue("NullPointerException expected.", e instanceof SimulationException);
				}
				catch(Exception e)	{
					fail("Expected NullPointerException");
				}
			}
		}
		
		Assert.assertEquals(totalCost, pController.getValueOfCart());
		Assert.assertTrue("Expected Display an error.", pController.getShowError());
		
	}
}