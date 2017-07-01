/*
GeoTag

Copyright (C) 2015  Joachim von Eichborn

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package joachimeichborn.geotag.model;

import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Immutable representation of a track position
 * 
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals
 * </p>
 * 
 * @author Joachim von Eichborn
 */
public final class PositionData implements Comparable<PositionData> {
	private static final Logger LOGGER = Logger.getLogger(PositionData.class.getSimpleName());
	private static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTimeParser().withOffsetParsed();

	private final Coordinates coordinates;
	private final DateTime timeStamp;
	private final DateTime timeStampWithoutTimeZone;
	private final String name;
	private final float accuracy;

	/**
	 * @param aPosition
	 *            The coordinates
	 * @param aLatitude
	 *            The latitude part of the position
	 * @param aAltitude
	 *            The altitude part of the position
	 * @param aTime
	 *            The time when the position was recorded
	 * @param aName
	 *            The position's name
	 * @param aAccuracy
	 *            The radius in meters of the circle around the recorded
	 *            position that contains the real position with a probability of
	 *            66.6%
	 */
	public PositionData(final Coordinates aPosition, final String aTime, final String aName, final float aAccuracy) {
		this(aPosition, FORMATTER.parseDateTime(aTime), aName, aAccuracy);
		LOGGER.finer("Parsed time information '" + aTime + "' to '" + timeStamp + "' (with time zone)");
	}

	/**
	 * @param aPosition
	 *            The coordinates
	 * @param aTimeStamp
	 *            The time when the position was recorded
	 * @param aName
	 *            The position's name
	 * @param aAccuracy
	 *            The radius in meters of the circle around the recorded
	 *            position that contains the real position with a probability of
	 *            66.6%
	 */
	public PositionData(final Coordinates aPosition, final DateTime aTimeStamp, final String aName,
			final float aAccuracy) {
		coordinates = aPosition;
		timeStamp = aTimeStamp;
		timeStampWithoutTimeZone = stripTimeZoneInformation(timeStamp);
		name = aName;
		accuracy = aAccuracy;
		LOGGER.finer("Parsed time stamp '" + aTimeStamp + "' (with time zone) to '" + timeStampWithoutTimeZone
				+ "' (without time zone)");
	}

	/**
	 * Get a date time object with the same absolute date as the given one but
	 * with the time zone stripped by setting it to UTC
	 * 
	 * @param aTime
	 *            The time in the source time zone
	 * @return The same absolute time (meaning the same date, hour of the day,
	 *         minute of the hour etc.) in UTC time
	 */
	private DateTime stripTimeZoneInformation(final DateTime aTime) {
		return new DateTime(DateTimeZone.UTC)
				.withDate(timeStamp.getYear(), timeStamp.getMonthOfYear(), timeStamp.getDayOfMonth())
				.withTime(timeStamp.getHourOfDay(), timeStamp.getMinuteOfHour(), timeStamp.getSecondOfMinute(), 0);
	}

	/**
	 * @return the coordinates
	 */
	public Coordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * Get the name of this position
	 * 
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the accuracy of this position. That is the radius in meters of the
	 * circle around the recorded position that contains the real position with
	 * a probability of 66.6%
	 * 
	 * @return The accuracy
	 */
	public float getAccuracy() {
		return accuracy;
	}

	/**
	 * Get the time when the position was recorded as a {@link DateTime} object
	 * 
	 * @return The time when the position was recorded
	 */
	public DateTime getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Get the time when the position was recorded as a {@link DateTime} object
	 * but with stripped time zone information. The time zone information is
	 * stripped by setting it to UTC
	 * 
	 * @return The time when the photo was taken without time zone information.
	 *         This is the same absolute time (meaning the same date, hour of
	 *         the day, minute of the hour etc.) as returned by
	 *         {@link #getTimeStamp()} in UTC time
	 */
	public DateTime getTimeStampWithoutTimeZone() {
		return timeStampWithoutTimeZone;
	}

	@Override
	public String toString() {
		return "PositionData [coordinates=" + coordinates + ", timeStamp=" + timeStamp + ", timeStampWithoutTimeZone="
				+ timeStampWithoutTimeZone + ", name=" + name + ", accuracy=" + accuracy + "]";
	}

	/**
	 * Comparison is done solely based on the time stamps, the position is not
	 * taken into account.
	 * 
	 * Note: Because of this, the natural ordering of this class is inconsistent
	 * with equals
	 */
	@Override
	public int compareTo(final PositionData aOtherPosition) {
		return timeStamp.compareTo(aOtherPosition.getTimeStamp());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + Float.floatToIntBits(accuracy);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object aObj) {
		if (this == aObj)
			return true;
		if (aObj == null)
			return false;
		if (getClass() != aObj.getClass())
			return false;
		PositionData other = (PositionData) aObj;
		if (Float.floatToIntBits(accuracy) != Float.floatToIntBits(other.accuracy))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.isEqual(other.timeStamp))
			return false;
		return true;
	}
}
