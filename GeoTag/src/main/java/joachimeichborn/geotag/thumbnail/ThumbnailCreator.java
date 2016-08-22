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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jxmapviewer.util.GraphicsUtilities;

import joachimeichborn.geotag.io.jpeg.PictureMetadataReader;
import joachimeichborn.geotag.utils.LifoBlockingDeque;
import joachimeichborn.geotag.utils.PictureOrientation;

/**
 * Create requested thumbnails
 * 
 * @author Joachim von Eichborn
 */
public class ThumbnailCreator {
	/**
	 * Worker class that computes thumbnails. When a new thumbnail is ready, the
	 * {@link ThumbnailConsumer} is informed about it
	 */
	private static class Worker implements Runnable {
		private final ThumbnailKey cacheKey;
		private final boolean rotatable;
		private final ThumbnailConsumer thumbnailConsumer;

		/**
		 * @param aCacheKey
		 * @param aRotatable
		 * @param aThumbnailConsumer
		 *            the thumbnail consumer that is informed when the thumbnail
		 *            is ready
		 */
		public Worker(final ThumbnailKey aCacheKey, final boolean aRotatable,
				final ThumbnailConsumer aThumbnailConsumer) {
			cacheKey = aCacheKey;
			rotatable = aRotatable;
			thumbnailConsumer = aThumbnailConsumer;
		}

		/**
		 * Compute a thumbnail. When it is ready, the {@link ThumbnailConsumer}
		 * is informed about it
		 */
		@Override
		public void run() {
			final Path pictureFile = Paths.get(cacheKey.getFile());
			final PictureMetadataReader metadataExtractor = new PictureMetadataReader(pictureFile);

			final BufferedImage original = getOriginalImage(pictureFile, metadataExtractor);
			if (original == null) {
				return;
			}

			final PictureOrientation orientation = metadataExtractor.getOrientation();
			final Dimension scaledDim = getScaledDimension(original.getWidth(), original.getHeight(), orientation);

			final BufferedImage thumbnail = createThumbnail(original, scaledDim.getWidth() - 2 * BORDER_SIZE,
					scaledDim.getHeight() - 2 * BORDER_SIZE);

			final Dimension rotatedDim;
			if (orientation.isHorizontal()) {
				rotatedDim = new Dimension(scaledDim.getWidth(), scaledDim.getHeight());
			} else {
				rotatedDim = new Dimension(scaledDim.getHeight(), scaledDim.getWidth());
			}

			final BufferedImage borderedThumbnail = GraphicsUtilities.createCompatibleImage(thumbnail,
					rotatedDim.getWidth(), rotatedDim.getHeight());
			final Graphics2D g = borderedThumbnail.createGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, rotatedDim.getWidth(), rotatedDim.getHeight());

			switch (orientation) {
			case ROTATE_90_CW:
				g.translate(scaledDim.getHeight() / 2, scaledDim.getWidth() / 2);
				g.rotate(Math.toRadians(270));
				g.translate(-rotatedDim.getHeight() / 2, -rotatedDim.getWidth() / 2);
				break;
			case ROTATE_180:
				g.rotate(Math.toRadians(180), scaledDim.getWidth() / 2, scaledDim.getHeight() / 2);
				break;
			case ROTATE_270_CW:
				g.translate(scaledDim.getHeight() / 2, scaledDim.getWidth() / 2);
				g.rotate(Math.toRadians(90));
				g.translate(-(rotatedDim.getHeight()) / 2, -(rotatedDim.getWidth()) / 2);
				break;
			default:
				break;
			}

			g.drawImage(thumbnail, BORDER_SIZE, BORDER_SIZE, scaledDim.getWidth() - BORDER_SIZE,
					scaledDim.getHeight() - BORDER_SIZE, 0, 0, scaledDim.getWidth() - 2 * BORDER_SIZE,
					scaledDim.getHeight() - 2 * BORDER_SIZE, Color.BLACK, null);
			g.dispose();

			logger.fine("Finished computing thumbnail for " + cacheKey);
			thumbnailConsumer.thumbnailReady(cacheKey, borderedThumbnail);
		}

		private Dimension getScaledDimension(final int aOriginalWidth, final int aOriginalHeight,
				final PictureOrientation aOrientation) {
			final int originalWidth;
			final int originalHeight;
			if (aOrientation.isHorizontal()) {
				originalWidth = aOriginalWidth;
				originalHeight = aOriginalHeight;
			} else {
				originalWidth = aOriginalHeight;
				originalHeight = aOriginalWidth;
			}

			final int width;
			final int height;
			if (rotatable && originalHeight > originalWidth && cacheKey.getWidth() > cacheKey.getHeight()) {
				width = cacheKey.getHeight();
				height = cacheKey.getWidth();
			} else {
				width = cacheKey.getWidth();
				height = cacheKey.getHeight();
			}

			final float widthFactor = width / (float) originalWidth;
			final float heightFactor = height / (float) originalHeight;

			final float factor = Math.min(widthFactor, heightFactor);

			final int scaledWidth = (int) (aOriginalWidth * factor);
			final int scaledHeight = (int) (aOriginalHeight * factor);

			return new Dimension(scaledWidth, scaledHeight);
		}

		private BufferedImage getOriginalImage(final Path aPictureFile,
				final PictureMetadataReader aMetadataExtractor) {
			final BufferedImage exivThumbnail = aMetadataExtractor.getThumbnail();

			if (exivThumbnail != null) {
				final int exivHeight;
				final int exivWidth;
				if (aMetadataExtractor.getOrientation().isHorizontal()) {
					exivHeight = exivThumbnail.getHeight();
					exivWidth = exivThumbnail.getWidth();
				} else {
					exivHeight = exivThumbnail.getWidth();
					exivWidth = exivThumbnail.getHeight();
				}

				final int height = cacheKey.getHeight();
				final int width = cacheKey.getWidth();

				if ((!rotatable && (exivHeight >= height || exivWidth >= width))
						|| (rotatable && (Math.max(exivHeight, exivWidth) >= Math.max(height, width)))) {
					logger.fine("Using exiv thumbnail image to compute thumbnail");
					return exivThumbnail;
				}
			}

			try {
				logger.fine("Using original image to compute thumbnail");
				return ImageIO.read(aPictureFile.toFile());
			} catch (

			IOException e) {
				logger.warning("Could not read " + aPictureFile + " for thumbnail generation: " + e.getMessage());
				return null;
			}
		}

		private BufferedImage createThumbnail(final BufferedImage aImage, final int aWidth, int aHeight) {
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
	}

	private static class Dimension {
		private int width;
		private int height;

		public Dimension(final int aWidth, final int aHeight) {
			width = aWidth;
			height = aHeight;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		@Override
		public String toString() {
			return "Dimension [width=" + width + ", height=" + height + "]";
		}
	}

	private static final int BORDER_SIZE = 1;
	private static final Logger logger = Logger.getLogger(ThumbnailCreator.class.getSimpleName());

	private final ExecutorService threadPool;
	final ThumbnailConsumer mThumbnailConsumer;

	/**
	 * @param aThumbnailConsumer
	 *            The thumbnail consumer that is informed when a thumbnail is
	 *            ready
	 */
	public ThumbnailCreator(final ThumbnailConsumer aThumbnailConsumer) {
		mThumbnailConsumer = aThumbnailConsumer;
		int cores = Runtime.getRuntime().availableProcessors();
		logger.fine("Using " + cores + " cores for thumbnail computation");
		threadPool = new ThreadPoolExecutor(cores, cores, 0L, TimeUnit.MILLISECONDS, new LifoBlockingDeque<Runnable>());
	}

	/**
	 * Request a thumbnail. The actual computation is done using multithreaded
	 * worker thread. Once a thumbnail is finished, the
	 * {@link ThumbnailConsumer} is informed about it
	 * 
	 * @param aKey
	 * @param aRotatable
	 */
	public void requestThumbnail(final ThumbnailKey aKey, final boolean aRotatable) {
		threadPool.execute(new Worker(aKey, aRotatable, mThumbnailConsumer));
	}
}
