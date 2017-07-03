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

package joachimeichborn.geotag.misc;

import java.util.logging.Logger;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Generate a preview image of a given color
 * 
 * @author Joachim von Eichborn
 */
public class ColorPreviewImageGenerator {
	private static final int COLOR_PREVIEW_WIDTH = 20;
	private static final int COLOR_PREVIEW_HEIGHT = 10;
	private static final Logger logger = Logger.getLogger(ColorPreviewImageGenerator.class.getSimpleName());

	private final ImageRegistry registry;
	private final Display display;

	public ColorPreviewImageGenerator(final ImageRegistry aRegistry, final Display aDisplay) {
		registry = aRegistry;
		display = aDisplay;
	}

	/**
	 * @param aColor the color for which a preview is generated
	 * @return an image of defined size with the given color
	 */
	public Image getColorPreview(final RGB aColor) {
		Image image = registry.get(aColor.toString());
		if (image == null) {
			logger.fine("Computing preview for color " + aColor);
			image = new Image(display, COLOR_PREVIEW_WIDTH, COLOR_PREVIEW_HEIGHT);

			final Color color = new Color(display, aColor);
			final GC gc = new GC(image);
			gc.setBackground(color);
			gc.fillRectangle(0, 0, COLOR_PREVIEW_WIDTH, COLOR_PREVIEW_HEIGHT);
			gc.dispose();
			color.dispose();
			registry.put(aColor.toString(), image);
		}
		return image;
	}
}
