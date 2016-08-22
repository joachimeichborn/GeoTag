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

import org.eclipse.swt.graphics.RGB;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.ui.map.AccuracyPainter;
import joachimeichborn.geotag.ui.map.AccuracyWaypoint;
import joachimeichborn.geotag.ui.map.ColoredImageWaypointRenderer;
import joachimeichborn.geotag.ui.map.RoutePainter;
import joachimeichborn.geotag.ui.parts.MapView;

public class TrackPainterHandler extends AbstractPainterHandler<Track> {
	private static final Logger logger = Logger.getLogger(TrackPainterHandler.class.getSimpleName());
	private static final String TRACK_PLACEMARK = "track_placemark.png";

	private BufferedImage trackPlacemark;

	public TrackPainterHandler(final MapView aMapView) {
		super(aMapView);

		final URL trackPlacemarkResource = ColoredImageWaypointRenderer.class.getResource(TRACK_PLACEMARK);
		if (trackPlacemarkResource != null) {
			try {
				trackPlacemark = ImageIO.read(trackPlacemarkResource);
			} catch (IOException e) {
				logger.severe("Could not load track placemark: " + e.getMessage());
			}
		} else {
			logger.severe("Could not obtain track placemark resource '" + TRACK_PLACEMARK + "'");
		}
	}

	@Override
	void computeContents() {
		painters.clear();
		geoPositions.clear();

		if (mapView.isShowTrackPlacemarks() || mapView.isShowTrackRoute() || mapView.isShowTrackAccuracy()) {
			for (final Track track : selectedItems) {
				final RGB trackColor = track.getColor();
				final Color color = new Color(trackColor.red, trackColor.green, trackColor.blue);

				final List<AccuracyWaypoint> waypoints = computeWaypoints(track);

				if (mapView.isShowTrackAccuracy()) {
					final AccuracyPainter<AccuracyWaypoint> accuracyPainter = new AccuracyPainter<>(color);
					accuracyPainter.setWaypoints(waypoints);
					painters.add(accuracyPainter);
				}

				if (mapView.isShowTrackRoute()) {
					final RoutePainter<Waypoint> routePainter = new RoutePainter<>(color);
					routePainter.setWaypoints(waypoints);
					painters.add(routePainter);
				}

				if (mapView.isShowTrackPlacemarks()) {
					final WaypointPainter<Waypoint> placemarkPainter = new WaypointPainter<>();
					// TODO implement waypointPainter that accepts any
					// collection as input
					placemarkPainter.setWaypoints(new HashSet<>(waypoints));
					placemarkPainter.setRenderer(new ColoredImageWaypointRenderer(trackPlacemark, color));
					painters.add(placemarkPainter);
				}
			}
			
			mapView.setLatestGeoPositions(geoPositions);
		}
	}

	private List<AccuracyWaypoint> computeWaypoints(final Track aTrack) {
		final List<AccuracyWaypoint> waypoints = new LinkedList<>();

		for (final PositionData position : aTrack.getPositions()) {
			final Coordinates coordinates = position.getCoordinates();
			final GeoPosition geoPosition = new GeoPosition(coordinates.getLatitude(), coordinates.getLongitude());
			geoPositions.add(geoPosition);
			waypoints.add(new AccuracyWaypoint(geoPosition, (int) position.getAccuracy()));
		}
		return waypoints;
	}
}
