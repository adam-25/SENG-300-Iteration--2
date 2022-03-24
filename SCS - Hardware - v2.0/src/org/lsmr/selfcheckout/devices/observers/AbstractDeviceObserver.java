package org.lsmr.selfcheckout.devices.observers;

import org.lsmr.selfcheckout.devices.AbstractDevice;

/**
 * This class represents the abstract interface for all device observers. All
 * subclasses should add their own event notification methods, the first
 * parameter of which should always be the device affected.
 */
public interface AbstractDeviceObserver {
	/**
	 * Announces that the indicated device has been enabled.
	 * 
	 * @param device
	 *                 The device that has been enabled.
	 */
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device);

	/**
	 * Announces that the indicated device has been disabled.
	 * 
	 * @param device
	 *                 The device that has been enabled.
	 */
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device);
}
