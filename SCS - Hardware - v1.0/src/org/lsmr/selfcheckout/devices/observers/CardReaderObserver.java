package org.lsmr.selfcheckout.devices.observers;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.CardReader;

/**
 * Observes events emanating from a coin dispenser.
 */
public interface CardReaderObserver extends AbstractDeviceObserver {
	/**
	 * Announces that a card has been inserted in the indicated card reader.
	 * 
	 * @param reader
	 *            The reader where the event occurred.
	 */
	void cardInserted(CardReader reader);

	/**
	 * Announces that a card has been removed from the indicated card reader.
	 * 
	 * @param reader
	 *            The reader where the event occurred.
	 */
	void cardRemoved(CardReader reader);

	/**
	 * Announces that a (tap-enabled) card has been tapped on the indicated card
	 * reader.
	 * 
	 * @param reader
	 *            The reader where the event occurred.
	 */
	void cardTapped(CardReader reader);

	/**
	 * Announces that a card has swiped on the indicated card reader.
	 * 
	 * @param reader
	 *            The reader where the event occurred.
	 */
	void cardSwiped(CardReader reader);

	/**
	 * Announces that the data has been read from a card.
	 * 
	 * @param reader
	 *            The reader where the event occurred.
	 * @param data
	 *            The data that was read. Note that this data may be corrupted.
	 */
	void cardDataRead(CardReader reader, CardData data);
}
