package org.lsmr.selfcheckout.customer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.lsmr.selfcheckout.BlockedCardException;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.Card.CardSwipeData;
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

	// The three possible values CardDate.getType() should return
	private final String debit = "DEBIT";
	private final String credit = "CREDIT";
	private final String membership = "MEMBERSHIP";
	
	// Used for testing. Setting verified to false will simulate the bank rejecting the credit/debit card.
	public boolean verified = true;
	
	private BigDecimal valueOfCart;
	private final SelfCheckoutStation checkoutStation; 
	private PCC pcc;
	private PCB pcb;
	private CC cc;
	private List<Coin> coinTrayList;
	private BigDecimal initialValueOfCart;
	private String membershipNo = null;
	
	
	public PaymentController(SelfCheckoutStation cs){
		checkoutStation = cs;
		initialValueOfCart = new BigDecimal(0);
		valueOfCart = new BigDecimal(0);
		coinTrayList = new ArrayList<Coin>();
		
		// Initializing observers
		pcc = new PCC();
		pcb = new PCB();
		cc = new CC();
		
		// Register observers in the coin related devices
		checkoutStation.coinSlot.attach(pcc);
		checkoutStation.coinValidator.attach(pcc);
		checkoutStation.coinTray.attach(pcc);
		
		// Registers observers in the bank note related devices
		checkoutStation.banknoteInput.attach(pcb);
		checkoutStation.banknoteValidator.attach(pcb);
		checkoutStation.banknoteInput.attach(pcb);
		
		// Registers observers in the Card related devices
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
	
	/**
	 * This method is used when an invalid card is read. In the final implementation, an error would
	 * be displayed on the customer's screen, then they would get prompted to choose a payment option.
	*/
	public void displayError() 
	{
		System.out.println("an error has occurred");
		// The user would get sent back to the payment options screen in the final implementation
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
	
	public void notifyManualMembershipEntry(String manualMembershipNo) {
		// Simulates customer entering their membership number through touch screen.
		manualMembershipNo = "405200";
		/**
		 * Simulate going to the database and finding which account corresponds with
		 * the entered Membership number
		*/
		membershipNo = manualMembershipNo;
	}
	
	/**
	 * If all items have been paid for, return true
	 * And disable the coin and bank note slot.
	*/
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
	
	
	//CARD CONTROLLER - Implementation of CardReader observers
	private class CC implements CardReaderObserver {
		/**
		 * Checks to make sure the CVV is of length 3 and only contains digits
		*/
		public boolean verifyCVV(String data) {
			if(data.matches("[0-9]+") && data.length() ==  3) {
				return true;
			}
			return false;
		}
		
		/**
		 * Checks to make sure the Card Number is of length 16 and only contains digits
		*/
		public boolean verifyCardNumber(String data) {
			if(data.matches("[0-9]+") && data.length() ==  3) {
				return true;
			}
			return false;
		}
		
		// Simulates verifying a debit card with the bank. Returns verified.
		public boolean verifyDebitCard(CardData data) {
			return verified;
		}
		
		// Simulates verifying a credit card with the bank. Returns verified.
		public boolean verifyCreditCard(CardData data) {
			return verified;
		}
		
		// Simulates verifying a membership card with the database containing membership info. Returns verified.
		public boolean verifyMembershipCard(CardData data) {
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
			// ignore
		}

		@Override
		public void cardTapped(CardReader reader) {
			System.out.println("Reading card data. Please wait...");
		}

		@Override
		public void cardSwiped(CardReader reader) {
			System.out.println("Reading card data. Please wait...");
		}

		/**
		 * Method is called whenever a card of any type is swiped, inserted or tapped by the CardReader
		*/
		@Override
		public void cardDataRead(CardReader reader, CardData data) {
			String cardType = data.getType();
			String cardNumber = data.getNumber();
			String cardHolder = data.getCardholder();
			String cardCVV = null;
			
			// If the card was swiped you cannot get the CVV information
			if (!(data instanceof CardSwipeData)) {
			cardCVV = data.getCVV();
			}
			
			if(cardType == null) {
				displayError();
			}
			
			else if(cardType == debit) {
				// Card was tapped or inserted. Need to verify CVV
				if (!(data instanceof CardSwipeData)) {
					if(verifyCardNumber(cardNumber) == true && cardHolder != null && verifyCVV(cardCVV) == true) {
						if(verifyDebitCard(data) == true) {
							valueOfCart = new BigDecimal(0);
							isAllItemPaid();
						} else {
							displayError();
						}
					} else {
						displayError();
					}
				
				// Card was swiped. Verify only card name and number
				} else {
					if(verifyCardNumber(cardNumber) == true && cardHolder != null) {
						if(verifyDebitCard(data) == true) {
							valueOfCart = new BigDecimal(0);
							isAllItemPaid();
						} else {
							displayError();
						}
					} else {
						displayError();
					}
				}
			}
			
			else if(cardType == credit) {
				// Card was tapped or inserted. Need to verify CVV
				if (!(data instanceof CardSwipeData)) {
					if(verifyCardNumber(cardNumber) == true && cardHolder != null && verifyCVV(cardCVV) == true) {
						if(verifyDebitCard(data) == true) {
							valueOfCart = new BigDecimal(0);
							isAllItemPaid();
						} else {
							displayError();
						}
					} else {
						displayError();
					}
				
				// Card was swiped. Verify only card name and number
				} else {
					if(verifyCardNumber(cardNumber) == true && cardHolder != null) {
						if(verifyCreditCard(data) == true) {
							valueOfCart = new BigDecimal(0);
							isAllItemPaid();
						} else {
							displayError();
						}
					} else {
						displayError();
					}
				}				
			}
			
			else if(cardType == membership){
				// Membership cards are always swiped
				if(cardHolder != null || verifyCardNumber(cardNumber) == true) {
					if(verifyMembershipCard(data) == true) {
						membershipNo = cardNumber;
					}
				} else {
					displayError();
				}
			}
		}
	}
}