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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import joachimeichborn.geotag.model.Track;

public class KmlWriter extends AbstractKmlWriter {
	private static final Logger logger = Logger.getLogger(KmlWriter.class.getSimpleName());

	/**
	 * Create the output KML file containing:
	 * <ul>
	 * <li>placemarks for all positions
	 * <li>a path connecting all positions in chronological order
	 * <li>circles showing the accuracy information for all positions
	 * </ul>
	 * 
	 * @throws IOException
	 */
	public void write(final Track aTrack, final Path aOutputFile) throws IOException {
		final String documentTitle = FilenameUtils.removeExtension(aOutputFile.getFileName().toString());

		final GeoTagKml kml = createKml(documentTitle, aTrack);

		try (final Writer kmlWriter = new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(aOutputFile.toFile())), StandardCharsets.UTF_8)) {
			kml.marshal(kmlWriter);
		}

		logger.fine("Wrote track to " + aOutputFile);
	}
}
