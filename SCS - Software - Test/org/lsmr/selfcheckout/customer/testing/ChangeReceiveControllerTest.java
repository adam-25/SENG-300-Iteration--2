package org.lsmr.selfcheckout.customer.testing;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.BanknoteDispenser;
import org.lsmr.selfcheckout.devices.CoinDispenser;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.EmptyException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.customer.ChangeReceiveController;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.customer.*;

public class ChangeReceiveControllerTest extends BaseTestClass {
	private ChangeReceiveController CRC;

	@Before
	public void setup() {

		// loads in preset data from BaseTestClass
		super.setup();

		// Testing Constructor
		try {
			CRC = new ChangeReceiveController(checkoutStation);
		} catch (SimulationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		int[] validBanknoteDenominations = { 100, 50, 5 };
		BigDecimal[] validCoinDenominations = { dec3, dec2, dec1 };
		int scaleMaxWeight = 2000;
		int scaleSensitivity = 1;
		SelfCheckoutStation sc = new SelfCheckoutStation(validCurrency, validBanknoteDenominations,
				validCoinDenominations, scaleMaxWeight, scaleSensitivity);

		ChangeReceiveController CRCTest = new ChangeReceiveController(sc);
		CRCTest.PC = new PaymentController(sc);
		CRCTest.PC.setValueOfCart(BigDecimal.valueOf(-101.50));

		BanknoteDispenser noteDispenser = null;

		noteDispenser = new BanknoteDispenser(100);

		for (Integer integer : sc.banknoteDispensers.keySet()) {

			noteDispenser = sc.banknoteDispensers.get(integer);

			for (int i = 0; i < 100; i++) {
				Banknote bn = new Banknote(Currency.getInstance("CAD"), integer);

				noteDispenser.load(bn);

			}

		}

		CoinDispenser dispenser = null;

		for (BigDecimal denomination : sc.coinDispensers.keySet()) {
			dispenser = sc.coinDispensers.get(denomination);

			for (int i = 0; i < 100; i++) {
				Coin cn = new Coin(Currency.getInstance("CAD"), denomination);

				dispenser.load(cn);
			}

		}

		CRCTest.calcChangeDue();

	}

	@Test
	public void calcChangeDueChangeLeftTest2() throws EmptyException, DisabledException, OverloadException {
		BigDecimal dec1 = new BigDecimal(0.50);
		BigDecimal dec2 = new BigDecimal(1);
		BigDecimal dec3 = new BigDecimal(2);

		Currency validCurrency = Currency.getInstance("CAD");
		int[] validBanknoteDenominations = { 100, 50, 20, 10, 5 };
		BigDecimal[] validCoinDenominations = { dec3, dec2, dec1 };
		int scaleMaxWeight = 2000;
		int scaleSensitivity = 1;
		SelfCheckoutStation sc = new SelfCheckoutStation(validCurrency, validBanknoteDenominations,
				validCoinDenominations, scaleMaxWeight, scaleSensitivity);

		ChangeReceiveController CRCTest = new ChangeReceiveController(sc);
		CRCTest.PC = new PaymentController(sc);
		CRCTest.PC.setValueOfCart(BigDecimal.valueOf(-20));

		BanknoteDispenser noteDispenser = null;

		noteDispenser = new BanknoteDispenser(100);

		for (Integer integer : sc.banknoteDispensers.keySet()) {
			noteDispenser = sc.banknoteDispensers.get(integer);

			for (int i = 0; i < 100; i++) {
				Banknote bn = new Banknote(Currency.getInstance("CAD"), integer);

				noteDispenser.load(bn);

			}

		}

		CoinDispenser dispenser = null;

		for (BigDecimal denomination : sc.coinDispensers.keySet()) {
			dispenser = sc.coinDispensers.get(denomination);

			for (int i = 0; i < 100; i++) {
				Coin cn = new Coin(Currency.getInstance("CAD"), denomination);

				dispenser.load(cn);
			}

		}

		CRCTest.calcChangeDue();

	}

}
