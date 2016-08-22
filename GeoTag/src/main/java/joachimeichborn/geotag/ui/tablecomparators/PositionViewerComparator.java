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

package joachimeichborn.geotag.ui.tablecomparators;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.e4.ui.workbench.swt.internal.copy.ViewComparator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerLabelProvider;
import joachimeichborn.geotag.ui.labelprovider.PositionsViewerLabelProvider;

public class PositionViewerComparator extends ViewComparator {
	private String column;
	private boolean direction;

	public PositionViewerComparator() {
		column = PictureViewerLabelProvider.NAME_COLUMN;
		direction = true;
	}

	public int getDirection() {
		return direction ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(final String aColumn) {
		if (aColumn.equals(column)) {
			direction = !direction;
		} else {
			column = aColumn;
			direction = true;
		}
	}

	@Override
	public int compare(final Viewer aViewer, final Object aObj1, final Object aObj2) {
		final PositionData p1 = (PositionData) aObj1;
		final PositionData p2 = (PositionData) aObj2;
		int rc = 0;
		switch (column) {
		case PositionsViewerLabelProvider.NAME_COLUMN:
			rc = p1.getName().compareTo(p2.getName());
			break;
		case PositionsViewerLabelProvider.COORDINATES_COLUMN:
			ObjectUtils.compare(p1.getCoordinates(), p2.getCoordinates());
			break;
		case PositionsViewerLabelProvider.TIMESTAMP_COLUMN:
			rc = p1.getTimeStamp().compareTo(p2.getTimeStamp());
			break;
		case PositionsViewerLabelProvider.ACCURACY_COLUMN:
			rc = Float.compare(p1.getAccuracy(), p2.getAccuracy());
			break;
		default:
			rc = 0;
		}

		if (!direction) {
			rc = -rc;
		}

		return rc;
	}

}
