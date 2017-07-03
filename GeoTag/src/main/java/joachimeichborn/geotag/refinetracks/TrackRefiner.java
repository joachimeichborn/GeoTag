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

package joachimeichborn.geotag.refinetracks;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.RGB;

import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class TrackRefiner {
	private static final Logger logger = Logger.getLogger(TrackRefiner.class.getSimpleName());
	private ImproveTrackOptions improvementOptions;
	private List<Track> tracks;
	private List<TrackImprovementTask> tasks;

	public TrackRefiner(final ImproveTrackOptions aImprovementOptions, final List<Track> aTracks) {
		improvementOptions = aImprovementOptions;
		tracks = new LinkedList<>(aTracks);

		tasks = new LinkedList<>();
		tasks.add(new RemoveDuplicatePositionsTask(improvementOptions));
		tasks.add(new FilterPositionsByPairwiseDistanceTask(improvementOptions));
		tasks.add(new ReplacePositionsByAccuracyComparisonTask(improvementOptions));
		tasks.add(new FilterPositionsByAccuracyRadiusTask(improvementOptions));
		tasks.add(new RemoveIrrelevantPositionsTask(improvementOptions));
		tasks.add(new InterpolatePositionsTask(improvementOptions));
	}

	public Track refine() {
		final List<PositionData> positions = new ArrayList<PositionData>();

		for (final Track track : tracks) {
			positions.addAll(track.getPositions());
		}

		logger.info("Refining " + positions.size() + " positions from " + tracks.size() + " tracks");

		for (final TrackImprovementTask task : tasks) {
			task.execute(positions);
		}

		final Track refinedTrack = new Track(Paths.get("refined.kml"), positions, new RGB(255, 255, 255));

		logger.info("Created refined track with " + refinedTrack.getPositions().size() + " positions");
		return refinedTrack;
	}
}
