package org.lsmr.selfcheckout.devices.observers;

import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.CoinSlot;

/**
 * Observes events emanating from a coin slot.
 */
public interface CoinSlotObserver extends AbstractDeviceObserver {
	/**
	 * An event announcing that a coin has been inserted.
	 * 
	 * @param slot
	 *             The device on which the event occurred.
	 */
	void coinInserted(CoinSlot slot);
}
