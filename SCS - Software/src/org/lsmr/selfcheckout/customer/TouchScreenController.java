package org.lsmr.selfcheckout.customer;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;


public class TouchScreenController  {
		
	private final SelfCheckoutStation checkoutStation;
	
	//Constructor - Initialize SelfCheckoutStation
	
	/**
	 * Creates a new touch screen controller for the specified checkout station.
	 */
	public TouchScreenController(SelfCheckoutStation cs) {
		checkoutStation = cs;
	}
	
	/**
	 * Enables the scanner to allow the user to begin scanning items.
	 */
	public void initiateStart() {
		checkoutStation.scanner.enable();
	}
	
	/**
	 * To be triggered after all items are scanned. Disables scanner and allows the user to pay with cash.
	 */
	public void inititateCheckout() {
		checkoutStation.scanner.disable();
		checkoutStation.coinSlot.enable();
		checkoutStation.banknoteInput.enable();
	}
}
