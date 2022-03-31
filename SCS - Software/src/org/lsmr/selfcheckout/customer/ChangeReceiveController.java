package org.lsmr.selfcheckout.customer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.BlockedCardException;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BanknoteDispenser;
import org.lsmr.selfcheckout.devices.BanknoteSlot;
import org.lsmr.selfcheckout.devices.BanknoteValidator;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.CoinSlot;
import org.lsmr.selfcheckout.devices.CoinTray;
import org.lsmr.selfcheckout.devices.CoinValidator;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.EmptyException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteDispenserObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteSlotObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;
import org.lsmr.selfcheckout.devices.observers.CoinSlotObserver;
import org.lsmr.selfcheckout.devices.observers.CoinTrayObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;

import org.lsmr.selfcheckout.devices.CoinDispenser;
import org.lsmr.selfcheckout.devices.observers.CoinDispenserObserver;

public class ChangeReceiveController {

	// Want to return any money that was over paid by the customer (change)

	private final SelfCheckoutStation checkoutStation;
	private BigDecimal valueOfCart;
	private BigDecimal initialValueOfCart;
	private CDObs cdobs;
	private BnDObs bndobs;
	private BigDecimal changeBack;

	int changeBackList[]; // list to keep track of number of bills/coins returned

	// Integer bankDenominations[] = {5, 10, 20, 50, 100};
	// BigDecimal[] cDenominations = new BigDecimal[] {new BigDecimal(0.05), new
	// BigDecimal(0.10)};

	// Map<BigDecimal, CoinDispenser> dispensers;

	public PaymentController PC;

	// constructor for the customer gets change use case
	public ChangeReceiveController(SelfCheckoutStation cs) throws SimulationException, OverloadException{
		checkoutStation = cs;
		initialValueOfCart = new BigDecimal(0);
		valueOfCart = new BigDecimal(0);
		
		//checkoutStation.coinDenominations = Arrays.asList(cDenominations);
		
		
		cdobs = new CDObs();
		bndobs = new BnDObs();
		
		
		
		
		for(BigDecimal denomination : checkoutStation.coinDispensers.keySet()) {
			CoinDispenser dispenser = checkoutStation.coinDispensers.get(denomination);
			dispenser.attach(cdobs);
			
		}
		for(Integer integer : checkoutStation.banknoteDispensers.keySet()) {
			BanknoteDispenser noteDispenser;
			noteDispenser = checkoutStation.banknoteDispensers.get(integer);
			noteDispenser.attach(bndobs);
			
		}
		
	}

	/**
	 * Method determines how much change is required to be given to customer
	 * 
	 * @returns the change that is due.
	 */
	public BigDecimal changeDue() {
		BigDecimal change = PC.getValueOfCart();

		if (change.compareTo(new BigDecimal(0)) < 0) {
			// then the customer put more money into the machine than they needed to so we
			// must return change
			return change.abs();
		}
		return new BigDecimal(0);
	}

	/**
	 * Calculates how the machine will give the change back to the customer
	 * 
	 * Assume that the bank denominations are in descending order
	 * 
	 * @return
	 * @throws OverloadException
	 * @throws DisabledException
	 * @throws EmptyException
	 */
	public void calcChangeDue() throws EmptyException, DisabledException, OverloadException {

		changeBack = changeDue(); // the total amount of money owed to the customer
		BigDecimal currentChangeLeft;
		int numOutput;

		if (changeBack.compareTo(new BigDecimal(0)) == 0) {
			// no change to give back we are done
			return;
		}

		// the following loop deals with banknote change
		currentChangeLeft = changeBack; // Initial amount of change to give back
		for (Integer i : checkoutStation.banknoteDispensers.keySet()) {
			BanknoteDispenser noteDispenser = checkoutStation.banknoteDispensers.get(i);
			numOutput = currentChangeLeft.intValue() / i;
			for (int j = 0; j < numOutput; j++) {
				noteDispenser.emit(); // emit the bank note

				// user removes banknote and then we can continue dispensing the next
			}
			currentChangeLeft = currentChangeLeft.subtract(new BigDecimal(numOutput).multiply(new BigDecimal(i)));
			System.out.println("numOutPut " + numOutput);
			System.out.println("currentchangeleft " + currentChangeLeft);
		}
			
		// Now we have to give the customer coin change
		if (currentChangeLeft.compareTo(new BigDecimal(0)) <= 0) {
			// if change is already given back then return
			return;

		}
		for (BigDecimal d : checkoutStation.coinDispensers.keySet()) {
			CoinDispenser coinDispenser = checkoutStation.coinDispensers.get(d);
			numOutput = currentChangeLeft.divide(d).intValue();
			for (int k = 0; k < numOutput; k++) {
				coinDispenser.emit(); // emit the coin

				// user removes coin and then we can continue dispensing the next
			}
			currentChangeLeft = currentChangeLeft.subtract(new BigDecimal(numOutput).multiply(d));
			System.out.println("Coin numOutPut " + numOutput);
			System.out.println("Coing currentchangeleft " + currentChangeLeft);
		}

	}

	/**
	 * This implements the observers required for the customer receives coins
	 * 
	 *
	 */
	private class CDObs implements CoinDispenserObserver, CoinTrayObserver {

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore

		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore

		}

		@Override
		public void coinsFull(CoinDispenser dispenser) {
			// Ignore. if full we are good

		}

		@Override
		public void coinsEmpty(CoinDispenser dispenser) {
			// coin dispenser is empty

		}

		@Override
		public void coinAdded(CoinDispenser dispenser, Coin coin) {
			// This is done by the attendant

		}

		@Override
		public void coinRemoved(CoinDispenser dispenser, Coin coin) {
			// coin has been removed from the dispenser
			// if coinRemoved then we also indicate that the removed coin is now in the tray
			try {
				checkoutStation.coinTray.accept(coin);
			} catch (OverloadException e) {

				e.printStackTrace();
			} catch (DisabledException e) {

				e.printStackTrace();
			}

		}

		@Override
		public void coinAdded(CoinTray tray) {
			// Indicates a coin has been added to the tray for the customer to pick up
			tray.collectCoins();
		}

		@Override
		public void coinsLoaded(CoinDispenser dispenser, Coin... coins) {
			// Ignore. This is done by the attendant

		}

		@Override
		public void coinsUnloaded(CoinDispenser dispenser, Coin... coins) {
			// Ignore. This is done by the attendant

		}

	}

	/**
	 * This implements the observers required for the customer receives banknotes
	 * 
	 *
	 */
	public class BnDObs implements BanknoteDispenserObserver, BanknoteSlotObserver {

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore

		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore

		}

		@Override
		public void moneyFull(BanknoteDispenser dispenser) {
			// if full we good

		}

		@Override
		public void banknotesEmpty(BanknoteDispenser dispenser) {
			// bank note dispenser is empty

		}

		@Override
		public void billAdded(BanknoteDispenser dispenser, Banknote banknote) {
			// Ignore this is done by the attendant

		}

		@Override
		public void banknoteRemoved(BanknoteDispenser dispenser, Banknote banknote) {
			// a banknote has been emitted for the customer to take
			try {
				checkoutStation.banknoteOutput.removeDanglingBanknote();
			} catch (SimulationException e) {
				throw new SimulationException(
						"A banknote is already dangling from the slot. Remove that before ejecting another.");
				// e.printStackTrace();
			}

		}

		@Override
		public void banknotesLoaded(BanknoteDispenser dispenser, Banknote... banknotes) {
			// Ignore. This is done by the attendant

		}

		@Override
		public void banknotesUnloaded(BanknoteDispenser dispenser, Banknote... banknotes) {
			// Ignore. This is done by the attendant

		}

		@Override
		public void banknoteInserted(BanknoteSlot slot) {
			// Ignore

		}

		@Override
		public void banknoteEjected(BanknoteSlot slot) {
			// A bank note has been ejected

			// for the purpose of this iteration we assume the user takes the banknote right
			// away:
		}

		@Override
		public void banknoteRemoved(BanknoteSlot slot) {
			// The user has removed the banknote
			// so we continue the banknote dispensing

		}

	}

}
