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

package joachimeichborn.geotag.ui.map;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

public class ImageWaypointRenderer implements WaypointRenderer<Waypoint> {
	private BufferedImage waypointImage;

	public ImageWaypointRenderer(final BufferedImage aImage) {
		waypointImage = aImage;
	}

	@Override
	public void paintWaypoint(final Graphics2D aGraphics, final JXMapViewer aMap, final Waypoint aWaypoint) {
		if (waypointImage != null) {
			final Point2D point = aMap.getTileFactory().geoToPixel(aWaypoint.getPosition(), aMap.getZoom());
			final int x = (int) point.getX() - waypointImage.getWidth() / 2;
			final int y = (int) point.getY() - waypointImage.getHeight();

			aGraphics.drawImage(waypointImage, x, y, null);
		}
	}
}
