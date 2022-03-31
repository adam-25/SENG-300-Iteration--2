package org.lsmr.selfcheckout.customer.testing;

import org.junit.Assert;
import org.junit.Test;

import org.lsmr.selfcheckout.customer.TouchScreenController;

/**
 * Test cases for the touch screen
 */
public class TouchScreenControllerTest extends BaseTestClass {	
	
	/**
	 * System under test
	 */
	TouchScreenController touchScreenController;
	
	@Override
	public void setup() {
		super.setup();
		touchScreenController = new TouchScreenController(checkoutStation);
	}

	//Test if scanner is enabled when the user starts using the checkout station
	@Test
	public void testInitStart() {
		touchScreenController.initiateStart();
		Assert.assertFalse(checkoutStation.handheldScanner.isDisabled());
	}
	
	
	//Test if scanner is disable and coin/banknote slot is enabled
	//When user wishes to checkout and make payment
	@Test
	public void testInitCheckout() {
		touchScreenController.inititateCheckout();
		Assert.assertTrue(checkoutStation.handheldScanner.isDisabled());
		Assert.assertFalse(checkoutStation.banknoteInput.isDisabled());
		Assert.assertFalse(checkoutStation.coinSlot.isDisabled());
	}

	@Test
	public void testEnabled() {
		touchScreenController.enabled(null);
		Assert.assertTrue(touchScreenController.enabledTrue == true);
	}
	
	@Test
	public void testDisabled() {
		touchScreenController.disabled(null);
		Assert.assertTrue(touchScreenController.disabledTrue == true);
	}
	
}
