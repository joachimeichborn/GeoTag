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
import java.nio.file.Path;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import joachimeichborn.geotag.model.Track;

public class KmzWriter extends AbstractKmlWriter {
	private static final Logger logger = Logger.getLogger(KmzWriter.class.getSimpleName());

	public void write(final Track aTrack, final Path aOutputFile) throws IOException {
		final String documentTitle = FilenameUtils.removeExtension(aOutputFile.getFileName().toString());

		final GeoTagKml kml = createKml(documentTitle, aTrack);

		kml.marshalAsKmz(aOutputFile.toString());

		logger.fine("Wrote track to " + aOutputFile);
	}
}
