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

package joachimeichborn.geotag.geocode;

/**
 * Enum containing all implemented geocoding provider
 * 
 * @author Joachim von Eichborn
 */
public enum GeocodingProvider {
	MAP_QUEST("Map Quest (Open Street Map)") {
		@Override
		public Geocoder getGeocoder() {
			return new MapQuestGeocoder();
		}
	},
	GOOGLE("Google") {
		@Override
		public Geocoder getGeocoder() {
			return new GoogleGeocoder();
		}
	};

	private String displayName;

	GeocodingProvider(final String aDisplayName) {
		displayName = aDisplayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public abstract Geocoder getGeocoder();

	public static GeocodingProvider getByDisplayName(final String aDisplayName) {
		for (final GeocodingProvider provider : values()) {
			if (provider.getDisplayName().equals(aDisplayName)) {
				return provider;
			}
		}

		throw new IllegalArgumentException(
				"Could not find a geocoding provider for display name '" + aDisplayName + "'");
	}
}
