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

package joachimeichborn.geotag.thumbnail;

import java.awt.image.BufferedImage;

/**
 * Interface to be implemented by classes that use thumbnails. The only function is called once a thumbnail is ready
 * 
 * @author Joachim von Eichborn
 */
public interface ThumbnailConsumer {
	/**
	 * Called when a thumbnail is ready
	 * @param aKey The key that was used to request the thumbnail
	 * @param aImage the created thumbnail
	 */
	public void thumbnailReady(final ThumbnailKey aKey, final BufferedImage aImage);
}
