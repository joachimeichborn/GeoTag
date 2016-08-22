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

/**
 * An immutable representation of a position.
 * 
 * @author Joachim von Eichborn
 */
public final class Coordinates implements Comparable<Coordinates> {
	private static final double EARTH_RADIUS = 6_371_000;

	private final double latitude;
	private final double longitude;
	private final double altitude;

	/**
	 * @param aLatitude
	 *            The latitude part of the position
	 * @param aLongitude
	 *            The longitude part of the position
	 * @param aAltitude
	 *            The altitude part of the position
	 */
	public Coordinates(final double aLatitude, final double aLongitude, final double aAltitude) {
		latitude = aLatitude;
		longitude = aLongitude;
		altitude = aAltitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Calculate the distance in meters between two positions approximating the
	 * earth as a sphere with radius {@link Coordinates#EARTH_RADIUS}
	 * 
	 * @param aOtherCoordinates
	 *            the other position
	 * @return The distance between the position in meters
	 */
	public double distanceTo(final Coordinates aOtherCoordinates) {
		final double phiRadians = getLatitude() * Math.PI / 180;
		final double otherPhiRadians = aOtherCoordinates.getLatitude() * Math.PI / 180;
		final double deltaPhi = (getLatitude() - aOtherCoordinates.getLatitude()) * Math.PI / 180;
		final double deltaLambda = (getLongitude() - aOtherCoordinates.getLongitude()) * Math.PI / 180;
		final double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) + Math.cos(phiRadians)
				* Math.cos(otherPhiRadians) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS * c;
	}

	@Override
	public String toString() {
		return "Coordinates [latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		long temp;
		temp = Double.doubleToLongBits(altitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Coordinates other = (Coordinates) aObj;
		if (Double.doubleToLongBits(altitude) != Double.doubleToLongBits(other.altitude))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	@Override
	public int compareTo(final Coordinates aOther) {
		if (this == aOther) {
			return 0;
		}
		
		int order = Double.compare(latitude, aOther.getLatitude());
		
		if (order == 0) {
			order = Double.compare(longitude, aOther.getLongitude());
		}
		
		if (order == 0) {
			order = Double.compare(altitude, aOther.getAltitude());
		}
		
		return order;
	}
	
	
}
