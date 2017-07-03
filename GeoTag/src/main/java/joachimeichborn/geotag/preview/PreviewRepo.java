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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.jxmapviewer.util.GraphicsUtilities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import joachimeichborn.geotag.io.database.DatabaseAccess;

/**
 * Repo organizing the picture previews.
 * 
 * @author Joachim von Eichborn
 */
@Creatable
@Singleton
public class PreviewRepo implements PreviewConsumer {
	private static final Logger LOGGER = Logger.getLogger(PreviewRepo.class.getSimpleName());
	private static final String PREVIEW_PLACEHOLDER_IMAGE = "preview_placeholder.png";
	
	private final DatabaseAccess dbAccess;
	private final PreviewCreator previewCreator;

	private Multimap<PreviewKey, PreviewConsumer> requestedImages;
	final BufferedImage placeholder;

	@Inject
	public PreviewRepo(final DatabaseAccess aDbAccess) {
		LOGGER.fine("Constructing preview repo");
		
		dbAccess = aDbAccess;
		requestedImages = Multimaps.synchronizedMultimap(HashMultimap.create());
		previewCreator = new PreviewCreator(this);

		BufferedImage placeholderTmp = null;
		try {
			placeholderTmp = ImageIO.read(PreviewRepo.class.getResource(PREVIEW_PLACEHOLDER_IMAGE));
		} catch (IOException | IllegalArgumentException e) {
			LOGGER.severe("Could not load preview placeholder: " + e.getMessage());
			placeholderTmp = new BufferedImage(100, 75, BufferedImage.TYPE_INT_RGB);
		}
		placeholder = placeholderTmp;
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
	public BufferedImage getPreview(final PreviewKey aCacheKey, final boolean aRotatable, final PreviewConsumer aConsumer) {
		BufferedImage entry = dbAccess.getPreview(aCacheKey);

		if (entry != null) {
			LOGGER.fine("Fetched preview using key " + aCacheKey);
			return entry;
		}

		final PreviewKey rotatedKey = PreviewKey.getRotatedKey(aCacheKey);

		if (aRotatable) {
			entry = dbAccess.getPreview(rotatedKey);
			if (entry != null && entry.getWidth() < entry.getHeight()) {
				LOGGER.fine("Fetched preview using rotated key " + rotatedKey);
				return entry;
			}
		}

		synchronized (requestedImages) {
			if (requestedImages.containsKey(aCacheKey)) {
				if (!requestedImages.get(aCacheKey).contains(aConsumer)) {
					LOGGER.fine("Adding consumer for key " + aCacheKey + ": " + aConsumer);
					requestedImages.put(aCacheKey, aConsumer);
				}
			} else if (aRotatable && requestedImages.containsKey(rotatedKey)) {
				if (!requestedImages.get(rotatedKey).contains(aConsumer)) {
					LOGGER.fine("Adding consumer for rotated key" + rotatedKey + ": " + aConsumer);
					requestedImages.put(rotatedKey, aConsumer);
				}
			} else {
				LOGGER.fine("Adding consumer for " + aCacheKey + ": " + aConsumer);
				requestedImages.put(aCacheKey, aConsumer);

				LOGGER.fine("Requesting preview for " + aCacheKey);
				previewCreator.requestPreview(aCacheKey, aRotatable);
			}
		}

		entry = dbAccess.getPreviewAnySize(aCacheKey.getFile());
		if (entry != null) {
			final float widthFactor;
			final float heightFactor;
			if (aRotatable && entry.getHeight() > entry.getWidth()) {
				widthFactor = aCacheKey.getHeight() / (float) entry.getWidth();
				heightFactor = aCacheKey.getWidth() / (float) entry.getHeight();
			} else {
				widthFactor = aCacheKey.getWidth() / (float) entry.getWidth();
				heightFactor = aCacheKey.getHeight() / (float) entry.getHeight();
			}

			final float factor = Math.min(widthFactor, heightFactor);

			LOGGER.fine("Fetched preview with incorrect size using file" + aCacheKey.getFile());
			return createPreview(entry, (int) (entry.getWidth() * factor), (int) (entry.getHeight() * factor));
		}

		return placeholder;
	}

	private BufferedImage createPreview(final BufferedImage aImage, final int aWidth, int aHeight) {
		final BufferedImage temp = GraphicsUtilities.createCompatibleImage(aImage, aWidth, aHeight);
		final Graphics2D g2 = temp.createGraphics();

		try {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(aImage, 0, 0, temp.getWidth(), temp.getHeight(), null);
		} finally {
			g2.dispose();
		}

		return temp;

	}

	/**
	 * Callback that is called once a requested preview is ready. The preview is
	 * saved to the database and all {@link PreviewConsumer}s that requested
	 * that preview from the repo are informed that it is ready
	 */
	@Override
	public void previewReady(final PreviewKey aKey, final BufferedImage aImage) {
		dbAccess.savePreview(aKey, aImage);

		final Collection<PreviewConsumer> consumers;
		synchronized (requestedImages) {
			consumers = requestedImages.removeAll(aKey);
		}

		for (final PreviewConsumer consumer : consumers) {
			consumer.previewReady(aKey, aImage);
		}
	}
}
