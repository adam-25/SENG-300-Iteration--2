package org.lsmr.selfcheckout.customer.testing;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import java.util.Currency;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.customer.PaymentController;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;


public class PaymentControllerTest extends BaseTestClass {


	//declaring self checkout station
	private SelfCheckoutStation cs;
	
	//declaring controller
	private PaymentController pController;

	
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
		try {
			//accept valid banknote
			cs.banknoteInput.accept(banknote);
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
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
}
