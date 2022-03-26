package org.lsmr.selfcheckout.customer.testing;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

/**
 * A parent unit test class which has a self checkout station initialized already.
 */
public class BaseTestClass {
	
	BigDecimal dec1 = new BigDecimal(0.50);
	BigDecimal dec2 = new BigDecimal(1);
	BigDecimal dec3 = new BigDecimal(2);
	
	//declaring parameters for self checkout station
	private Currency c1 = null;
	private final int[] banknoteDenominations = {5, 10 , 15, 20};
	public final BigDecimal[] coinDenominations = {dec1, dec2, dec3};
	private int scaleMaxWeight;
	private int scaleSensitivity;
	
	/**
	 * Attach observers to this to test the observers.
	 */
	SelfCheckoutStation checkoutStation;
	
	/**
	 * Initializes SelfCheckoutStation with dummy data.
	 */
	@Before
	public void setup() {
		//initializing parameters for SCS
		c1 = Currency.getInstance("CAD");
		scaleMaxWeight = 2000;
		scaleSensitivity = 1;

		//initializing SCS
		checkoutStation = new SelfCheckoutStation(c1, banknoteDenominations, coinDenominations, scaleMaxWeight, scaleSensitivity);

	}
	
	@Test
	public void testCase() {
		
	}
}
