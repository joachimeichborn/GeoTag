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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

public class ColoredImageWaypointRenderer implements WaypointRenderer<Waypoint> {
	private ImageWaypointRenderer renderer;

	public ColoredImageWaypointRenderer(final BufferedImage aImage, final Color aColor) {
		final BufferedImage coloredImage=new BufferedImage(aImage.getWidth(), aImage.getHeight(), aImage.getType());
		
		final int white = Color.white.getRGB();
		final int replacementColor = aColor.getRGB();

		for (int x = 0; x < aImage.getWidth(); x++) {
			for (int y = 0; y < aImage.getHeight(); y++) {
				final int originalColor = aImage.getRGB(x, y);
				coloredImage.setRGB(x, y, originalColor == white? replacementColor: originalColor);
			}
		}
		
		renderer = new ImageWaypointRenderer(coloredImage);
	}

	@Override
	public void paintWaypoint(final Graphics2D aGraphics, final JXMapViewer aMap, final Waypoint aWaypoint) {
		renderer.paintWaypoint(aGraphics, aMap, aWaypoint);
	}
}
