package org.lsmr.selfcheckout;

import java.math.BigDecimal;
import java.util.Currency;

import org.lsmr.selfcheckout.devices.SimulationException;

/**
 * Instances of this class represent individual coins.
 */
public class Coin {
	private BigDecimal value;
	private Currency currency;

	/**
	 * Rather than specifying a currency for every coin, a default currency can be
	 * specified which will be used when the currency is not specified.
	 */
	public static Currency DEFAULT_CURRENCY;

	/**
	 * Constructs a coin, using the default currency.
	 * 
	 * @param value
	 *            The value of the coin, in multiples of the unit of currency.
	 * @throws SimulationException
	 *             If the value is &le;0.
	 * @throws SimulationException
	 *             If the argument is null.
	 */
	public Coin(BigDecimal value) {
		if(value == null)
			throw new SimulationException(new NullPointerException("value is null"));

		if(DEFAULT_CURRENCY == null)
			throw new SimulationException(new NullPointerException("Default currency is null"));

		if(value.compareTo(BigDecimal.ZERO) <= 0)
			throw new SimulationException(
				new IllegalArgumentException("The value must be greater than 0: the argument passed was " + value));

		this.value = value;
		this.currency = DEFAULT_CURRENCY;
	}
		
	/**
	 * Constructs a coin.
	 * 
	 * @param currency
	 *            The currency represented by this coin.
	 * @param value
	 *            The value of the coin, in multiples of the unit of currency.
	 * @throws SimulationException
	 *             If the value is &le;0.
	 * @throws SimulationException
	 *             If either argument is null.
	 */
	public Coin(Currency currency, BigDecimal value) {
		if(value == null)
			throw new SimulationException(new NullPointerException("value is null"));

		if(currency == null)
			throw new SimulationException(new NullPointerException("currency is null"));

		if(value.compareTo(BigDecimal.ZERO) <= 0)
			throw new SimulationException(
				new IllegalArgumentException("The value must be greater than 0: the argument passed was " + value));

		this.value = value;
		this.currency = currency;
	}

	/**
	 * Accessor for the value.
	 * 
	 * @return The value of the coin. Should always be greater than 0.
	 */
	public BigDecimal getValue() {
		return value;
	}

	/**
	 * Accessor for the currency.
	 * 
	 * @return The currency for this coin. Note that this is not the same as the
	 *             "denomination" (e.g., a Canadian dime is worth 0.1 Canadian
	 *             dollars, so a Canadian dime would have currency "Canadian
	 *             dollars").
	 */
	public Currency getCurrency() {
		return currency;
	}

	@Override
	public String toString() {
		return value.toString() + " " + currency;
	}
}
