package org.lsmr.selfcheckout.customer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BanknoteSlot;
import org.lsmr.selfcheckout.devices.BanknoteValidator;
import org.lsmr.selfcheckout.devices.CoinSlot;
import org.lsmr.selfcheckout.devices.CoinTray;
import org.lsmr.selfcheckout.devices.CoinValidator;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteSlotObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.CoinSlotObserver;
import org.lsmr.selfcheckout.devices.observers.CoinTrayObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;


public class PaymentController{

	private BigDecimal valueOfCart;
	private final SelfCheckoutStation checkoutStation; 
	private PCC pcc;
	private PCB pcb;
	private List<Coin> coinTrayList;
	private BigDecimal initialValueOfCart;
	
	
	//Customer checkout use case 
	public PaymentController(SelfCheckoutStation cs){
		checkoutStation = cs;
		initialValueOfCart = new BigDecimal(0);
		valueOfCart = new BigDecimal(0);
		coinTrayList = new ArrayList<Coin>();
		
		
		//Initializing observers
		pcc = new PCC();
		pcb = new PCB();
		
		//Register observers in the coin related devices
		checkoutStation.coinSlot.attach(pcc);
		checkoutStation.coinValidator.attach(pcc);
		checkoutStation.coinTray.attach(pcc);
		
		//Registers observers in the bank note related devices
		checkoutStation.banknoteInput.attach(pcb);
		checkoutStation.banknoteValidator.attach(pcb);
		checkoutStation.banknoteInput.attach(pcb);
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
	
	
	//If all items have been paid for, return true
	//And disable the coin and bank note slot
	public boolean isAllItemPaid() {
		if (valueOfCart.compareTo(new BigDecimal(0)) == -1 || valueOfCart.compareTo(new BigDecimal(0)) == 0 ) {
			checkoutStation.coinSlot.disable();
			checkoutStation.banknoteInput.disable();
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
	
}
