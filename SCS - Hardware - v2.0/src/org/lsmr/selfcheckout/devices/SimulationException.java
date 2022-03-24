package org.lsmr.selfcheckout.devices;

/**
 * An exception that can be raised when the behaviour within the simulator makes
 * no sense, typically when it has not been configured correctly. This is
 * different from an exception being raised because the preconditions of a
 * component are violated, but that would make sense in the real world.
 */
@SuppressWarnings("serial")
public class SimulationException extends RuntimeException {
	private String nested;

	/**
	 * Constructor used to nest other exceptions.
	 * 
	 * @param nested
	 *            An underlying exception that is to be wrapped.
	 */
	public SimulationException(Exception nested) {
		this.nested = nested.toString();
	}

	/**
	 * Basic constructor.
	 * 
	 * @param message
	 *            An explanatory message of the problem.
	 */
	public SimulationException(String message) {
		nested = message;
	}
}
