package org.lsmr.selfcheckout.devices;

import java.util.ArrayList;
import java.util.Random;

import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.devices.AbstractDevice.Phase;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;

public class ElectronicScale extends AbstractDevice<ElectronicScaleObserver> {
	private ArrayList<Item> items = new ArrayList<>();

	private double weightLimitInGrams;
	private double currentWeightInGrams = 0;
	private double weightAtLastEvent = 0;
	private double sensitivity;

	/**
	 * Constructs an electronic scale with the indicated maximum weight that it can
	 * handle before going into overload. The constructed scale will initially be in
	 * the configuration phase.
	 * 
	 * @param weightLimitInGrams
	 *            The weight threshold beyond which the scale will overload.
	 * @param sensitivity
	 *            The number of grams that can be added or removed since the last
	 *            change event, without causing a new change event.
	 * @throws SimulationException
	 *             If either argument is &le;0.
	 */
	public ElectronicScale(int weightLimitInGrams, int sensitivity) {
		if(weightLimitInGrams <= 0)
			throw new SimulationException("The maximum weight cannot be zero or less.");

		if(sensitivity <= 0)
			throw new SimulationException("The sensitivity cannot be zero or less.");

		this.weightLimitInGrams = weightLimitInGrams;
		this.sensitivity = sensitivity;
	}

	/**
	 * Gets the weight limit for the scale. Weights greater than this will not be
	 * weighable by the scale, but will cause overload.
	 * <p>
	 * This operation is permissible during all phases.
	 * 
	 * @return The weight limit.
	 */
	public double getWeightLimit() {
		return weightLimitInGrams;
	}

	/**
	 * Gets the current weight on the scale.
	 * <p>
	 * This operation is not permissible during the configuration or error phases.
	 * 
	 * @return The current weight.
	 * @throws SimulationException
	 *             If this operation is called during the configuration or error
	 *             phases.
	 * @throws OverloadException
	 *             If the weight has overloaded the scale.
	 */
	public double getCurrentWeight() throws OverloadException {
		if(phase == Phase.ERROR)
			throw new SimulationException(
				"This method may not be used when the device is in an erroneous operation phase.");
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException("This method may not be called during the configuration phase.");

		if(currentWeightInGrams <= weightLimitInGrams)
			return currentWeightInGrams + new Random().nextDouble() / 10.0;

		throw new OverloadException();
	}

	/**
	 * Gets the sensitivity of the scale. Changes smaller than this limit are not
	 * noticed or announced.
	 * <p>
	 * This operation is permissible during all phases.
	 * 
	 * @return The sensitivity.
	 */
	public double getSensitivity() {
		return sensitivity;
	}

	/**
	 * Adds an item to the scale. If the addition is successful, a weight changed
	 * event is announced. If the weight is greater than the weight limit, an
	 * overload event is announced.
	 * <p>
	 * This operation is not permissible during the configuration or error phase.
	 * 
	 * @param item
	 *            The item to add.
	 * @throws SimulationException
	 *             If the same item is added more than once or is null.
	 * @throws SimulationException
	 *             If this operation is called during the configuration or error
	 *             phases.
	 */
	public void add(Item item) {
		if(phase == Phase.ERROR)
			throw new SimulationException(
				"This method may not be used when the device is in an erroneous operation phase.");
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException("This method may not be called during the configuration phase.");

		if(item == null)
			throw new SimulationException("Null is not a valid item.");

		if(items.contains(item))
			throw new SimulationException("The same item cannot be added more than once to the scale.");
		
		currentWeightInGrams += item.getWeight();

		items.add(item);

		if(currentWeightInGrams > weightLimitInGrams)
			notifyOverload();

		if(Math.abs(currentWeightInGrams - weightAtLastEvent) > sensitivity)
			notifyWeightChanged();
	}

	/**
	 * Removes an item from the scale. If the operation is successful, a weight
	 * changed event is announced. If the scale was overloaded and this removal
	 * causes it to no longer be overloaded, an out of overload event is announced.
	 * <p>
	 * This operation is not permissible during the configuration or error phases.
	 * 
	 * @param item
	 *            The item to remove.
	 * @throws SimulationException
	 *             If the item is not on the scale (including if it is null).
	 * @throws SimulationException
	 *             If this operation is called during the configuration or error
	 *             phases.
	 */
	public void remove(Item item) {
		if(phase == Phase.ERROR)
			throw new SimulationException(
				"This method may not be used when the device is in an erroneous operation phase.");
		if(phase == Phase.CONFIGURATION)
			throw new SimulationException("This method may not be called during the configuration phase.");

		if(!items.remove(item))
			throw new SimulationException("The item was not found amongst those on the scale.");

		// To avoid drift in the sum due to round-off error, recalculate the weight.
		double newWeightInGrams = 0.0;
		for(Item itemOnScale : items)
			newWeightInGrams += itemOnScale.getWeight();

		double original = currentWeightInGrams;
		currentWeightInGrams = newWeightInGrams;

		if(original > weightLimitInGrams && newWeightInGrams <= weightLimitInGrams)
			notifyOutOfOverload();

		if(currentWeightInGrams <= weightLimitInGrams && Math.abs(original - currentWeightInGrams) > sensitivity)
			notifyWeightChanged();
	}

	private void notifyOverload() {
		for(ElectronicScaleObserver l : observers)
			l.overload(this);
	}

	private void notifyOutOfOverload() {
		weightAtLastEvent = currentWeightInGrams;

		for(ElectronicScaleObserver l : observers)
			l.outOfOverload(this);
	}

	private void notifyWeightChanged() {
		weightAtLastEvent = currentWeightInGrams;

		for(ElectronicScaleObserver l : observers)
			l.weightChanged(this, currentWeightInGrams);
	}
}
