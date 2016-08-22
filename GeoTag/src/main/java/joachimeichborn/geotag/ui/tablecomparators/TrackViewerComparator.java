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

import org.eclipse.e4.ui.workbench.swt.internal.copy.ViewComparator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.ui.labelprovider.TrackViewerObservableLabelProvider;

public class TrackViewerComparator extends ViewComparator {
	private String column;
	private boolean direction;

	public TrackViewerComparator() {
		column = TrackViewerObservableLabelProvider.NAME_COLUMN;
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
		final Track t1 = (Track) aObj1;
		final Track t2 = (Track) aObj2;
		int rc = 0;
		switch (column) {
		case TrackViewerObservableLabelProvider.NAME_COLUMN:
			rc = t1.getFile().getFileName().toString().compareTo(t2.getFile().getFileName().toString());
			break;
		case TrackViewerObservableLabelProvider.POSITION_COUNT_COLUMN:
			rc = Integer.compare(t1.getPositions().size(), t2.getPositions().size());
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
