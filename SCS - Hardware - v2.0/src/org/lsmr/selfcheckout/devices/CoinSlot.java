package org.lsmr.selfcheckout.devices;

import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.AbstractDevice.Phase;
import org.lsmr.selfcheckout.devices.observers.CoinSlotObserver;

/**
 * Represents a simple coin slot device that has one output channel. The slot is
 * stupid: it has no functionality other than being enabled/disabled, and cannot
 * determine the value and currency of the coin.
 */
public final class CoinSlot extends AbstractDevice<CoinSlotObserver> implements Acceptor<Coin> {
	private UnidirectionalChannel<Coin> sink;

	/**
	 * Creates a coin slot.
	 */
	public CoinSlot() {}

	/**
	 * Connects channels to the coin slot. Causes no events.
	 * <p>
	 * This operation is permissible only during the configuration phase.
	 * 
	 * @param sink
	 *            Where coins will always be passed.
	 */
	public void connect(UnidirectionalChannel<Coin> sink) {
		if(phase != Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may only be called during the configuration phase."));

		this.sink = sink;
	}

	/**
	 * Tells the coin slot that the indicated coin is being inserted. If the slot is
	 * enabled, this causes a "coinInserted" event to be announced to its observers.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 * 
	 * @param coin
	 *            The coin to be added. Cannot be null.
	 * @throws DisabledException
	 *             If the coin slot is currently disabled.
	 * @throws SimulationException
	 *             If coin is null.
	 * @throws NullPointerException
	 *             If the coin is null.
	 */
	public void accept(Coin coin) throws DisabledException {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		if(isDisabled())
			throw new DisabledException();

		if(coin == null)
			throw new SimulationException(new NullPointerException("coin is null"));

		notifyCoinInserted();

		if(sink.hasSpace()) {
			try {
				sink.deliver(coin);
			}
			catch(OverloadException e) {
				// Should never happen
				phase = Phase.ERROR;
				throw new SimulationException(e);
			}
		}
		else
			throw new SimulationException("Unable to route coin: Output channel is full");
	}

	@Override
	public boolean hasSpace() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		return sink.hasSpace();
	}

	private void notifyCoinInserted() {
		for(CoinSlotObserver observer : observers)
			observer.coinInserted(this);
	}
}
