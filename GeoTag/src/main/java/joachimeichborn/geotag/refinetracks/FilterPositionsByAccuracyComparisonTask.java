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

import java.util.List;

import joachimeichborn.geotag.model.PositionData;

/**
 * Filter positions based on the accuracy information that are recorded with
 * each position. Positions are not deleted from the list but replaced with
 * adjacent positions that are better in terms of accuracy. So the resulting
 * position list will have the same number of entries as the initial position
 * list.
 * 
 * @author Joachim von Eichborn
 */
public class FilterPositionsByAccuracyComparisonTask extends AbstractTrackImprovementTask {
	static final String REPLACED_POSITION_NAME = "replacement dummy";

	public FilterPositionsByAccuracyComparisonTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	@Override
	public boolean isEnabled() {
		return options.getFilterByAccuracyComparison();
	}

	/**
	 * Check whether the first position is a valid replacement for the second
	 * one.<br>
	 * The first position is considered to be a valid replacement if a circle
	 * drawn about the position with the radius of its accuracy lies completely
	 * within the corresponding circle of the second position.
	 * 
	 * @param aPositionA
	 *            The first position
	 * @param aPositionB
	 *            The second position
	 * @return True if the first position is a valid replacement for the second
	 *         one, false otherwise
	 */
	private boolean isPositionAReplacementForPositionB(final PositionData aPositionA, final PositionData aPositionB) {
		final double distanceBetweenPositions = aPositionA.getCoordinates().distanceTo(aPositionB.getCoordinates());

		assert distanceBetweenPositions >= 0 : "Distance between " + aPositionA + " and " + aPositionB + " is negative";

		if (aPositionA.getCoordinates().distanceTo(aPositionB.getCoordinates()) + aPositionA.getAccuracy() < aPositionB
				.getAccuracy()) {
			logger.fine("Replacing, distance: " + distanceBetweenPositions + ", accuracy a: " + aPositionA.getAccuracy()
					+ ", accuracy b: " + aPositionB.getAccuracy());
			return true;
		}

		return false;
	}

	/**
	 * Check for the two positions given by the two indexed if one of them is a
	 * valid replacement for the other one. If so, the replacement is performed.
	 * 
	 * @param indexFirstPosition
	 *            The index in the positions list of the first position
	 * @param indexSecondPosition
	 *            The index in the positions list of the second position
	 * @param aPositions
	 *            The list of positions
	 * @return True if a replacement has been performed, false otherwise
	 */
	private boolean replaceIfBetter(final int indexFirstPosition, final int indexSecondPosition,
			final List<PositionData> aPositions) {
		final PositionData firstPosition = aPositions.get(indexFirstPosition);
		final PositionData secondPosition = aPositions.get(indexSecondPosition);
		if (isPositionAReplacementForPositionB(firstPosition, secondPosition)) {
			aPositions.set(indexSecondPosition, new PositionData(firstPosition.getCoordinates(),
					secondPosition.getTimeStamp(), REPLACED_POSITION_NAME, firstPosition.getAccuracy()));
			return true;
		}

		return false;
	}

	/**
	 * Check for every position, if it could be replaced with a better (more
	 * accurate) adjacent positions. This is repeated until now positions are
	 * replaced any more. <br>
	 * Position adjacency is meant in a timely matter, a position can only be
	 * replaced with the previous or next position. If a position is replaced,
	 * the new position has the time stamp of the one that is replaced but the
	 * coordinates and accuracy of the position that is the template for the
	 * replacement.
	 */
	@Override
	void process(final List<PositionData> aPositions) {
		logger.fine("Position filtering by location accuracy activated");

		boolean replacementOccured = true;
		int replacementCount = 0;

		while (replacementOccured) {
			replacementOccured = false;
			for (int i = 0; i < aPositions.size() - 1; i++) {
				if (replaceIfBetter(i, i + 1, aPositions) || replaceIfBetter(i + 1, i, aPositions)) {
					replacementOccured = true;
					replacementCount++;
				}
			}
		}

		logger.info("Replaced " + replacementCount + " positions by location accuracy");
	}
}
