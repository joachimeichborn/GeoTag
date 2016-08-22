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

import java.util.LinkedList;
import java.util.List;

import joachimeichborn.geotag.model.PositionData;

/**
 * Filter positions based on the distances between a given node and it's
 * adjacent nodes compared to the distance between the adjacent node. Filtered
 * positions are deleted from the list of positions
 * 
 * @author Joachim von Eichborn
 */
public class FilterPositionsByPairwiseDistanceTask extends AbstractTrackImprovementTask {

	public FilterPositionsByPairwiseDistanceTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	/**
	 * Check for every position, if it should be filtered out based on the
	 * distances to its adjacent positions in relation to the distance between
	 * them.<br>
	 * If the distance of a position to the previous and to the next positions
	 * is large compared to the distance between these positions, the position
	 * is considered to be an outlier and is removed.
	 */
	@Override
	void process(final List<PositionData> aPositions) {
		logger.fine("Position filtering by pairwise distances activated");

		final List<PositionData> clonedPositions = new LinkedList<PositionData>(aPositions);

		for (int i = 1; i < clonedPositions.size() - 1; i++) {
			final PositionData previous = clonedPositions.get(i - 1);
			final PositionData current = clonedPositions.get(i);
			final List<PositionData> identicalPositions = getConsecutiveIdenticalPositions(i, clonedPositions);
			final PositionData next = clonedPositions.get(i + identicalPositions.size());

			if (previous.getCoordinates().equals(next.getCoordinates())) {
				// do not filter positions if the flanking positions are
				// identical because this might be a star-like pattern
				continue;
			}

			double completeDistance = previous.getCoordinates().distanceTo(next.getCoordinates());
			double partOne = previous.getCoordinates().distanceTo(current.getCoordinates());
			double partTwo = next.getCoordinates().distanceTo(current.getCoordinates());
			if (completeDistance * options.getDistanceFactor() < partOne
					&& completeDistance * options.getDistanceFactor() < partTwo) {
				logger.fine("Removing filtered position " + current);
				for (final PositionData position : identicalPositions) {
					aPositions.remove(position);
				}
			}
		}

		logger.info("Filtered " + (clonedPositions.size() - aPositions.size()) + " positions by pairwise distances");
	}

	private List<PositionData> getConsecutiveIdenticalPositions(final int aIndex, final List<PositionData> aPositions) {
		final List<PositionData> identicalPositions = new LinkedList<>();

		final PositionData current = aPositions.get(aIndex);
		identicalPositions.add(current);

		int i = 1;
		while (aIndex + i < aPositions.size() - 1
				&& current.getCoordinates().equals(aPositions.get(aIndex + i).getCoordinates())) {
			identicalPositions.add(aPositions.get(aIndex + i));
			i++;
		}

		return identicalPositions;
	}

	@Override
	public boolean isEnabled() {
		return options.getFilterByPairwiseDistance();
	}
}
