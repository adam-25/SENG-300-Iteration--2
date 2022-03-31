package org.lsmr.selfcheckout.customer;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.TouchScreenObserver;



public class TouchScreenController implements TouchScreenObserver {
		
	private final SelfCheckoutStation checkoutStation;
	public checkoutState state;
	
	// These 2 booleans are for Iteration 2 testing
	public boolean enabledTrue = false;
	public boolean disabledTrue = false;
	// Delete for Iteration 3 ^^
	
	public enum checkoutState{
		SCAN, PAY
	}
	//Constructor - Initialize SelfCheckoutStation
	
	/**
	 * Creates a new touch screen controller for the specified checkout station.
	 */
	public TouchScreenController(SelfCheckoutStation cs) {
		checkoutStation = cs;
		state = checkoutState.SCAN;
		
		
	}
	
	/**
	 * Enables the scanner to allow the user to begin scanning items.
	 */
	public void initiateStart() {
		state = checkoutState.SCAN;
		checkoutStation.mainScanner.enable();
		checkoutStation.handheldScanner.enable();
		
	}
	
	/**
	 * To be triggered after all items are scanned. Disables scanner and allows the user to pay with cash.
	 */
	public void inititateCheckout() {
		state = checkoutState.PAY;
		checkoutStation.mainScanner.disable();
		checkoutStation.handheldScanner.disable();
		checkoutStation.coinSlot.enable();
		checkoutStation.banknoteInput.enable();
	}

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// For testing purposes
		enabledTrue = true;
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// For testing purposes
		disabledTrue = true;
		
	}
}