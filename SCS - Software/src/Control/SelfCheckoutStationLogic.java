package Control;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BanknoteValidator;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.CoinValidator;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.BarcodeScannerObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;
import org.lsmr.selfcheckout.devices.observers.TouchScreenObserver;

/**
 * Represents the logic of a Self-Checkout Station.
 * 
 * @author Fu-Yin Lin
 * @author Ryan McHale
 * @author Karim Kassouri
 * @author Munhib Saad
 * @author Parker Wieck
 * @author Muhammad Ali
 * @author Ayomide Alabi
 *
 */
public class SelfCheckoutStationLogic implements BarcodeScannerObserver, ElectronicScaleObserver, TouchScreenObserver,
		CoinValidatorObserver, BanknoteValidatorObserver {
	private SelfCheckoutStation selfCheckoutStation;
	private Map<Barcode, Map<String, String>> barcodeList;
	private Map<Barcode, Integer> scannedItemList = new HashMap<>();
	private double totalCost;
	private double totalPayment;
	private double currentExpectedWeight;
	private boolean waitForAttendant = false;
	private boolean checkingOut = false;

	/**
	 * Basic constructor.
	 * 
	 * @param scs   The self-checkout station to install the logic. Cannot be null.
	 * @param store The store to provide store product information. Cannot be null.
	 */
	public SelfCheckoutStationLogic(SelfCheckoutStation scs, Store store) {
		if (scs == null || store == null) {
			throw new SimulationException(new NullPointerException());
		}
		selfCheckoutStation = scs;
		scs.scanner.attach(this);
		scs.scale.attach(this);
		scs.coinValidator.attach(this);
		scs.banknoteValidator.attach(this);
		barcodeList = store.getBarcodeList();
		totalCost = 0.0;
		totalPayment = 0.0;
		currentExpectedWeight = 0.0;
	}


	
	
	
	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// not used
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// not used
	}

	@Override
	public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
		if (barcode == null) {
			throw new SimulationException("undefined barcode");
		}

		if (scannedItemList.containsKey(barcode)) {
			scannedItemList.replace(barcode, scannedItemList.get(barcode) + 1);
		} else {
			scannedItemList.put(barcode, 1);
		}

		totalCost += Double.parseDouble(barcodeList.get(barcode).get("price"));
		currentExpectedWeight = Double.parseDouble(barcodeList.get(barcode).get("weight"));
	}

	@Override
	public void overload(ElectronicScale scale) {
		waitForAttendant = true;
	}

	@Override
	public void outOfOverload(ElectronicScale scale) {
		waitForAttendant = false;
	}

	@Override
	public void weightChanged(ElectronicScale scale, double weightInGrams) {
		if (weightInGrams > 0) {
			double difference = Math.abs(currentExpectedWeight - weightInGrams);
			if (difference <= selfCheckoutStation.scale.getSensitivity()) {
				// Good to proceed further actions
				waitForAttendant = false;
			} else {
				// Call attendant for further investigation
				waitForAttendant = true;
			}
		} else {
			// Call attendant to figure out what gets removed
			waitForAttendant = true;
		}
	}

	/**
	 * Method deals with button pressed on the touch screen (simulation only)
	 */
	/*
	 * 
	 
	 Would be implemented with the appropriate hardware.
	 We are missing hardware to accept crypto, credit, and any interface with the touchscreen.
	 
	public void touchScreenButtonPressed(TouchScreenObserver touchscreen, String buttonPressed) {
		// button = 'payment'
		// buttons = 'cash', 'card', 'crypto'
		// if 'cash', coin slot and banknote slot should be enabled
		// if 'card', card reader should be enabled
		// if 'crypto', not sure which hardware to use
		// if button = 'pay' (in cash case) is enabled (default disabled), that means
		// payment >= cost
		// if payment == cost, call printReceipt()
		// else payment > cost, call returnChange() and printReceipt()
				
		if (buttonPressed == "payment") {
			checkingOut = true;
		}
		
		if (buttonPressed == "cash") {
			// coin and banknote slots enabled
		}
		
		if (buttonPressed == "card") {
			// card reader enabled
		}
		
		if (buttonPressed == "crypto") {
			// cryto payment machine enabled
		}
		
		if (buttonPressed == "cancel") {
			checkingOut = false;
		}
		
		if (buttonPressed == "pay") {
			if (totalPayment > totalCost) {
				//returnChange(); not yet implemented
			} 
			printReceipt();
			checkingOut = false;
		}
	}
	*/


	@Override
	public void validCoinDetected(CoinValidator validator, BigDecimal value) {
		totalPayment += value.doubleValue();
		if (totalPayment >= totalCost) {
			// 'pay' button enabled
		}
	}

	@Override
	public void invalidCoinDetected(CoinValidator validator) {
		// System block and attendant notified
		System.out.println("INVALID COIN DETECTED");
		waitForAttendant = true;
	}

	@Override
	public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
		totalPayment += value;
		if (totalPayment >= totalCost) {
			// 'pay' button enabled
		}
	}

	@Override
	public void invalidBanknoteDetected(BanknoteValidator validator) {
		// System block and attendant notified
		System.out.println("INVALID BANKNOTE DETECTED");
		waitForAttendant = true;
	}

	/**
	 * Method to calculate and return change to customer
	 
	public void returnChange() {
		// to be implemented in future iteration
	}
	*/

	/**
	 * Method to generate and print receipt
	 */
	public void printReceipt() {
		StringBuilder str = new StringBuilder();
		for (Barcode barcode : scannedItemList.keySet()) {
			int quantity = scannedItemList.get(barcode);
			double price = Double.parseDouble(barcodeList.get(barcode).get("price"));
			str.append(barcode);
			str.append(" ");
			str.append(barcodeList.get(barcode).get("name"));
			str.append(" ");
			str.append(quantity);
			str.append(" ");
			str.append(price);
			str.append(" ");
			str.append(quantity * price);
		}

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			selfCheckoutStation.printer.print(c);
		}
	}

	/**
	 * Accesses the list of scanned items.
	 * 
	 * @return A Map with current scanned barcode and corresponding quantity.
	 
	public Map<Barcode, Integer> getScannedItemList() {
		return scannedItemList;
	}
	 */
	
	/**
	 * Accesses the current total cost.
	 * 
	 * @return The current total cost of all scanned items.
	 */
	public double getTotalCost() {
		return totalCost;
	}
	/**
	 * Accesses the current total payment.
	 * 
	 * @return The current total ammount paid.
	 */
	public double getTotalPayment() {
		return totalPayment;
	}
	/**
	 * Accesses whether or not we should wait for attendant.
	 * 
	 * @return Whether or not we should wait for attendant.
	 */
	public boolean getWaitForAttendant() {
		return waitForAttendant;
	}
	
	/**
	 * Accesses the current expected weight.
	 * 
	 * @return The current expected weight of recently scanned items.
	 */
	public double getTotalExpectedWeight() {
		return currentExpectedWeight;
	}

	/**
	 * Accesses the current list information of barcoded items.
	 * 
	 * @return A Map with all barcoded items in store and their corresponding info.
	 */
	public Map<Barcode, Map<String, String>> getBarcodeList() {
		return barcodeList;
	}
	
	
}
