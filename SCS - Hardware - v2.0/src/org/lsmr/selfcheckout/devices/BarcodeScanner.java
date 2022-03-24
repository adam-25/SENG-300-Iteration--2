package org.lsmr.selfcheckout.devices;

import java.util.Random;

import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.devices.AbstractDevice.Phase;
import org.lsmr.selfcheckout.devices.observers.BarcodeScannerObserver;

/**
 * A complex device hidden behind a simple simulation. They can scan and that is
 * about all.
 */
public class BarcodeScanner extends AbstractDevice<BarcodeScannerObserver> {
	/**
	 * Create a barcode scanner.
	 */
	public BarcodeScanner() {}

	private Random random = new Random();
	private static final int PROBABILITY_OF_FAILED_SCAN = 10; /* out of 100 */

	/**
	 * Simulates the customer's action of scanning an item. The result of the scan
	 * is only announced to any registered observers.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 * 
	 * @param item
	 *            The item to scan. Of course, it will only work if the item has a
	 *            barcode, and maybe not even then.
	 * @throws SimulationException
	 *             If item is null.
	 */
	public void scan(Item item) {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		if(isDisabled())
			return; // silently ignore it

		if(item == null)
			throw new SimulationException(
				new NullPointerException("item is null, which has no analogue in the real world."));

		if(item instanceof BarcodedItem && random.nextInt(100) >= PROBABILITY_OF_FAILED_SCAN)
			notifyBarcodeScanned((BarcodedItem)item);

		// otherwise, silently ignore it
	}

	private void notifyBarcodeScanned(BarcodedItem item) {
		for(BarcodeScannerObserver l : observers)
			l.barcodeScanned(this, item.getBarcode());
	}
}
