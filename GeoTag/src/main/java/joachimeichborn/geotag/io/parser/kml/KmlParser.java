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

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class KmlParser extends AbstractKmlParser {
	private static final Logger logger = Logger.getLogger(KmlParser.class.getSimpleName());

	public Track read(final Path aKmlFile) {
		logger.fine("Reading positions from " + aKmlFile);

		final Kml kml = Kml.unmarshal(aKmlFile.toFile());
		final List<PositionData> positions = readPositions(kml);

		logger.fine("Read " + positions.size() + " coordinates from " + aKmlFile);

		return new Track(aKmlFile, positions);
	}
}
