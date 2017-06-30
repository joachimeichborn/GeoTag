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

package joachimeichborn.geotag.io.database;

import java.awt.image.BufferedImage;

import joachimeichborn.geotag.thumbnail.ThumbnailKey;

/**
 * Interface for database access methods
 * 
 * @author Joachim von Eichborn
 */
public interface DatabaseAccess {
	/**
	 * @param aKey
	 * @param aThumbnail
	 */
	void saveThumbnail(final ThumbnailKey aKey, final BufferedImage aThumbnail);

	/**
	 * @param aKey
	 * @return the thumbnail identified by the specified key or
	 *         <code>null</code> if no entry exists for that key
	 */
	BufferedImage getThumbnail(final ThumbnailKey aKey);

	/**
	 * Trim the database to contain at most the specified number of entries
	 * @param aMaxNumberEntries
	 */
	void trim(final int aMaxNumberEntries);

	/**
	 * Close the database
	 */
	void close();
}