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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;

public class RoutePainter<W extends Waypoint> implements Painter<JXMapViewer> {
	private static final boolean ANTI_ALIAS = true;
	private List<W> waypoints;
	private Color color;

	public RoutePainter(final Color aColor) {
		waypoints = new LinkedList<>();
		color = aColor;
	}

	public void setWaypoints(final List<? extends W> aWaypoints) {
		waypoints.clear();
		waypoints.addAll(aWaypoints);
	}

	@Override
	public void paint(final Graphics2D aGraphics, final JXMapViewer aMap, final int aWidth, final int aHeight) {
		final Graphics2D graphics = (Graphics2D) aGraphics.create();
		final Rectangle viewport = aMap.getViewportBounds();
		graphics.translate(-viewport.x, -viewport.y);

		if (ANTI_ALIAS) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));

		drawRoute(graphics, aMap);

		graphics.dispose();
	}

	private void drawRoute(final Graphics2D aGraphics, final JXMapViewer aMap) {
		boolean first = true;
		int lastX = 0;
		int lastY = 0;

		for (final Waypoint waypoint : waypoints) {
			final Point2D point = aMap.getTileFactory().geoToPixel(waypoint.getPosition(), aMap.getZoom());

			if (!first) {
				aGraphics.drawLine(lastX, lastY, (int) point.getX(), (int) point.getY());
			} else {
				first = false;
			}

			lastX = (int) point.getX();
			lastY = (int) point.getY();
		}
	}
}
