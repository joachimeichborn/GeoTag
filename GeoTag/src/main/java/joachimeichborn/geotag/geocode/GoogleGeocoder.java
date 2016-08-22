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
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.StringUtils;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;

/**
 * Geocoding implementation based on googles geocoding API
 * 
 * @author Joachim von Eichborn
 */
public class GoogleGeocoder implements Geocoder {
	private final static String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?sensor=false&latlng=";
	private final static String STATUS_KEY = "status";
	private final static String TYPES_KEY = "types";
	private final static String ADDRESS_COMPONENTS_KEY = "address_components";
	private final static String FORMATTED_ADDRESS_ATTRIBUTE = "formatted_address";
	private final static String LONG_NAME_ATTRIBUTE = "long_name";
	private final static String SHORT_NAME_ATTRIBUTE = "short_name";

	public enum LocationType {
		COUNTRY_POLITICAL(new String[] { "country", "political" }), //
		LOCALITY_POLITICAL(new String[] { "locality", "political" }), //
		ADMINISTRATIVE_AREA_LEVEL_1_POLITICAL(new String[] { "administrative_area_level_1", "political" }), //
		ADMINISTRATIVE_AREA_LEVEL_2_POLITICAL(new String[] { "administrative_area_level_2", "political" }), //
		ADMINISTRATIVE_AREA_LEVEL_3_POLITICAL(new String[] { "administrative_area_level_3", "political" }), //
		SUBLOCALITY_POLITICAL(new String[] { "sublocality", "political" }), //
		SUBLOCALITY_LEVEL_1_POLITICAL(new String[] { "sublocality_level_1", "political" }), //
		ROUTE(new String[] { "route" });

		private JsonArray identifiers;

		LocationType(final String[] aIdentifiers) {
			final JsonArrayBuilder builder = Json.createArrayBuilder();

			for (final String identifier : aIdentifiers) {
				builder.add(identifier);
			}

			identifiers = builder.build();
		}

		public JsonArray getIdentifiers() {
			return identifiers;
		}
	}

	private static final Logger logger = Logger.getLogger(GoogleGeocoder.class.getSimpleName());

	public Geocoding queryPosition(final Coordinates aPosition) {
		final JsonObject jsonRepresentation = getGeocodeResult(aPosition);

		if (jsonRepresentation != null) {
			return extractLocationInformation(jsonRepresentation);
		}

		return null;
	}

	private Geocoding extractLocationInformation(final JsonObject aJsonRepresentation) {
		final JsonArray results = aJsonRepresentation.getJsonArray("results");
		logger.fine("Found " + String.valueOf(results.size()) + " entries");

		return createTextualRepresentation(results);
	}

	private Geocoding createTextualRepresentation(final JsonArray aResults) {
		final Geocoding.Builder builder = new Geocoding.Builder();

		builder.setCountryCode(
				getMatchingContentFromAddressComponent(SHORT_NAME_ATTRIBUTE, aResults, LocationType.COUNTRY_POLITICAL));
		builder.setCountryName(
				getMatchingContentFromAddressComponent(LONG_NAME_ATTRIBUTE, aResults, LocationType.COUNTRY_POLITICAL));
		builder.setCity(
				getMatchingContentFromAddressComponent(LONG_NAME_ATTRIBUTE, aResults, LocationType.LOCALITY_POLITICAL));
		builder.setProvinceState(getMatchingContentFromAddressComponent(LONG_NAME_ATTRIBUTE, aResults,
				LocationType.ADMINISTRATIVE_AREA_LEVEL_1_POLITICAL, LocationType.ADMINISTRATIVE_AREA_LEVEL_2_POLITICAL,
				LocationType.ADMINISTRATIVE_AREA_LEVEL_3_POLITICAL));
		builder.setSublocation(getMatchingContentFromAddressComponent(LONG_NAME_ATTRIBUTE, aResults,
				LocationType.SUBLOCALITY_POLITICAL, LocationType.SUBLOCALITY_LEVEL_1_POLITICAL));
		builder.setLocationName(getMatchingContent(FORMATTED_ADDRESS_ATTRIBUTE, aResults,
				LocationType.LOCALITY_POLITICAL, LocationType.SUBLOCALITY_POLITICAL,
				LocationType.SUBLOCALITY_LEVEL_1_POLITICAL, LocationType.ROUTE));

		return builder.build();
	}

	private String getMatchingContentFromAddressComponent(final String aAttribute, final JsonArray aResults,
			final LocationType... aLocationTypes) {
		for (final LocationType locationType : aLocationTypes) {
			for (final JsonObject result : aResults.getValuesAs(JsonObject.class)) {
				final JsonArray addressComponents = result.getJsonArray(ADDRESS_COMPONENTS_KEY);
				final String content = getMatchingContent(aAttribute, addressComponents, locationType);
				if (!StringUtils.isEmpty(content)) {
					return content;
				}
			}
		}

		return null;
	}

	private String getMatchingContent(final String aAttribute, final JsonArray aResults,
			final LocationType... aLocationTypes) {
		for (final LocationType locationType : aLocationTypes) {
			for (final JsonObject component : aResults.getValuesAs(JsonObject.class)) {
				final JsonArray types = component.getJsonArray(TYPES_KEY);
				if (types.containsAll(locationType.getIdentifiers()) && component.containsKey(aAttribute)) {
					return component.getString(aAttribute);
				}
			}
		}

		return null;
	}

	private JsonObject getGeocodeResult(final Coordinates aPosition) {
		URL url;
		try {
			url = new URL(GEOCODE_URL + aPosition.getLatitude() + "," + aPosition.getLongitude());
		} catch (MalformedURLException e1) {
			logger.severe("Initializing URL \"" + GEOCODE_URL + "\" failed");
			return null;
		}

		final JsonObject jsonRepresentation = retrieveLocation(url);

		if (locationRetrievalSuccessful(jsonRepresentation)) {
			return jsonRepresentation;
		} else {
			return null;
		}
	}

	private boolean locationRetrievalSuccessful(final JsonObject aJsonRepresentation) {
		final String status = getStatus(aJsonRepresentation);

		if ("OK".equals(status)) {
			return true;
		} else {
			if ("OVER_QUERY_LIMIT".equals(status)) {
				logger.severe("Could not obtain a textual representation because query limit was reached");
			} else {
				logger.severe("Geocoding failed, status code is " + status);
			}
			return false;
		}
	}

	private JsonObject retrieveLocation(final URL aUrl) {
		JsonObject jsonRepresentation = null;
		try (final InputStream is = aUrl.openStream(); final JsonReader rdr = Json.createReader(is)) {
			jsonRepresentation = rdr.readObject();
		} catch (final IOException e) {
			logger.fine("Receiving textual representation failed: " + e.getMessage());
		}

		return jsonRepresentation;
	}

	private String getStatus(final JsonObject aJsonRepresentation) {
		if (aJsonRepresentation == null || !aJsonRepresentation.containsKey(STATUS_KEY)) {
			logger.fine("JSON status cannot be obtained");
			return "";
		}

		return aJsonRepresentation.getString(STATUS_KEY);
	}
}
