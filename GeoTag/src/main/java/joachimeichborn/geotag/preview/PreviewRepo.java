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
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import joachimeichborn.geotag.io.database.DatabaseAccess;
import joachimeichborn.geotag.io.database.DatabaseFactory;

/**
 * Repo organizing the picture previews.
 * 
 * @author Joachim von Eichborn
 */
public class PreviewRepo implements PreviewConsumer {
	private static final Logger logger = Logger.getLogger(PreviewRepo.class.getSimpleName());
	private static final String PREVIEW_PLACEHOLDER_IMAGE = "preview_placeholder.png";
	private static final PreviewRepo INSTANCE = new PreviewRepo();
	private DatabaseAccess database;

	private Multimap<PreviewKey, PreviewConsumer> requestedImages;

	BufferedImage placeholder;
	private final PreviewCreator previewCreator;

	private PreviewRepo() {
		previewCreator = new PreviewCreator(this);
		requestedImages = Multimaps.synchronizedMultimap(HashMultimap.create());

		placeholder = null;
		try {
			placeholder = ImageIO.read(PreviewRepo.class.getResource(PREVIEW_PLACEHOLDER_IMAGE));
		} catch (IOException | IllegalArgumentException e) {
			logger.severe("Could not load preview placeholder: " + e.getMessage());
			placeholder = new BufferedImage(100, 75, BufferedImage.TYPE_INT_RGB);
		}
	}

	public static PreviewRepo getInstance() {
		return INSTANCE;
	}

	/**
	 * Request a preview. If it already exists, it is returned immediately.
	 * Otherwise, a placeholder image is returned and the requesting
	 * {@link PreviewConsumer} is informed once the actual preview is ready.
	 * 
	 * @param aCacheKey
	 * @param aRotatable
	 * @param aConsumer
	 * @return
	 */
	public BufferedImage getPreview(final PreviewKey aCacheKey, final boolean aRotatable,
			final PreviewConsumer aConsumer) {
		BufferedImage entry = getDatabase().getPreview(aCacheKey);

		if (entry != null) {
			logger.fine("Fetched preview using key " + aCacheKey);
			return entry;
		}

		final PreviewKey rotatedKey = PreviewKey.getRotatedKey(aCacheKey);

		if (aRotatable) {
			entry = getDatabase().getPreview(rotatedKey);
			if (entry != null && entry.getWidth() < entry.getHeight()) {
				logger.fine("Fetched preview using rotated key " + rotatedKey);
				return entry;
			}
		}

		synchronized (requestedImages) {
			if (requestedImages.containsKey(aCacheKey)) {
				if (!requestedImages.get(aCacheKey).contains(aConsumer)) {
					logger.fine("Adding consumer for key " + aCacheKey + ": " + aConsumer);
					requestedImages.put(aCacheKey, aConsumer);
				}
			} else if (aRotatable && requestedImages.containsKey(rotatedKey)) {
				if (!requestedImages.get(rotatedKey).contains(aConsumer)) {
					logger.fine("Adding consumer for rotated key" + rotatedKey + ": " + aConsumer);
					requestedImages.put(rotatedKey, aConsumer);
				}
			} else {
				logger.fine("Requesting preview for " + aCacheKey);
				
				if (!requestedImages.get(aCacheKey).contains(aConsumer)) {
					logger.fine("Adding consumer for " + aCacheKey + ": " + aConsumer);
					requestedImages.put(aCacheKey, aConsumer);
				}

				previewCreator.requestPreview(aCacheKey, aRotatable);
			}
		}

		return placeholder;
	}

	/**
	 * Callback that is called once a requested preview is ready. In that
	 * case, all {@link PreviewConsumer}s that requested that preview from
	 * the repo are informed that it is ready
	 */
	@Override
	public void previewReady(final PreviewKey aKey, final BufferedImage aImage) {
		getDatabase().savePreview(aKey, aImage);

		final Collection<PreviewConsumer> consumers;
		synchronized (requestedImages) {
			consumers = requestedImages.removeAll(aKey);
		}

		for (final PreviewConsumer consumer : consumers) {
			consumer.previewReady(aKey, aImage);
		}
	}

	private DatabaseAccess getDatabase() {
		if (database == null) {
			database = DatabaseFactory.getDatabaseAccess();
		}

		return database;
	}

	/**
	 * Hack to mock database access as long as dependency injection is not used
	 * 
	 * @param aDatabaseAccess
	 */
	void setDatabase(final DatabaseAccess aDatabaseAccess) {
		database = aDatabaseAccess;
	}
}
