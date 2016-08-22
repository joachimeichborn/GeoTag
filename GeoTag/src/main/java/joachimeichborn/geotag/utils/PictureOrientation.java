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

package joachimeichborn.geotag.utils;

import java.util.logging.Logger;

/**
 * Enum containing all possible picture orientations that may be set in picture meta data
 * 
 * @author Joachim von Eichborn
 */
public enum PictureOrientation {
	HORIZONTAL_NORMAL(1, true), //
	MIRROR_HORIZONTAL(2, true), //
	ROTATE_180(3, true), //
	MIRROR_VERTICAL(4, true), //
	MIRROR_HORIZONTAL_AND_ROTATE_270_CW(5, false), //
	ROTATE_270_CW(6, false), //
	MIRROR_HORIZONTAL_AND_ROTATE_90_CW(7, false), //
	ROTATE_90_CW(8, false);

	private static final Logger logger = Logger.getLogger(PictureOrientation.class.getSimpleName());

	private int metadataValue;
	private boolean horizontal;

	private PictureOrientation(final int aMetadataValue, final boolean aHorizontal) {
		metadataValue = aMetadataValue;
		horizontal = aHorizontal;
	}

	/**
	 * @return true if the picture is horizontal, false otherwise 
	 */
	public boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * @param aMetadataValue the orientation as given in the meta data
	 * @return the orientation matching the given meta data value. If the meta data value cannot be matched, the orientation is assumed to be a normal horizontal image
	 */
	public static PictureOrientation getByMetadataValue(final int aMetadataValue) {
		for (final PictureOrientation orientation : values()) {
			if (orientation.metadataValue == aMetadataValue) {
				return orientation;
			}
		}

		logger.fine("Could not find an orientation matching metadata value {}, defaulting to horizontal orientation");
		return HORIZONTAL_NORMAL;
	}
}
