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

package joachimeichborn.geotag.ui.map.painterhandler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.swt.widgets.Display;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.preview.PreviewConsumer;
import joachimeichborn.geotag.preview.PreviewKey;
import joachimeichborn.geotag.preview.PreviewRepo;
import joachimeichborn.geotag.ui.map.ImageWaypointRenderer;
import joachimeichborn.geotag.ui.parts.MapView;
import joachimeichborn.geotag.ui.preferences.MapPreferences;

public class PicturePainterHandler extends AbstractPainterHandler<Picture>implements PreviewConsumer {
	private class ThumbnailUpdater implements Runnable {
		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					computeContents();
					mapView.repaint();
				}
			});
		}
	}

	private static final Logger logger = Logger.getLogger(PicturePainterHandler.class.getSimpleName());
	private static final String PICTURE_PLACEMARK = "picture_placemark.png";
	private static final float DIMENSION_FACTOR = 0.75f;
	private static final int DIRECT_RERENDER_THRESHOLD = 30;

	private final Set<String> requestedImages = Collections.synchronizedSet(new HashSet<>());
	private final PreviewRepo previewRepo;
	private ImageWaypointRenderer imageRenderer;
	private ScheduledExecutorService thumbnailUpdateExecutor;
	private ScheduledFuture<?> thumbnailUpdater;

	@Inject
	@Preference(nodePath = LifeCycleManager.PREFERENCES_NODE, value = MapPreferences.THUMBNAIL_SIZE)
	private int longerDimension;

	public PicturePainterHandler(final MapView aMapView) {
		super(aMapView);

		previewRepo = PreviewRepo.getInstance();
		thumbnailUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
		thumbnailUpdater = thumbnailUpdateExecutor.schedule(new ThumbnailUpdater(), 0, TimeUnit.MILLISECONDS);

		final URL picturePlacemarkResource = ImageWaypointRenderer.class.getResource(PICTURE_PLACEMARK);
		if (picturePlacemarkResource != null) {
			try {
				final BufferedImage picturePlacemark = ImageIO.read(picturePlacemarkResource);
				imageRenderer = new ImageWaypointRenderer(picturePlacemark);
			} catch (IOException e) {
				logger.severe("Could not load picture placemark: " + e.getMessage());
			}
		} else {
			logger.severe("Could not obtain picture placemark resource '" + PICTURE_PLACEMARK + "'");
		}
	}

	@Override
	void computeContents() {
		if (thumbnailUpdater.getDelay(TimeUnit.MILLISECONDS) > 0) {
			thumbnailUpdater.cancel(false);
		}
		
		painters.clear();
		geoPositions.clear();
		requestedImages.clear();

		if (mapView.isShowPicturePlacemarks()) {
			if (mapView.isShowPictureThumbnails()) {
				final int shorterDimension = (int) (DIMENSION_FACTOR * longerDimension);
				for (final Picture picture : selectedItems) {
					final Coordinates coordinates = picture.getCoordinates();

					if (coordinates != null) {
						final PreviewKey key = new PreviewKey(picture.getFile().toString(), longerDimension,
								shorterDimension);
						final BufferedImage thumbnail = previewRepo.getPreview(key, true, this);
						synchronized (requestedImages) {
							requestedImages.add(key.getFile());
						}

						if (thumbnail != null) {
							final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
							final GeoPosition geoPosition = new GeoPosition(coordinates.getLatitude(),
									coordinates.getLongitude());
							geoPositions.add(geoPosition);

							waypointPainter.setWaypoints(Collections.singleton(new DefaultWaypoint(geoPosition)));
							waypointPainter.setRenderer(new ImageWaypointRenderer(thumbnail));
							painters.add(waypointPainter);
						}
					}
				}
			} else {
				final Set<Waypoint> waypoints = new HashSet<>();
				final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
				for (final Picture picture : selectedItems) {
					final Coordinates coordinates = picture.getCoordinates();

					if (coordinates != null) {
						final GeoPosition geoPosition = new GeoPosition(coordinates.getLatitude(),
								coordinates.getLongitude());
						geoPositions.add(geoPosition);
						waypoints.add(new DefaultWaypoint(geoPosition));
					}
				}
				waypointPainter.setWaypoints(waypoints);
				waypointPainter.setRenderer(imageRenderer);
				painters.add(waypointPainter);
			}
			
			mapView.setLatestGeoPositions(geoPositions);
		}
	}

	@Override
	public void previewReady(PreviewKey aKey, BufferedImage aImage) {
		boolean correctImage = false;
		synchronized (requestedImages) {
			final boolean current = requestedImages.remove(aKey.getFile());

			// check whether the image that was computed is still of use
			if (current) {
				if (Math.max(aImage.getWidth(), aImage.getHeight()) == (int) (DIMENSION_FACTOR * longerDimension)) {
					previewRepo.getPreview(aKey, true, this);
					requestedImages.add(aKey.getFile());
				} else {
					correctImage = true;
				}
			}
		}

		if (correctImage) {
			requestRepaint();
		}
	}

	public void requestRepaint() {
		final int delay = selectedItems.size() > DIRECT_RERENDER_THRESHOLD ? 3_000 : 0;
		if (thumbnailUpdater.getDelay(TimeUnit.MILLISECONDS) > delay
				|| thumbnailUpdater.getDelay(TimeUnit.MILLISECONDS) < 0) {
			thumbnailUpdater.cancel(false);
			thumbnailUpdater = thumbnailUpdateExecutor.schedule(new ThumbnailUpdater(), delay, TimeUnit.MILLISECONDS);
		}
	}
}
