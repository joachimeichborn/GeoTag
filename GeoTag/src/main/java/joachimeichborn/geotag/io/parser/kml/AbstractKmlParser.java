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

package joachimeichborn.geotag.io.parser.kml;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.TimePrimitive;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import joachimeichborn.geotag.io.parser.TrackParser;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;

public abstract class AbstractKmlParser implements TrackParser {
	private static final Logger logger = Logger.getLogger(AbstractKmlParser.class.getSimpleName());
	private static String FLOAT_PATTERN = "^\\d*.\\d*$";

	List<PositionData> readPositions(final Kml aKml) {
		final List<PositionData> positions = new LinkedList<>();

		final Feature kmlFeature = aKml.getFeature();
		if (kmlFeature != null) {
			if (kmlFeature instanceof Document) {
				parseDocument((Document) kmlFeature, positions);
			} else if (kmlFeature instanceof Folder) {
				final Folder folder = (Folder) kmlFeature;
				for (final Feature folderFeature : folder.getFeature()) {
					if (folderFeature instanceof Document) {
						parseDocument((Document) folderFeature, positions);
					}
				}
			}
		}

		return positions;
	}

	/**
	 * Parse a document subnode within the KML tree
	 * 
	 * @param aDocument
	 *            The document subnode
	 * @param aPositions
	 *            The position list
	 */
	private void parseDocument(final Document aDocument, final List<PositionData> aPositions) {
		for (final Feature documentFeature : aDocument.getFeature()) {
			if (documentFeature instanceof Placemark) {
				parsePlacemark((Placemark) documentFeature, aPositions);
			} else if (documentFeature instanceof Folder) {
				final Folder folder = (Folder) documentFeature;
				for (final Feature folderFeature : folder.getFeature()) {
					if (folderFeature instanceof Placemark) {
						parsePlacemark((Placemark) folderFeature, aPositions);
					}
				}
			}
		}
	}

	/**
	 * Parse a placemark subnode within the KML tree. If the placemark contains
	 * a position, create a new {@link PositionData} object and add it to the
	 * position list
	 * 
	 * @param aPlacemark
	 *            The placemark subnode
	 * @param aPositions
	 *            The position list
	 */
	private void parsePlacemark(final Placemark aPlacemark, final List<PositionData> aPositions) {
		final TimePrimitive timePrimitive = aPlacemark.getTimePrimitive();
		if (timePrimitive instanceof TimeStamp) {
			final TimeStamp timeStamp = (TimeStamp) timePrimitive;
			final String time = timeStamp.getWhen();
			final Geometry geometry = aPlacemark.getGeometry();
			if (geometry instanceof Point) {
				final Point point = (Point) geometry;
				final List<Coordinate> coordinates = point.getCoordinates();
				for (final Coordinate coordinate : coordinates) {
					float accuracy = getPositionAccuracy(aPlacemark.getDescription());
					final Coordinates position = new Coordinates(coordinate.getLatitude(), coordinate.getLongitude(),
							coordinate.getAltitude());
					aPositions.add(new PositionData(position, time, aPlacemark.getName(), accuracy));
				}
			}
		}
	}

	private float getPositionAccuracy(final String aText) {
		if (!StringUtils.isEmpty(aText)) {
			if (aText.matches(FLOAT_PATTERN)) {
				return Float.valueOf(aText);
			}
		}

		logger.fine("Found description '" + aText + "' that does not contain accuracy information");
		return 0;
	}
}
