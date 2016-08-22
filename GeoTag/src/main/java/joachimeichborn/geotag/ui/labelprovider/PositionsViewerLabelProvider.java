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

package joachimeichborn.geotag.ui.labelprovider;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;

public class PositionsViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
	public static final String NAME_COLUMN = "Name";
	public static final String ACCURACY_COLUMN = "Accuracy";
	public static final String TIMESTAMP_COLUMN = "Time";
	public static final String COORDINATES_COLUMN = "Coordinates";
	private static final DateTimeFormatter timestampFormatter = DateTimeFormat.forPattern("dd.MM.yy HH:mm:ss ZZ");
	
	private final int nameColumnIndex;
	private final int accuracyColumnIndex;
	private final int timestampColumnIndex;
	private final int coordinatesColumnIndex;

	public PositionsViewerLabelProvider(final List<String> aColumns) {
		nameColumnIndex = aColumns.indexOf(NAME_COLUMN);
		accuracyColumnIndex = aColumns.indexOf(ACCURACY_COLUMN);
		timestampColumnIndex = aColumns.indexOf(TIMESTAMP_COLUMN);
		coordinatesColumnIndex = aColumns.indexOf(COORDINATES_COLUMN);
	}

	@Override
	public String getColumnText(final Object aElement, final int aColumnIndex) {
		if (aElement == null) {
			return null;
		}

		final PositionData position = (PositionData) aElement;

		if (aColumnIndex == nameColumnIndex) {
			return position.getName();
		} else if (aColumnIndex == accuracyColumnIndex) {
			return String.valueOf(position.getAccuracy());
		} else if (aColumnIndex == timestampColumnIndex) {
			return position.getTimeStamp().toString(timestampFormatter);
		} else if (aColumnIndex == coordinatesColumnIndex) {
			final Coordinates coordinates = position.getCoordinates();
			return String.format("%1$.4f, %2$.4f", coordinates.getLatitude(), coordinates.getLongitude());
		} else {
			return null;
		}
	}
	

	@Override
	public Image getColumnImage(final Object aElement, final int aColumnIndex) {
		return null;
	}
}
