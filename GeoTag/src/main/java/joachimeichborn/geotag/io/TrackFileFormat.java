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

package joachimeichborn.geotag.io;

import joachimeichborn.geotag.io.parser.TrackParser;
import joachimeichborn.geotag.io.parser.gpx.GpxParser;
import joachimeichborn.geotag.io.parser.kml.KmlParser;
import joachimeichborn.geotag.io.parser.kml.KmzParser;
import joachimeichborn.geotag.io.writer.TrackWriter;
import joachimeichborn.geotag.io.writer.kml.KmlWriter;
import joachimeichborn.geotag.io.writer.kml.KmzWriter;

public enum TrackFileFormat {
	KML("kml") {
		@Override
		public TrackParser getParser() {
			return new KmlParser();
		}

		public TrackWriter getWriter() {
			return new KmlWriter();
		}
	},
	KMZ("kmz") {
		@Override
		public TrackParser getParser() {
			return new KmzParser();
		}

		public TrackWriter getWriter() {
			return new KmzWriter();
		}
	},
	GPX("gpx") {
		@Override
		public TrackParser getParser() {
			return new GpxParser();
		}

		public TrackWriter getWriter() {
			throw new UnsupportedOperationException("Writing GPX files is not supported");
		}
	};

	private final String extension;

	private TrackFileFormat(final String aExtension) {
		extension = aExtension;
	}

	public abstract TrackParser getParser();

	public abstract TrackWriter getWriter();

	public static TrackFileFormat getByExtension(final String aExtension) {
		for (final TrackFileFormat format : values()) {
			if (format.extension.equals(aExtension)) {
				return format;
			}
		}

		throw new IllegalArgumentException("Unknown extension '" + aExtension + "'.");
	}
}