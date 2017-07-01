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

package joachimeichborn.geotag.io.writer.kml;

import java.io.IOException;

import org.joda.time.format.ISODateTimeFormat;

import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import joachimeichborn.geotag.io.writer.TrackWriter;
import joachimeichborn.geotag.io.writer.kml.CirclePolygon.Radian;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public abstract class AbstractKmlWriter implements TrackWriter {
	private static final String EXTENDED_DATA_DATE = "date";
	private static final String EXTENDED_DATA_ACCURACY = "accuracy";
	private static final String EXTENDED_DATA_TIME = "time";
	private static final String EXTENDED_DATA_NAME = "name";

	private static final String PIN_STYLE = "PinStyle";
	private static final String ACCURACY_STYLE = "AccuracyStyle";
	private static final String PATH_STYLE = "PathStyle";

	GeoTagKml createKml(final String aTitle, final Track aTrack) throws IOException {
		final Document document = new Document().withName(aTitle).withOpen(true);

		addStylesDefinitions(document);

		addPlacemarkFolder(document, aTrack);

		addPath(document, aTrack);

		addAccuracyCircles(document, aTrack);

		final GeoTagKml kml = new GeoTagKml();
		kml.setFeature(document);

		return kml;
	}

	/**
	 * Add styles that define how the contents of the KML file should be
	 * displayed
	 * 
	 * @param aDocument
	 *            The KML document that is build
	 */
	private void addStylesDefinitions(final Document aDocument) {
		aDocument.createAndAddStyle().withLineStyle(new LineStyle().withColor("ff0000ff").withWidth(2))
				.withId(PATH_STYLE);
		aDocument.createAndAddStyle().withLineStyle(new LineStyle().withColor("ff55ff7f").withWidth(2))
				.withId(ACCURACY_STYLE);
		aDocument.createAndAddStyle().withLabelStyle(new LabelStyle().withColor("00ffffff"))
				.withBalloonStyle(new BalloonStyle()
						.withText("<h3>$[" + EXTENDED_DATA_NAME + "]</h3><br><table border='1' cellpadding='5'>" + //
								"<tr><td>Date</td><td>$[" + EXTENDED_DATA_DATE + "]</td></tr>" + //
								"<tr><td>Time</td><td>$[" + EXTENDED_DATA_TIME + "]</td></tr>" + //
								"<tr><td>Accuracy</td><td>$[" + EXTENDED_DATA_ACCURACY + "]</td></tr>" + //
								"</table>"))
				.withId(PIN_STYLE);
	}

	/**
	 * Add a placemark for each position
	 * 
	 * @param aDocument
	 *            The KML document that is build
	 * @param aTrack
	 */
	private void addPlacemarkFolder(final Document aDocument, final Track aTrack) {
		final Folder pinFolder = new Folder().withName("Places").withOpen(false);
		for (final PositionData position : aTrack.getPositions()) {
			final Placemark place = new Placemark();
			place.setName(position.getName());
			place.setDescription(Float.toString(position.getAccuracy()));
			place.createAndSetTimeStamp()
					.withWhen(position.getTimeStamp().toString(ISODateTimeFormat.dateTimeNoMillis()));
			place.createAndSetPoint().addToCoordinates(position.getCoordinates().getLongitude(),
					position.getCoordinates().getLatitude(), position.getCoordinates().getAltitude());
			place.setStyleUrl("#" + PIN_STYLE);
			place.setVisibility(false);
			final ExtendedData extendedData = place.createAndSetExtendedData();
			extendedData.createAndAddData(position.getName()).withName(EXTENDED_DATA_NAME);
			extendedData.createAndAddData(position.getTimeStamp().toString(ISODateTimeFormat.yearMonthDay()))
					.withName(EXTENDED_DATA_DATE);
			extendedData.createAndAddData(position.getTimeStamp().toString(ISODateTimeFormat.hourMinuteSecond()))
					.withName(EXTENDED_DATA_TIME);
			extendedData.createAndAddData(Float.toString(position.getAccuracy())).withName(EXTENDED_DATA_ACCURACY);
			pinFolder.getFeature().add(place);
		}
		aDocument.getFeature().add(pinFolder);
	}

	/**
	 * Add a path that connects all positions in chronological order
	 * 
	 * @param aDocument
	 *            The KML document that is build
	 */
	private void addPath(final Document aDocument, final Track aTrack) {
		final LineString line = new LineString().withTessellate(true);

		PositionData lastPosition = null;
		for (final PositionData position : aTrack.getPositions()) {
			if (lastPosition != null && !(position.getCoordinates().equals(lastPosition.getCoordinates()))) {
				line.addToCoordinates(position.getCoordinates().getLongitude(), position.getCoordinates().getLatitude(),
						position.getCoordinates().getAltitude());
			}
			lastPosition = position;
		}
		aDocument.createAndAddPlacemark().withName("Path").withStyleUrl("#" + PATH_STYLE).withGeometry(line);
	}

	/**
	 * Add for each position a circle that displays it's accuracy
	 * 
	 * @param aDocument
	 *            The KML document that is build
	 */
	private void addAccuracyCircles(final Document aDocument, final Track aTrack) {
		final Folder accuraciesFolder = new Folder().withName("Accuracies").withOpen(false);
		for (final PositionData position : aTrack.getPositions()) {
			if (position.getAccuracy() > 0f) {
				final LineString circle = new LineString().withTessellate(true);

				for (final Radian point : CirclePolygon.calculateCirclePoints(position.getCoordinates().getLatitude(),
						position.getCoordinates().getLongitude(), position.getAccuracy())) {
					circle.addToCoordinates(point.getLongitude(), point.getLatitude());
				}
				accuraciesFolder.createAndAddPlacemark().withName(String.valueOf(position.getAccuracy()))
						.withStyleUrl("#" + ACCURACY_STYLE).withVisibility(false).withGeometry(circle)
						.createAndSetTimeStamp()
						.withWhen(position.getTimeStamp().toString(ISODateTimeFormat.dateTimeNoMillis()));
			}
		}
		aDocument.getFeature().add(accuraciesFolder);
	}

}
