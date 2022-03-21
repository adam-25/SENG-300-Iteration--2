package org.lsmr.selfcheckout.devices;

import java.util.Arrays;
import java.util.List;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.devices.AbstractDevice.Phase;
import org.lsmr.selfcheckout.devices.observers.BanknoteStorageUnitObserver;

/**
 * Represents devices that store banknotes. They only receive banknotes, not
 * dispense them. To access the banknotes inside, a human operator needs to
 * physically remove the banknotes, simulated with the {@link #unload()} method.
 * A {@link #load(Banknote...)} method is provided for symmetry.
 */
public class BanknoteStorageUnit extends AbstractDevice<BanknoteStorageUnitObserver> implements Acceptor<Banknote> {
	private Banknote[] storage;
	private int nextIndex = 0;

	/**
	 * Creates a banknote storage unit that can hold the indicated number of
	 * banknotes.
	 * 
	 * @param capacity
	 *            The maximum number of banknotes that the unit can hold.
	 * @throws SimulationException
	 *             If the capacity is not positive.
	 */
	public BanknoteStorageUnit(int capacity) {
		if(capacity <= 0)
			throw new SimulationException(new IllegalArgumentException("The capacity must be positive."));

		storage = new Banknote[capacity];
	}

	/**
	 * Gets the maximum number of banknotes that this storage unit can hold.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @return The capacity.
	 */
	public int getCapacity() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		return storage.length;
	}

	/**
	 * Gets the current count of banknotes contained in this storage unit.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @return The current count.
	 */
	public int getBanknoteCount() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		return nextIndex;
	}

	/**
	 * Allows a set of banknotes to be loaded into the storage unit directly.
	 * Existing banknotes in the dispenser are not removed. Causes a
	 * "banknotesLoaded" event to be announced. Disabling has no effect on
	 * loading/unloading.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @param banknotes
	 *            A sequence of banknotes to be added. Each cannot be null.
	 * @throws SimulationException
	 *             if the number of banknotes to be loaded exceeds the capacity of
	 *             the unit.
	 * @throws SimulationException
	 *             If the banknotes argument is null.
	 * @throws SimulationException
	 *             If any banknote is null.
	 * @throws OverloadException
	 *             If too many banknotes are stuffed in the unit.
	 */
	public void load(Banknote... banknotes) throws SimulationException, OverloadException {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		if(banknotes == null)
			throw new SimulationException(
				new NullPointerException("banknotes is null which has no analogue in the real world"));

		if(banknotes.length + nextIndex > storage.length)
			throw new OverloadException("You tried to stuff too many banknotes in the storage unit.");

		for(Banknote banknote : banknotes)
			if(banknote == null)
				throw new SimulationException(
					new NullPointerException("No banknote may be null, which has no analogue in the real world."));

		System.arraycopy(banknotes, 0, storage, nextIndex, banknotes.length);
		nextIndex += banknotes.length;

		notifyBanknotesLoaded();
	}

	/**
	 * Unloads banknotes from the storage unit directly. Causes a
	 * "banknotesUnloaded" event to be announced.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @return A list of the banknotes unloaded. May be empty. Will never be null.
	 */
	public List<Banknote> unload() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		List<Banknote> banknotes = Arrays.asList(storage);

		storage = new Banknote[storage.length];
		nextIndex = 0;
		notifyBanknotesUnloaded();

		return banknotes;
	}

	/**
	 * Causes the indicated banknote to be added to the storage unit. If successful,
	 * a "banknoteAdded" event is announced to its observers. If a successful
	 * banknote addition causes the unit to become full, a "banknotesFull" event is
	 * instead announced to its observers.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 * 
	 * @param banknote
	 *            The banknote to add.
	 * @throws DisabledException
	 *             If the unit is currently disabled.
	 * @throws SimulationException
	 *             If banknote is null.
	 * @throws OverloadException
	 *             If the unit is already full.
	 */
	public void accept(Banknote banknote) throws DisabledException, OverloadException {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		if(isDisabled())
			throw new DisabledException();

		if(banknote == null)
			throw new SimulationException(
				new NullPointerException("banknote is null, which has no analogue in the real world."));

		if(nextIndex < storage.length) {
			storage[nextIndex++] = banknote;

			if(nextIndex == storage.length)
				notifyBanknotesFull();
			else
				notifyBanknoteAdded();
		}
		else
			throw new OverloadException();
	}

	@Override
	public boolean hasSpace() {
		return nextIndex < storage.length;
	}

	private void notifyBanknotesLoaded() {
		for(BanknoteStorageUnitObserver l : observers)
			l.banknotesLoaded(this);
	}

	private void notifyBanknotesUnloaded() {
		for(BanknoteStorageUnitObserver l : observers)
			l.banknotesUnloaded(this);
	}

	private void notifyBanknotesFull() {
		for(BanknoteStorageUnitObserver l : observers)
			l.banknotesFull(this);
	}

	private void notifyBanknoteAdded() {
		for(BanknoteStorageUnitObserver l : observers)
			l.banknoteAdded(this);
	}
}
