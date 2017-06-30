package joachimeichborn.geotag.io.parser.kml;

import java.nio.file.Path;
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
import joachimeichborn.geotag.io.parser.Parser;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class KmlReader implements Parser {
	private static final Logger logger = Logger.getLogger(KmlReader.class.getSimpleName());
	private static String FLOAT_PATTERN = "^\\d*.\\d*$";

	public Track read(final Path aKmlFile) {
		logger.fine("Reading positions from " + aKmlFile);

		final List<PositionData> positions = new LinkedList<>();

		final Kml kml = Kml.unmarshal(aKmlFile.toFile());
		final Feature kmlFeature = kml.getFeature();
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

		logger.fine("Read " + positions.size() + " coordinates from " + aKmlFile);

		return new Track(aKmlFile, positions);
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
