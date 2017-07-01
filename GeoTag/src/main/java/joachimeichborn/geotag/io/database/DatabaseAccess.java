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

import joachimeichborn.geotag.preview.PreviewKey;

/**
 * Interface for database access methods
 * 
 * @author Joachim von Eichborn
 */
public interface DatabaseAccess {
	/**
	 * @param aKey
	 * @param aPreview
	 */
	void savePreview(final PreviewKey aKey, final BufferedImage aPreview);

	/**
	 * @param aKey
	 * @return the preview identified by the specified key or
	 *         <code>null</code> if no entry exists for that key
	 */
	BufferedImage getPreview(final PreviewKey aKey);

	/**
	 * @param aFile
	 * @return a preview for the given file, no information about the desired size is given
	 *         <code>null</code> if no entry exists for that file
	 *         */
	BufferedImage getPreviewAnySize(final String aFile);
	
	/**
	 * @param aFile
	 * @return true if a preview of any size exists for the given file
	 */
	boolean doesPreviewExist(final String aFile);
	
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
