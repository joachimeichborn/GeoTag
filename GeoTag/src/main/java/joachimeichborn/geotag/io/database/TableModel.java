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

package joachimeichborn.geotag.io.database;

/**
 * Helper class containing the database model
 * 
 * @author Joachim von Eichborn
 */
public class TableModel {
	private TableModel() {
	}

	public static class Thumbnail {
		public static final String TABLE_NAME = "thumbnail";
		public static final String ID_COLUMN = "id";
		public static final String FILE_NAME_COLUMN = "filename";
		public static final String WIDTH_COLUM = "width";
		public static final String HEIGHT_COLUMN = "height";
		public static final String IMAGE_COLUMN = "image";
	}
}
