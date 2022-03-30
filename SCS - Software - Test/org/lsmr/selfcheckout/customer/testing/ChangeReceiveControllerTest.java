package org.lsmr.selfcheckout.customer.testing;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.EmptyException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.customer.ChangeReceiveController;
import org.lsmr.selfcheckout.customer.*;

public class ChangeReceiveControllerTest extends BaseTestClass {
	private ChangeReceiveController CRC;
	// private PaymentController PCTest;

	@Before
	public void setup() {

		// loads in preset data from BaseTestClass
		super.setup();

		// Testing Constructor
		CRC = new ChangeReceiveController(checkoutStation);

		// Instantiating PaymentController
		CRC.PC = new PaymentController(checkoutStation);

	}

	@Test
	public void changeDueGTZeroTest() {

		CRC.PC.setValueOfCart(BigDecimal.valueOf(10));
		Assert.assertEquals(CRC.changeDue(), BigDecimal.valueOf(0));

	}

	@Test
	public void changeDueLTZeroTest() {
		CRC.PC.setValueOfCart(BigDecimal.valueOf(-1));
		BigDecimal change = CRC.PC.getValueOfCart();

		Assert.assertEquals(CRC.changeDue(), change.abs());
	}

	@Test
	public void calcChangeDueNOChangeLeftTest() throws EmptyException, DisabledException, OverloadException {
		CRC.PC.setValueOfCart(BigDecimal.valueOf(10));
		CRC.calcChangeDue();
	}

	@Test
	public void calcChangeDueChangeLeftTest1() throws EmptyException, DisabledException, OverloadException {
		BigDecimal dec1 = new BigDecimal(0.50);
		BigDecimal dec2 = new BigDecimal(1);
		BigDecimal dec3 = new BigDecimal(2);
		
		Currency validCurrency = Currency.getInstance("CAD");
		int[] validBanknoteDenominations = {100,15,10,5};
		BigDecimal[] validCoinDenominations = {dec1, dec2, dec3};
		int scaleMaxWeight = 2000;
		int scaleSensitivity = 1;
		SelfCheckoutStation sc = new SelfCheckoutStation(validCurrency, validBanknoteDenominations, validCoinDenominations, scaleMaxWeight, scaleSensitivity);
		
		ChangeReceiveController CRCTest = new ChangeReceiveController(sc);
		CRCTest.PC = new PaymentController(sc);
		CRCTest.PC.setValueOfCart(BigDecimal.valueOf(-20));
		CRCTest.calcChangeDue();
		
//		CRC.PC.setValueOfCart(BigDecimal.valueOf(-1));
//		CRC.calcChangeDue();
		
	}
	
	@Test
	public void calcChangeDueChangeLeftTest2() throws EmptyException, DisabledException, OverloadException {
		BigDecimal dec1 = new BigDecimal(0.50);
		BigDecimal dec2 = new BigDecimal(1);
		BigDecimal dec3 = new BigDecimal(2);
		
		Currency validCurrency = Currency.getInstance("CAD");
		int[] validBanknoteDenominations = {100,15,10,5};
		BigDecimal[] validCoinDenominations = {dec1, dec2, dec3};
		int scaleMaxWeight = 2000;
		int scaleSensitivity = 1;
		SelfCheckoutStation sc = new SelfCheckoutStation(validCurrency, validBanknoteDenominations, validCoinDenominations, scaleMaxWeight, scaleSensitivity);
		
		ChangeReceiveController CRCTest = new ChangeReceiveController(sc);
		CRCTest.PC = new PaymentController(sc);
		CRCTest.PC.setValueOfCart(BigDecimal.valueOf(-1));
		CRCTest.calcChangeDue();
		
//		CRC.PC.setValueOfCart(BigDecimal.valueOf(-1));
//		CRC.calcChangeDue();
		
	}

}
