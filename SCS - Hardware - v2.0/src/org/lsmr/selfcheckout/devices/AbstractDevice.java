package org.lsmr.selfcheckout.devices;

import java.util.ArrayList;

import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;

/**
 * The abstract base class for all devices involved in the simulator.
 * <p>
 * This class utilizes the Observer design pattern. Subclasses inherit the
 * attach method, but each must define its own notifyXXX methods.
 * </p>
 * <p>
 * Each device must be coupled to an appropriate observer interface, which
 * extends AbstractDeviceObserver; the type parameter T represents this
 * observer.
 * <p>
 * <p>
 * Any individual device can be disabled, which means it will not permit
 * physical movements to be caused by the software. Any method that could cause
 * a physical movement will declare that it throws DisabledException.
 * </p>
 * 
 * @param <T>
 *            The type of observers used for this device. For a device whose
 *            class is X, its corresponding observer interface would typically
 *            be XObserver.
 */
public abstract class AbstractDevice<T extends AbstractDeviceObserver> {
	/**
	 * Used to represent the operation phase of this device.
	 */
	protected enum Phase {
		/**
		 * The device is being configured.
		 */
		CONFIGURATION,
		/**
		 * The device is in normal operation.
		 */
		NORMAL,
		/**
		 * The device has undergone a fatal error requiring physical repair and reset.
		 */
		ERROR
	}

	/**
	 * The current operation phase of this device.
	 */
	protected Phase phase = Phase.CONFIGURATION;

	/**
	 * Many devices require configuration before their use. Before configuration is
	 * complete, the device should not operate. Once configuration is complete, any
	 * further configuration attempts should cause exceptions.
	 */
	public void endConfigurationPhase() {
		phase = Phase.NORMAL;
	}

	/**
	 * For testing purposes only. Forces this device into an erroneous state.
	 */
	public void forceErrorPhase() {
		phase = Phase.ERROR;
	}

	/**
	 * A list of the registered observers on this device.
	 */
	protected ArrayList<T> observers = new ArrayList<>();

	/**
	 * Locates the indicated observer and removes it such that it will no longer be
	 * informed of events from this device. If the observer is not currently
	 * registered with this device, calls to this method will return false, but
	 * otherwise have no effect.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @param observer
	 *            The observer to remove.
	 * @return true if the observer was found and removed, false otherwise.
	 */
	public final boolean detach(T observer) {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		return observers.remove(observer);
	}

	/**
	 * All observers registered with this device are removed. If there are none,
	 * calls to this method have no effect.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 */
	public final void detachAll() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		observers.clear();
	}

	/**
	 * Registers the indicated observer to receive event notifications from this
	 * device.
	 * <p>
	 * This operation is permissible during the configuration phase.
	 * 
	 * @param observer
	 *            The observer to be added.
	 */
	public final void attach(T observer) {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));

		if(observer == null)
			throw new SimulationException(new IllegalArgumentException(
				"This method may not receive null, as it has no analogue in the real world."));

		observers.add(observer);
	}

	private boolean disabled = false;

	/**
	 * Disables this device from receiving input and producing output.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 */
	public final void disable() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		disabled = true;
		notifyDisabled();
	}

	private void notifyDisabled() {
		for(T observer : observers)
			observer.disabled(this);
	}

	/**
	 * Enables this device for receiving input and producing output.
	 * <p>
	 * This operation is not permissible during the configuration phase.
	 */
	public final void enable() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		disabled = false;
		notifyEnabled();
	}

	private void notifyEnabled() {
		for(T observer : observers)
			observer.enabled(this);
	}

	/**
	 * Returns whether this device is currently disabled from receiving input and
	 * producing output.
	 * <p>
	 * This operation is illegal during the configuration phase.
	 * 
	 * @return true if the device is disabled; false if the device is enabled.
	 */
	public final boolean isDisabled() {
		if(phase == Phase.ERROR)
			throw new SimulationException(new IllegalStateException(
				"This method may not be used when the device is in an erroneous operation phase."));
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException(
				new IllegalStateException("This method may not be called during the configuration phase."));

		return disabled;
	}
}
