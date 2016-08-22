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
 * Repo organizing the picture thumbnails.
 * 
 * @author Joachim von Eichborn
 */
public class ThumbnailRepo implements ThumbnailConsumer {
	private static final Logger logger = Logger.getLogger(ThumbnailRepo.class.getSimpleName());
	private static final String THUMBNAIL_PLACEHOLDER_IMAGE = "thumbnail_placeholder.png";
	private static final ThumbnailRepo INSTANCE = new ThumbnailRepo();
	private DatabaseAccess database;

	private Multimap<ThumbnailKey, ThumbnailConsumer> requestedImages;

	BufferedImage placeholder;
	private final ThumbnailCreator thumbnailCreator;

	private ThumbnailRepo() {
		thumbnailCreator = new ThumbnailCreator(this);
		requestedImages = Multimaps.synchronizedMultimap(HashMultimap.create());

		placeholder = null;
		try {
			placeholder = ImageIO.read(ThumbnailRepo.class.getResource(THUMBNAIL_PLACEHOLDER_IMAGE));
		} catch (IOException | IllegalArgumentException e) {
			logger.severe("Could not load thumbnail placeholder: " + e.getMessage());
			placeholder = new BufferedImage(100, 75, BufferedImage.TYPE_INT_RGB);
		}
	}

	public static ThumbnailRepo getInstance() {
		return INSTANCE;
	}

	/**
	 * Request a thumbnail. If it already exists, it is returned immediately.
	 * Otherwise, a placeholder image is returned and the requesting
	 * {@link ThumbnailConsumer} is informed once the actual thumbnail is ready.
	 * 
	 * @param aCacheKey
	 * @param aRotatable
	 * @param aConsumer
	 * @return
	 */
	public BufferedImage getThumbnail(final ThumbnailKey aCacheKey, final boolean aRotatable,
			final ThumbnailConsumer aConsumer) {
		BufferedImage entry = getDatabase().getThumbnail(aCacheKey);

		if (entry != null) {
			logger.fine("Fetched thumbnail using key " + aCacheKey);
			return entry;
		}

		final ThumbnailKey rotatedKey = ThumbnailKey.getRotatedKey(aCacheKey);

		if (aRotatable) {
			entry = getDatabase().getThumbnail(rotatedKey);
			if (entry != null && entry.getWidth() < entry.getHeight()) {
				logger.fine("Fetched thumbnail using rotated key " + rotatedKey);
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
				logger.fine("Requesting thumbnail for " + aCacheKey);
				
				if (!requestedImages.get(aCacheKey).contains(aConsumer)) {
					logger.fine("Adding consumer for " + aCacheKey + ": " + aConsumer);
					requestedImages.put(aCacheKey, aConsumer);
				}

				thumbnailCreator.requestThumbnail(aCacheKey, aRotatable);
			}
		}

		return placeholder;
	}

	/**
	 * Callback that is called once a requested thumbnail is ready. In that
	 * case, all {@link ThumbnailConsumer}s that requested that thumbnail from
	 * the repo are informed that it is ready
	 */
	@Override
	public void thumbnailReady(final ThumbnailKey aKey, final BufferedImage aImage) {
		getDatabase().saveThumbnail(aKey, aImage);

		final Collection<ThumbnailConsumer> consumers;
		synchronized (requestedImages) {
			consumers = requestedImages.removeAll(aKey);
		}

		for (final ThumbnailConsumer consumer : consumers) {
			consumer.thumbnailReady(aKey, aImage);
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
