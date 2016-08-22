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

import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerLabelProvider;

public class PictureViewerComparator extends ViewComparator {
	private String column;
	private boolean direction;

	public PictureViewerComparator() {
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
		final Picture p1 = (Picture) aObj1;
		final Picture p2 = (Picture) aObj2;
		int rc = 0;
		switch (column) {
		case PictureViewerLabelProvider.NAME_COLUMN:
			rc = p1.getFile().getFileName().toString().compareTo(p2.getFile().getFileName().toString());
			break;
		case PictureViewerLabelProvider.TIME_COLUMN:
			rc = p1.getTime().compareTo(p2.getTime());
			break;
		case PictureViewerLabelProvider.COORDINATES_COLUMN:
			ObjectUtils.compare(p1.getCoordinates(), p2.getCoordinates());
			break;
		case PictureViewerLabelProvider.LOCATION_COLUMN:
			rc = p1.getGeocoding().getLocationName().compareTo(p2.getGeocoding().getLocationName());
			break;
		case PictureViewerLabelProvider.CITY_COLUMN:
			rc = p1.getGeocoding().getCity().compareTo(p2.getGeocoding().getCity());
			break;
		case PictureViewerLabelProvider.SUBLOCATION_COLUMN:
			rc = p1.getGeocoding().getSublocation().compareTo(p2.getGeocoding().getSublocation());
			break;
		case PictureViewerLabelProvider.PROVINCE_STATE_COLUMN:
			rc = p1.getGeocoding().getProvinceState().compareTo(p2.getGeocoding().getProvinceState());
			break;
		case PictureViewerLabelProvider.COUNTRY_CODE_COLUMN:
			rc = p1.getGeocoding().getCountryCode().compareTo(p2.getGeocoding().getCountryCode());
			break;
		case PictureViewerLabelProvider.COUNTRY_NAME_COLUMN:
			rc = p1.getGeocoding().getCountryName().compareTo(p2.getGeocoding().getCountryName());
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
