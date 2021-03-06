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

package joachimeichborn.geotag.io.parser.gpx;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import joachimeichborn.geotag.io.parser.TrackParser;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;

public class GpxParser implements TrackParser {
	private static final Logger logger = Logger.getLogger(GpxParser.class.getSimpleName());

	private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withOffsetParsed();

	private static class Handler extends DefaultHandler {
		private final List<PositionData> positions = new LinkedList<>();
		private final StringBuffer buffer = new StringBuffer();
		private Double longitude;
		private Double latitude;
		private double altitude;
		private String timestamp;

		@Override
		public void startElement(final String aUri, final String aLocalName, final String aQualifiedName,
				final Attributes aAttributes) {
			buffer.setLength(0);

			switch (aLocalName) {
			case "trkpt": {
				altitude = 0;
				timestamp = null;

				final String longitudeAtt = aAttributes.getValue("lon");
				longitude = NumberUtils.createDouble(longitudeAtt);

				final String latitudeAtt = aAttributes.getValue("lat");
				latitude = NumberUtils.createDouble(latitudeAtt);

				break;
			}
			}
		}

		@Override
		public void endElement(final String aUri, final String aLocalName, final String aQualifiedName) {
			switch (aLocalName) {
			case "ele": {
				final String elevationContent = buffer.toString();

				altitude = Double.parseDouble(elevationContent);
				break;
			}
			case "time": {
				timestamp = buffer.toString();
				break;
			}
			case "trkpt": {
				if (longitude != null && latitude != null && timestamp != null) {
					final Coordinates coordinates = new Coordinates(latitude, longitude, altitude);
					final PositionData position = new PositionData(coordinates, formatter.parseDateTime(timestamp),
							timestamp, 0);
					positions.add(position);
				} else {
					logger.severe("Could not create position based on longitude '" + longitude + "', latitude '"
							+ latitude + "', timestamp '" + timestamp + "'");
				}
				break;
			}
			}
		}

		@Override
		public void characters(final char[] aCharacters, final int aStart, final int aLength) {
			buffer.append(new String(aCharacters, aStart, aLength));
		}

		public List<PositionData> getPositions() {
			return positions;
		}
	}

	public joachimeichborn.geotag.model.Track read(final Path aGpxFile) throws IOException {
		logger.fine("Reading positions from " + aGpxFile);

		final List<PositionData> positions = new LinkedList<>();

		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			final SAXParser saxParser = factory.newSAXParser();
			final Handler handler = new Handler();
			saxParser.parse(aGpxFile.toFile(), handler);
			
			positions.addAll(handler.getPositions());

		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException(e);
		}

		logger.fine("Read " + positions.size() + " coordinates from " + aGpxFile);

		return new joachimeichborn.geotag.model.Track(aGpxFile, positions);
	}
}
