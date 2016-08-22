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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.ui.map.AccuracyPainter;
import joachimeichborn.geotag.ui.map.AccuracyWaypoint;
import joachimeichborn.geotag.ui.map.ColoredImageWaypointRenderer;
import joachimeichborn.geotag.ui.parts.MapView;

public class PositionPainterHandler extends AbstractPainterHandler<PositionData> {
	private static final Logger logger = Logger.getLogger(PositionPainterHandler.class.getSimpleName());
	private static final String POSITION_PLACEMARK = "position_placemark.png";
	private static final Color positionColor = new Color(238, 40, 29);

	private ColoredImageWaypointRenderer positionRenderer;

	public PositionPainterHandler(final MapView aMapView) {
		super(aMapView);

		final URL positionPlacemarkResource = ColoredImageWaypointRenderer.class.getResource(POSITION_PLACEMARK);
		if (positionPlacemarkResource != null) {
			try {
				final BufferedImage positionPlacemark = ImageIO.read(positionPlacemarkResource);
				positionRenderer = new ColoredImageWaypointRenderer(positionPlacemark, positionColor);
			} catch (IOException e) {
				logger.severe("Could not load position placemark: " + e.getMessage());
			}
		} else {
			logger.severe("Could not obtain position placemark resource '" + POSITION_PLACEMARK + "'");
		}
	}

	@Override
	void computeContents() {
		painters.clear();
		geoPositions.clear();

		if (mapView.isShowPositions()) {
			final List<AccuracyWaypoint> waypoints = new LinkedList<>();

			for (final PositionData position : selectedItems) {
				final Coordinates coordinates = position.getCoordinates();
				final GeoPosition geoPosition = new GeoPosition(coordinates.getLatitude(), coordinates.getLongitude());
				geoPositions.add(geoPosition);
				waypoints.add(new AccuracyWaypoint(geoPosition, (int) position.getAccuracy()));
			}

			final AccuracyPainter<AccuracyWaypoint> accuracyPainter = new AccuracyPainter<>(positionColor);
			accuracyPainter.setWaypoints(waypoints);
			painters.add(accuracyPainter);

			final WaypointPainter<Waypoint> placemarkPainter = new WaypointPainter<>();
			// TODO implement waypointPainter that accepts any
			// collection as input
			placemarkPainter.setWaypoints(new HashSet<>(waypoints));
			placemarkPainter.setRenderer(positionRenderer);
			painters.add(placemarkPainter);
			
			mapView.setLatestGeoPositions(geoPositions);
		}
	}
}
