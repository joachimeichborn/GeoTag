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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

/**
 * Generate colors. The color set is predefined and colors are returned in a defined order. Thus it is guaranteed that consecutive colors will be different, but the colors will repeat themselves after a while
 * 
 * @author Joachim von Eichborn
 */
public class ColorGenerator {
	private static final ColorGenerator INSTANCE = new ColorGenerator();

	private final List<RGB> colors;
	private int index = 0;

	private ColorGenerator() {
		colors = new ArrayList<>();
		colors.add(new RGB(0, 0, 255));
		colors.add(new RGB(255, 0, 0));
		colors.add(new RGB(0, 255, 0));
		colors.add(new RGB(128, 128, 128));
		colors.add(new RGB(255, 255, 0));
		colors.add(new RGB(0, 255, 255));
		colors.add(new RGB(255, 0, 255));
		colors.add(new RGB(255, 200, 0));
		colors.add(new RGB(255, 175, 175));
		colors.add(new RGB(0, 0, 0));
	}

	/**
	 * @return the {@link ColorGenerator} singleton
	 */
	public static ColorGenerator getInstance() {
		return INSTANCE;
	}

	/**
	 * @return the next color
	 */
	public RGB getNextColor() {
		if (index >= colors.size()) {
			index = 0;
		}

		return colors.get(index++);
	}
}
