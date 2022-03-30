package org.lsmr.selfcheckout.customer;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;


public class TouchScreenController  {
		
	private final SelfCheckoutStation checkoutStation;
	protected boolean askAttendantHelp = false;
	
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
		checkoutStation.mainScanner.enable();
		checkoutStation.handheldScanner.enable();
	}
	
	/**
	 * To be triggered after all items are scanned. Disables scanner and allows the user to pay with cash.
	 */
	public void inititateCheckout() {
		checkoutStation.mainScanner.disable();
		checkoutStation.handheldScanner.disable();
		checkoutStation.coinSlot.enable();
		checkoutStation.banknoteInput.enable();
	}
}