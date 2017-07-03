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

package joachimeichborn.geotag.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class PictureAnnotator {
	private static final Logger logger = Logger.getLogger(PictureAnnotator.class.getSimpleName());
	private static final DateTimeFormatter utcDateFormatter = DateTimeFormat.forPattern("yyyy:MM:dd HH:mm:ss")
			.withOffsetParsed().withZoneUTC();

	private final List<Picture> pictures;
	private final LinkedList<Track> tracks;
	private int tolerance;
	private boolean overwrite;
	private List<Picture> annotatedPictures;
	private List<Picture> nonAnnotatedPictures;

	/**
	 * 
	 * @param aTracks
	 * @param aPictures
	 * @param aTolerance tolerance in minutes
	 * @param aOverwrite
	 */
	public PictureAnnotator(final List<Track> aTracks, final List<Picture> aPictures, final int aTolerance,
			final boolean aOverwrite) {
		pictures = new LinkedList<>(aPictures);
		tracks = new LinkedList<>(aTracks);
		tolerance = aTolerance;
		overwrite = aOverwrite;

		annotatedPictures = new LinkedList<>();
		nonAnnotatedPictures = new LinkedList<>();
	}
	
	public void computeMatches() {
		final List<PositionData> positions = tracks.stream().flatMap(t -> t.getPositions().stream()).sorted().collect(Collectors.toCollection(ArrayList::new));

		logger.fine("Annotating " + pictures.size() + " pictures with " + positions.size() + " positions, tolerance "
				+ tolerance + " minutes and overwrite " + overwrite);
		
		if (!overwrite) {
			filterPicturesWithPosition();
		}

		for (final Picture picture : pictures) {
			long timestamp = 0;

			try {
				timestamp = utcDateFormatter.parseDateTime(picture.getTime()).getMillis();
			} catch (final IllegalArgumentException e) {
				logger.info("Could not parse time '" + picture.getTime() + "' from picture '" + picture.getFile()
						+ "': " + e.getMessage());
				nonAnnotatedPictures.add(picture);
				continue;
			}

			final PositionData position = findBestPosition(positions, timestamp);

			final long delta = Math.abs(timestamp - position.getTimeStampWithoutTimeZone().getMillis()) / 60_000;

			if (delta <= tolerance) {
				logger.finer("Accepted timestamp with delta " + delta + " min (" + picture.getFile() + ")");
				final Picture annotatedPicture = new Picture(picture.getFile(), picture.getTime(),
						position.getCoordinates(), picture.getGeocoding());
				annotatedPictures.add(annotatedPicture);
			} else {
				logger.fine("Rejected timestamp with delta " + delta + " min (" + picture.getFile() + ")");
				nonAnnotatedPictures.add(picture);
			}
		}

		logger.fine("Annotated " + annotatedPictures.size() + " pictures, skipped " + nonAnnotatedPictures.size()
				+ " pictures");
	}

	private void filterPicturesWithPosition() {
		final Iterator<Picture> iter = pictures.iterator();
		while (iter.hasNext()) {
			final Picture picture = iter.next();
			if (picture.getCoordinates() != null) {
				logger.finer("Ignoring picture " + picture.getFile() + " with existing position "
						+ picture.getCoordinates());
				nonAnnotatedPictures.add(picture);
				iter.remove();
			}
		}
	}

	private PositionData findBestPosition(final List<PositionData> aPositions, final long aMillis) {
		final int size = aPositions.size();
		if (size == 1) {
			return aPositions.get(0);
		} else if (size == 2) {
			if (Math.abs(aPositions.get(0).getTimeStampWithoutTimeZone().getMillis() - aMillis) < //
			Math.abs(aPositions.get(1).getTimeStampWithoutTimeZone().getMillis() - aMillis)) {
				return aPositions.get(0);
			} else {
				return aPositions.get(1);
			}
		} else {
			final PositionData middleEntry = aPositions.get(size / 2);
			if (middleEntry.getTimeStampWithoutTimeZone().getMillis() == aMillis) {
				return middleEntry;
			} else if (middleEntry.getTimeStampWithoutTimeZone().getMillis() < aMillis) {
				return findBestPosition(aPositions.subList(size / 2, aPositions.size()), aMillis);
			} else {
				return findBestPosition(aPositions.subList(0, size / 2 + 1), aMillis);
			}
		}
	}

	public List<Picture> getAnnotatedPictures() {
		return annotatedPictures;
	}

	public List<Picture> getNonAnnotatedPictures() {
		return nonAnnotatedPictures;
	}
}
