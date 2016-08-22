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

import java.util.ArrayList;
import java.util.List;

import joachimeichborn.geotag.model.PositionData;

/**
 * Remove positions where two neighbouring positions with the same coordinates
 * and accuracy exist that are timepoint wise less than a given threshold apart
 * 
 * @author Joachim von Eichborn
 */
public class RemoveIrrelevantPositionsTask extends AbstractTrackImprovementTask {
	private static final long MAX_TIME_DELTA = 300_000;

	public RemoveIrrelevantPositionsTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	public boolean isEnabled() {
		return options.getRemoveIrrelevantPositions();
	}

	/**
	 * Remove irrelevant positions from the list.<br>
	 * 
	 * Positions are considered to be irrelevant if the two neighbouring
	 * positions have the same coordinates and accuracy and are timepoint wise
	 * less than a given threshold apart.
	 */
	@Override
	void process(final List<PositionData> aPositions) {
		logger.fine("Filtering irrelevant positions activated");

		final List<PositionData> clonedPositions = new ArrayList<PositionData>(aPositions);

		if (!aPositions.isEmpty()) {
			aPositions.clear();
			aPositions.add(clonedPositions.get(0));

			for (int i = 1; i < clonedPositions.size() - 1; i++) {
				final PositionData previous = clonedPositions.get(i - 1);
				final PositionData position = clonedPositions.get(i);
				final PositionData next = clonedPositions.get(i + 1);

				if (!(position.getCoordinates().equals(previous.getCoordinates())
						&& position.getCoordinates().equals(next.getCoordinates())
						&& Float.floatToIntBits(position.getAccuracy()) == Float.floatToIntBits(previous.getAccuracy())
						&& Float.floatToIntBits(position.getAccuracy()) == Float.floatToIntBits(next.getAccuracy())
						&& position.getTimeStamp().getMillis() - previous.getTimeStamp().getMillis() < MAX_TIME_DELTA
						&& next.getTimeStamp().getMillis() - position.getTimeStamp().getMillis() < MAX_TIME_DELTA)) {
					aPositions.add(position);
				}
			}

			if (clonedPositions.size() > 1) {
				aPositions.add(clonedPositions.get(clonedPositions.size() - 1));
			}
		}

		logger.info("Removed " + (clonedPositions.size() - aPositions.size()) + " irrelevant positions");
	}
}
