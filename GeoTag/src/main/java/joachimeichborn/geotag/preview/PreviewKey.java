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

/**
 * Key that identifies a preview. Used to request previews and to identify existing previews in the repo 
 * 
 * @author Joachim von Eichborn
 */
public class PreviewKey {
	private final String file;
	private final int width;
	private final int height;

	public PreviewKey(final String aFile, final int aWidth, final int aHeight) {
		file = aFile;
		width = aWidth;
		height = aHeight;
	}

	public String getFile() {
		return file;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return "PreviewKey [file=" + file + ", width=" + width + ", height=" + height + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + width;
		result = prime * result + height;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PreviewKey other = (PreviewKey) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (width != other.width)
			return false;
		if (height != other.height)
			return false;
		return true;
	}
	
	public static PreviewKey getRotatedKey(final PreviewKey aKey){
		return new PreviewKey(aKey.getFile(), aKey.getHeight(), aKey.getWidth());
	}
}