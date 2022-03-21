package org.lsmr.selfcheckout.devices;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.AbstractDevice.Phase;
import org.lsmr.selfcheckout.devices.observers.CoinDispenserObserver;

/**
 * Represents a device that stores coins of a particular denomination to
 * dispense them as change.
 * <p>
 * Coin dispensers can receive coins from other sources. To simplify the
 * simulation, no check is performed on the value of each coin, meaning it is an
 * external responsibility to ensure the correct routing of coins.
 * </p>
 */
public final class CoinDispenser extends AbstractDevice<CoinDispenserObserver>
	implements Acceptor<Coin>, FromStorageEmitter<Coin> {
	private int maxCapacity;
	private Queue<Coin> queue = new LinkedList<Coin>();
	private UnidirectionalChannel<Coin> sink;

	/**
	 * Creates a coin dispenser with the indicated maximum capacity.
	 * 
	 * @param capacity
	 *            The maximum number of coins that can be stored in the dispenser.
	 *            Must be positive.
	 * @throws SimulationException
	 *             if capacity is not positive.
	 */
	public CoinDispenser(int capacity) {
		if(capacity <= 0)
			throw new SimulationException(new IllegalArgumentException("Capacity must be positive: " + capacity));

		this.maxCapacity = capacity;
	}

	/**
	 * Accesses the current number of coins in the dispenser.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @return The number of coins currently in the dispenser.
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * Allows a set of coins to be loaded into the dispenser directly. Existing
	 * coins in the dispenser are not removed. Causes a "coinsLoaded" event to be
	 * announced.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @param coins
	 *            A sequence of coins to be added. Each cannot be null.
	 * @throws OverloadException
	 *             if the number of coins to be loaded exceeds the capacity of the
	 *             dispenser.
	 * @throws SimulationException
	 *             If any coin is null.
	 */
	public void load(Coin... coins) throws SimulationException, OverloadException {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		if(maxCapacity < queue.size() + coins.length)
			throw new OverloadException("Capacity of dispenser is exceeded by load");

		for(Coin coin : coins)
			if(coin == null)
				throw new SimulationException(new NullPointerException("A coin is null"));
			else
				queue.add(coin);

		notifyLoad(coins);
	}

	private void notifyLoad(Coin[] coins) {
		for(CoinDispenserObserver observer : observers)
			observer.coinsLoaded(this, coins);
	}

	/**
	 * Unloads coins from the dispenser directly. Causes a "coinsUnloaded" event to
	 * be announced.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @return A list of the coins unloaded. May be empty. Will never be null.
	 */
	public List<Coin> unload() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		List<Coin> result = new ArrayList<>(queue);
		queue.clear();

		notifyUnload(result.toArray(new Coin[result.size()]));

		return result;
	}

	private void notifyUnload(Coin[] coins) {
		for(CoinDispenserObserver observer : observers)
			observer.coinsUnloaded(this, coins);
	}

	/**
	 * Connects an output channel to this coin dispenser. Any existing output
	 * channels are disconnected. Causes no events to be announced.
	 * <p>
	 * This operation is only permissible during the configuration phase.
	 * 
	 * @param sink
	 *            The new output device to act as output. Can be null, which leaves
	 *            the channel without an output.
	 */
	public void connect(UnidirectionalChannel<Coin> sink) {
		if(phase != Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may only be called during the configuration phase."));

		this.sink = sink;
	}

	/**
	 * Returns the maximum capacity of this coin dispenser.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @return The capacity. Will be positive.
	 */
	public int getCapacity() {
		return maxCapacity;
	}

	/**
	 * Causes the indicated coin to be added into the dispenser. If successful, a
	 * "coinAdded" event is announced to its observers. If a successful coin
	 * addition causes the dispenser to become full, a "coinsFull" event is
	 * announced to its observers.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 * 
	 * @throws DisabledException
	 *             If the coin dispenser is currently disabled.
	 * @throws SimulationException
	 *             If coin is null.
	 * @throws OverloadException
	 *             If the coin dispenser is already full.
	 */
	@Override
	public void accept(Coin coin) throws OverloadException, DisabledException {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		if(isDisabled())
			throw new DisabledException();

		if(coin == null)
			throw new SimulationException(
				new NullPointerException("coin is null, which has no analogue in the real world."));

		if(queue.size() >= maxCapacity)
			throw new OverloadException();

		queue.add(coin);
		notifyCoinAdded(coin);

		if(queue.size() >= maxCapacity)
			notifyCoinsFull();
	}

	/**
	 * Releases a single coin from this coin dispenser. If successful, a
	 * "coinRemoved" event is announced to its observers. If a successful coin
	 * removal causes the dispenser to become empty, a "coinsEmpty" event is
	 * announced to its observers.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 * 
	 * @throws OverloadException
	 *             If the output channel is unable to accept another coin.
	 * @throws EmptyException
	 *             If no coins are present in the dispenser to release.
	 * @throws DisabledException
	 *             If the dispenser is currently disabled.
	 */
	public void emit() throws OverloadException, EmptyException, DisabledException {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		if(isDisabled())
			throw new DisabledException();

		if(queue.size() == 0)
			throw new EmptyException();

		Coin coin = queue.remove();

		notifyCoinRemoved(coin);
		sink.deliver(coin);

		if(queue.isEmpty())
			notifyCoinsEmpty();
	}

	/**
	 * Returns whether this coin dispenser has enough space to accept at least one
	 * more coin. Announces no events.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 */
	@Override
	public boolean hasSpace() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		return queue.size() < maxCapacity;
	}

	private void notifyCoinAdded(Coin coin) {
		for(CoinDispenserObserver observer : observers)
			observer.coinAdded(this, coin);
	}

	private void notifyCoinRemoved(Coin coin) {
		for(CoinDispenserObserver observer : observers)
			observer.coinRemoved(this, coin);
	}

	private void notifyCoinsFull() {
		for(CoinDispenserObserver observer : observers)
			observer.coinsFull(this);
	}

	private void notifyCoinsEmpty() {
		for(CoinDispenserObserver observer : observers)
			observer.coinsEmpty(this);
	}
}
