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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.TimePrimitive;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import joachimeichborn.geotag.io.parser.PositionParsingException;
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
				positions.addAll(parseDocument((Document) kmlFeature));
			} else if (kmlFeature instanceof Folder) {
				final Folder folder = (Folder) kmlFeature;
				for (final Feature folderFeature : folder.getFeature()) {
					if (folderFeature instanceof Document) {
						positions.addAll(parseDocument((Document) folderFeature));
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
	 * @return
	 */
	private List<PositionData> parseDocument(final Document aDocument) {
		final List<PositionData> positions = new ArrayList<PositionData>();

		for (final Feature documentFeature : aDocument.getFeature()) {
			if (documentFeature instanceof Placemark) {
				final PositionData position = parsePlacemark((Placemark) documentFeature);
				if (position != null) {
					positions.add(position);
				}
			} else if (documentFeature instanceof Folder) {
				final Folder folder = (Folder) documentFeature;
				for (final Feature folderFeature : folder.getFeature()) {
					if (folderFeature instanceof Placemark) {
						final PositionData position = parsePlacemark((Placemark) folderFeature);
						if (position != null) {
							positions.add(position);
						}
					}
				}
			}
		}

		return positions;
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
	 * @throws PositionParsingException
	 */
	private PositionData parsePlacemark(final Placemark aPlacemark) {
		final TimePrimitive timePrimitive = aPlacemark.getTimePrimitive();
		final Geometry geometry = aPlacemark.getGeometry();
		if (!(timePrimitive instanceof TimeStamp)) {
			logger.finer("Skipping placemark " + aPlacemark.getName() + " without time stamp");
			return null;
		}
		if (!(geometry instanceof Point)) {
			logger.finer("Skipping placemark " + aPlacemark.getName() + " without point geometry");
			return null;
		}

		final String time = ((TimeStamp) timePrimitive).getWhen();
		float accuracy = parseAccuracy(aPlacemark);
		final Coordinates position;
		try {
			position = parseCoordinates(geometry);
		} catch (PositionParsingException e) {
			logger.log(Level.WARNING, "Could not parse placemark " + aPlacemark.getName(), e);
			return null;
		}

		return new PositionData(position, time, aPlacemark.getName(), accuracy);
	}

	private float parseAccuracy(final Placemark aPlacemark) {
		if (aPlacemark.getExtendedData() != null) {
			final List<Data> data = aPlacemark.getExtendedData().getData();
			for (final Data entry : data) {
				if ("accuracy".equals(entry.getName()) && entry.getValue().matches(FLOAT_PATTERN)) {
					return Float.valueOf(entry.getValue());
				}
			}
		}

		final String description = aPlacemark.getDescription();
		if (!StringUtils.isEmpty(description) && description.matches(FLOAT_PATTERN)) {
			return Float.valueOf(description);
		}

		return 0f;
	}

	private Coordinates parseCoordinates(final Geometry aGeometry) throws PositionParsingException {
		final List<Coordinate> coordinates = ((Point) aGeometry).getCoordinates();

		if (coordinates.size() != 1) {
			throw new PositionParsingException(
					"Placemark contains " + coordinates.size() + " coordinates instead of one as expected");
		}

		final Coordinate coordinate = coordinates.get(0);
		final Coordinates position = new Coordinates(coordinate.getLatitude(), coordinate.getLongitude(),
				coordinate.getAltitude());
		return position;
	}
}
