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

package joachimeichborn.geotag.geocode;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;

/**
 * Interface to geocoding functionality
 * 
 * @author Joachim von Eichborn
 */
public interface Geocoder {
	/**
	 * Query for geocoding information for the given position
	 * @param aPosition
	 * @return One {@link Geocoding} object containing the retrieved information
	 */
	public Geocoding queryPosition(final Coordinates aPosition);
}
