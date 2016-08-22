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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.commons.lang3.StringUtils;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;

/**
 * Geocoder that provides the data of OpenStreetMap Nominatim, but seemingly
 * without restrictions on the number of queries that can be submitted
 * 
 * @author Joachim von Eichborn
 */
public class MapQuestGeocoder implements Geocoder {
	private final static String GEOCODE_URL = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=json&accept-language=de,en&addressdetails=1&zoom=18&lat=%f&lon=%f";

	public static enum LocationType {
		COUNTRY_CODE(new String[] { "country_code" }), //
		COUNTRY_NAME(new String[] { "country" }), //
		PROVINCE_STATE(new String[] { "state" }), //
		SUBLOCATION(new String[] { "state_district" }), //
		CITY(new String[] { "city", "village" }), //
		SUBURB(new String[] { "suburb" });

		private List<String> identifiers;

		LocationType(final String[] aIdentifiers) {
			identifiers = Arrays.asList(aIdentifiers);
		}

		public List<String> getIdentifiers() {
			return identifiers;
		}
	}

	private static final Logger logger = Logger.getLogger(MapQuestGeocoder.class.getSimpleName());

	public Geocoding queryPosition(final Coordinates aPosition) {
		final JsonObject jsonRepresentation = getGeocodeResult(aPosition);

		if (jsonRepresentation != null) {
			return extractLocationInformation(jsonRepresentation);
		}

		return null;
	}

	private Geocoding extractLocationInformation(final JsonObject aJsonRepresentation) {
		final JsonString location = aJsonRepresentation.getJsonString("display_name");
		final JsonObject addressDetails = aJsonRepresentation.getJsonObject("address");

		return createTextualRepresentation(location.getString(), addressDetails);
	}

	private Geocoding createTextualRepresentation(final String aLocation, final JsonObject aAddressDetails) {
		final Geocoding.Builder builder = new Geocoding.Builder();

		builder.setCountryCode(getMatchingContent(LocationType.COUNTRY_CODE, aAddressDetails));
		builder.setCountryName(getMatchingContent(LocationType.COUNTRY_NAME, aAddressDetails));
		builder.setProvinceState(getMatchingContent(LocationType.PROVINCE_STATE, aAddressDetails));
		builder.setSublocation(getMatchingContent(LocationType.SUBLOCATION, aAddressDetails));
		final List<String> city = new LinkedList<>();
		city.add(getMatchingContent(LocationType.CITY, aAddressDetails));
		city.add(getMatchingContent(LocationType.SUBURB, aAddressDetails));
		builder.setCity(StringUtils.join(city, ", "));
		builder.setLocationName(aLocation);

		return builder.build();
	}

	private String getMatchingContent(final LocationType aLocationType, final JsonObject aAddressDetails) {
		for (final String identifier : aLocationType.getIdentifiers()) {
			final JsonString value = aAddressDetails.getJsonString(identifier);
			if (value != null) {
				return value.getString();
			}
		}

		return null;
	}

	private JsonObject getGeocodeResult(final Coordinates aPosition) {
		URL url;
		try {
			url = new URL(
					String.format(Locale.ENGLISH, GEOCODE_URL, aPosition.getLatitude(), aPosition.getLongitude()));
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "Initializing URL \"" + GEOCODE_URL + "\" failed", e);
			return null;
		}

		return makeRequest(url);
	}

	private JsonObject makeRequest(final URL aUrl) {
		JsonObject jsonRepresentation = null;
		try (final InputStream is = aUrl.openStream(); final JsonReader rdr = Json.createReader(is)) {
			jsonRepresentation = rdr.readObject();
		} catch (final IOException e) {
			logger.fine("Receiving textual representation failed: " + e.getMessage());
		}

		return jsonRepresentation;
	}
}
