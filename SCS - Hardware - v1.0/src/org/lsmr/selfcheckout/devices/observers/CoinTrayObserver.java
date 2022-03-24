package org.lsmr.selfcheckout.devices.observers;

import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.CoinTray;

/**
 * Observes events emanating from a coin tray. Coin trays are dumb devices so
 * very few kinds of events can be announced by them.
 */
public interface CoinTrayObserver extends AbstractDeviceObserver {
	/**
	 * Announces that a coin has been added to the indicated tray.
	 * 
	 * @param tray
	 *            The tray where the event occurred.
	 */
	void coinAdded(CoinTray tray);
}
