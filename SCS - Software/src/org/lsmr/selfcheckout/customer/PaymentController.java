package org.lsmr.selfcheckout.customer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.lsmr.selfcheckout.BlockedCardException;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BanknoteSlot;
import org.lsmr.selfcheckout.devices.BanknoteValidator;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.CoinSlot;
import org.lsmr.selfcheckout.devices.CoinTray;
import org.lsmr.selfcheckout.devices.CoinValidator;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteSlotObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;
import org.lsmr.selfcheckout.devices.observers.CoinSlotObserver;
import org.lsmr.selfcheckout.devices.observers.CoinTrayObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;


public class PaymentController{

	private BigDecimal valueOfCart;
	private final SelfCheckoutStation checkoutStation; 
	private PCC pcc;
	private PCB pcb;
	private CC cc;
	private List<Coin> coinTrayList;
	private BigDecimal initialValueOfCart;
	private final String debit = "DEBIT";
	private final String credit = "CREDIT";
	private final String membership = "MEMBERSHIP";
	public boolean verified = true;
	private String membershipNo = null;
	
	
	
	//Customer checkout use case 
	public PaymentController(SelfCheckoutStation cs){
		checkoutStation = cs;
		initialValueOfCart = new BigDecimal(0);
		valueOfCart = new BigDecimal(0);
		coinTrayList = new ArrayList<Coin>();
		
		
		//Initializing observers
		pcc = new PCC();
		pcb = new PCB();
		cc = new CC();
		
		//Register observers in the coin related devices
		checkoutStation.coinSlot.attach(pcc);
		checkoutStation.coinValidator.attach(pcc);
		checkoutStation.coinTray.attach(pcc);
		
		//Registers observers in the bank note related devices
		checkoutStation.banknoteInput.attach(pcb);
		checkoutStation.banknoteValidator.attach(pcb);
		checkoutStation.banknoteInput.attach(pcb);
		
		//Registers observers in the Card related devices
		 checkoutStation.cardReader.attach(cc);
	}
	
	public BigDecimal getValueOfCart() {
		return valueOfCart;
	}
	
	public BigDecimal getInitialValueOfCart() {
		return initialValueOfCart;
	}
	
	public void setValueOfCart(BigDecimal cartValue) {
		initialValueOfCart = cartValue;
		valueOfCart = cartValue;
	}
	
	//this method is used when an invalid card is read, in the final implementation, an error would
	//be displayed on the customer's screen and then would prompt him to select a payment option
	public void displayError() 
	{
		System.out.println("an error has occured");
		//go back to payment options
	}
	
	public String getMembershipNo()
	{
		return membershipNo;
	}
	
	public boolean hasMembership()
	{
		if(membershipNo == null)
		{
			return false;
		}
		
		return true;
	}
	
	//If all items have been paid for, return true
	//And disable the coin and bank note slot
	public boolean isAllItemPaid() {
		if (valueOfCart.compareTo(new BigDecimal(0)) == -1 || valueOfCart.compareTo(new BigDecimal(0)) == 0 ) {
			checkoutStation.coinSlot.disable();
			checkoutStation.banknoteInput.disable();
			checkoutStation.cardReader.disable();
			return true;
		}
		return false;
	}
	

	public List<Coin> getCoinTrayList() {
		return coinTrayList;
	}
	
	//COIN PAYMENT - Implementation of Coin observers
	private class PCC implements CoinSlotObserver, CoinValidatorObserver, CoinTrayObserver{
		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore	
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore
		}

		
		@Override
		public void coinInserted(CoinSlot slot) {
			// Ignore
		}

		
		@Override
		public void validCoinDetected(CoinValidator validator, BigDecimal value) {
			valueOfCart = valueOfCart.subtract(value);
			isAllItemPaid();
		}

		@Override
		public void invalidCoinDetected(CoinValidator validator) {
			//Ignore 
		}
		
		@Override
		public void coinAdded(CoinTray tray) {
			
			//Simulates removal of coin from the coin tray
			for(Coin theCoin :tray.collectCoins() ) {
				coinTrayList.add(theCoin);
			}
			
		}
	}
	
	
	
	
	//BANKNOTE PAYMENT - Implementation of Bank note observers
	private class PCB implements BanknoteSlotObserver, BanknoteValidatorObserver{
		
		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {			
			//Ignore
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			//Ignore
		}
		
		@Override
		public void banknoteInserted(BanknoteSlot slot) {
			//Ignore 
		}

		
		@Override
		public void banknoteEjected(BanknoteSlot slot) {
			//Ignore
		}

		@Override
		public void banknoteRemoved(BanknoteSlot slot) {
			//Ignore
		}

		@Override
		public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
			//Subtract the value of cart from the customer bank note value
			BigDecimal bigDecimalVal = new BigDecimal(value);
			valueOfCart = valueOfCart.subtract(bigDecimalVal);
			isAllItemPaid();
		}

		@Override
		public void invalidBanknoteDetected(BanknoteValidator validator) {
			// Ignore
		}
	}
	
	private class CC implements CardReaderObserver {
		
		public boolean verifyCVV(String data)
		{
			return false;
		}
		public boolean verifyCardNumber(String data)
		{
			return false;
		}
		
		public boolean verifyDebitCard(CardData data)
		{
			return verified;
		}
		
		public boolean verifyCreditCard(CardData data)
		{
			return verified;
		}
		
		public boolean verifyMembershipCard(CardData data)
		{
			return verified;
		}
	

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// ignore
			
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// ignore
			
		}

		@Override
		public void cardInserted(CardReader reader) {
			// ignore 
			
		}

		@Override
		public void cardRemoved(CardReader reader) {
			// ignore - membership cards can only be swiped or have number manually entered.
			
		}

		@Override
		public void cardTapped(CardReader reader) {
			// ignore - membership cards can only be swiped or have number manually entered.
			
		}

		@Override
		public void cardSwiped(CardReader reader) {
			System.out.println("Reading card data. Please wait...");
		
			
		}

		@Override
		public void cardDataRead(CardReader reader, CardData data){
			String cardType = data.getType();
			String cardNumber = data.getNumber();
			String cardHolder = data.getCardholder();
			String cardCVV = data.getCVV();
			
			if(cardType == null)
			{
					displayError();
			}
			
			if(cardType == debit)
			{
				//review try catch logic
				if(verifyCardNumber(cardNumber) == false || cardHolder == null || verifyCVV(cardCVV) == false)
				{
					displayError();
				}
				
				if(verifyDebitCard(data) == true)
				{
					valueOfCart = new BigDecimal(0);
					isAllItemPaid();
				}
				else {
					displayError();
				}
				
			}
			
			else if(cardType == credit)
			{
				//review try catch logic
				if(verifyCardNumber(cardNumber) == false || cardHolder == null || verifyCVV(cardCVV) == false)
				{
					displayError();
				}
				
				if(verifyCreditCard(data) == true)
				{
					valueOfCart = new BigDecimal(0);
					isAllItemPaid();
				}
				else {
					displayError();
				}

				
			
			}
			
			else if(cardType == membership)
			{
				if(cardHolder == null || verifyCardNumber(cardNumber) == false)
				{
					displayError();
				}
				if(verifyMembershipCard(data) == true)
				{
					membershipNo = cardNumber;
				}
				
				
			}
			
		}
		
		
	}
	
}