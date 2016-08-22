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

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;
import joachimeichborn.geotag.model.Picture;

public class PictureViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
	public static final String NAME_COLUMN = "Name";
	public static final String TIME_COLUMN = "Time";
	public static final String COORDINATES_COLUMN = "Coordinates";
	public static final String LOCATION_COLUMN = "Loaction name";
	public static final String CITY_COLUMN = "City";
	public static final String SUBLOCATION_COLUMN = "Sublocation";
	public static final String PROVINCE_STATE_COLUMN = "Province/State";
	public static final String COUNTRY_CODE_COLUMN = "Country code";
	public static final String COUNTRY_NAME_COLUMN = "Country name";

	private final int nameColumnIndex;
	private final int timeColumnIndex;
	private final int coordinatesColumnIndex;
	private int locationColumnIndex;
	private int cityColumnIndex;
	private int sublocationColumnIndex;
	private int provinceStateColumnIndex;
	private int countryCodeColumnIndex;
	private int countryNameColumnIndex;

	public PictureViewerLabelProvider(final List<String> aColumns) {
		nameColumnIndex = aColumns.indexOf(NAME_COLUMN);
		timeColumnIndex = aColumns.indexOf(TIME_COLUMN);
		coordinatesColumnIndex = aColumns.indexOf(COORDINATES_COLUMN);
		locationColumnIndex = aColumns.indexOf(LOCATION_COLUMN);
		cityColumnIndex = aColumns.indexOf(CITY_COLUMN);
		sublocationColumnIndex = aColumns.indexOf(SUBLOCATION_COLUMN);
		provinceStateColumnIndex = aColumns.indexOf(PROVINCE_STATE_COLUMN);
		countryCodeColumnIndex = aColumns.indexOf(COUNTRY_CODE_COLUMN);
		countryNameColumnIndex = aColumns.indexOf(COUNTRY_NAME_COLUMN);
	}

	@Override
	public String getColumnText(final Object aElement, final int aColumnIndex) {
		if (aElement == null) {
			return null;
		}

		final Picture picture = (Picture) aElement;

		if (aColumnIndex == nameColumnIndex) {
			return picture.getFile().getFileName().toString();
		} else if (aColumnIndex == timeColumnIndex) {
			return picture.getTime();
		} else if (aColumnIndex == coordinatesColumnIndex) {
			final Coordinates coordinates = picture.getCoordinates();
			if (coordinates != null) {
				return String.format("%1$.4f, %2$.4f", coordinates.getLatitude(), coordinates.getLongitude());
			} else {
				return "";
			}
		} else if (aColumnIndex == locationColumnIndex) {
			final Geocoding geocoding = picture.getGeocoding();
			if (geocoding != null) {
				return geocoding.getLocationName();
			} else {
				return "";
			}
		} else if (aColumnIndex == cityColumnIndex) {
			final Geocoding geocoding = picture.getGeocoding();
			if (geocoding != null) {
				return geocoding.getCity();
			} else {
				return "";
			}
		} else if (aColumnIndex == sublocationColumnIndex) {
			final Geocoding geocoding = picture.getGeocoding();
			if (geocoding != null) {
				return geocoding.getSublocation();
			} else {
				return "";
			}
		} else if (aColumnIndex == provinceStateColumnIndex) {
			final Geocoding geocoding = picture.getGeocoding();
			if (geocoding != null) {
				return geocoding.getProvinceState();
			} else {
				return "";
			}
		} else if (aColumnIndex == countryCodeColumnIndex) {
			final Geocoding geocoding = picture.getGeocoding();
			if (geocoding != null) {
				return geocoding.getCountryCode();
			} else {
				return "";
			}
		} else if (aColumnIndex == countryNameColumnIndex) {
			final Geocoding geocoding = picture.getGeocoding();
			if (geocoding != null) {
				return geocoding.getCountryName();
			} else {
				return "";
			}
		} else {
			return null;
		}
	}

	@Override
	public Image getColumnImage(final Object aElement, final int aColumnIndex) {
		return null;
	}
}
