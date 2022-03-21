package org.lsmr.selfcheckout.devices.observers;

import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.CoinStorageUnit;

/**
 * Observes events emanating from a coin storage unit.
 */
public interface CoinStorageUnitObserver extends AbstractDeviceObserver {
	/**
	 * Announces that the indicated coin storage unit is full of coins.
	 * 
	 * @param unit
	 *            The storage unit where the event occurred.
	 */
	void coinsFull(CoinStorageUnit unit);

	/**
	 * Announces that a coin has been added to the indicated storage unit.
	 * 
	 * @param unit
	 *            The storage unit where the event occurred.
	 */
	void coinAdded(CoinStorageUnit unit);

	/**
	 * Announces that the indicated storage unit has been loaded with coins.
	 * Used to simulate direct, physical loading of the unit.
	 * 
	 * @param unit
	 *            The storage unit where the event occurred.
	 */
	void coinsLoaded(CoinStorageUnit unit);

	/**
	 * Announces that the storage unit has been emptied of coins. Used to
	 * simulate direct, physical unloading of the unit.
	 * 
	 * @param unit
	 *            The storage unit where the event occurred.
	 */
	void coinsUnloaded(CoinStorageUnit unit);
}
