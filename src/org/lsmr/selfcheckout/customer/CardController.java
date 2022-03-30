package org.lsmr.selfcheckout.customer;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;

public class CardController {
	private final SelfCheckoutStation checkoutStation;
	private MCC mcc;
	
	//constructor
	public CardController(SelfCheckoutStation cs) {
	checkoutStation = cs;
	mcc = new MCC();
	
	//Register observers to the scanner
	checkoutStation.cardReader.attach(mcc);
	}
	
	private class MCC implements CardReaderObserver {

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cardInserted(CardReader reader) {
			// ignore - membership cards can only be swiped or have number manually entered.
			
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
		public void cardDataRead(CardReader reader, CardData data) {
			String cardType = data.getType();
			String cardNumber = data.getNumber();
		}
		
		
	}
}