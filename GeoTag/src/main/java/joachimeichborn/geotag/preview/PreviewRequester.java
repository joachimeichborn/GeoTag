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

package joachimeichborn.geotag.preview;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import joachimeichborn.geotag.io.database.DatabaseAccess;
import joachimeichborn.geotag.io.database.DatabaseFactory;

/**
 * Checks for a given {@link PreviewKey}, if any preview for that file exist -
 * disregarding it's size. If not, a new preview is requested for that key. This
 * is used to ensure that any previews exist for all pictures that are loaded.
 * This helps avoiding to show the placeholder image because that way always a
 * preview (maybe with an incorrect size) is present
 * 
 * @author Joachim von Eichborn
 */
public class PreviewRequester implements PreviewConsumer {
	private static final Logger logger = Logger.getLogger(PreviewRequester.class.getSimpleName());
	private DatabaseAccess database;

	private final PreviewCreator previewCreator;

	public PreviewRequester() {
		previewCreator = new PreviewCreator(this);
		database = DatabaseFactory.getDatabaseAccess();
	}

	public void triggerPreviewCreation(final PreviewKey aCacheKey) {
		if (database.doesPreviewExist(aCacheKey.getFile())) {
			return;
		}

		logger.fine("Triggering preview creation for " + aCacheKey);
		previewCreator.requestPreview(aCacheKey, true);
	}

	/**
	 * Callback that is called once a requested preview is ready. The created
	 * preview is saved to the database
	 */
	@Override
	public void previewReady(final PreviewKey aKey, final BufferedImage aImage) {
		database.savePreview(aKey, aImage);
	}
}
