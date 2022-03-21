package org.lsmr.selfcheckout.devices.observers;

import java.math.BigDecimal;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.CoinValidator;

/**
 * Observes events emanating from a coin validator.
 */
public interface CoinValidatorObserver extends AbstractDeviceObserver {
	/**
	 * An event announcing that the indicated coin has been detected and determined
	 * to be valid.
	 * 
	 * @param validator
	 *            The device on which the event occurred.
	 * @param value
	 *            The value of the coin.
	 */
	void validCoinDetected(CoinValidator validator, BigDecimal value);

	/**
	 * An event announcing that a coin has been detected and determined to be
	 * invalid.
	 * 
	 * @param validator
	 *            The device on which the event occurred.
	 */
	void invalidCoinDetected(CoinValidator validator);
}
