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

import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.math.IntMath;

import joachimeichborn.geotag.model.PositionData;

/**
 * Replace positions based on the accuracy information that are recorded with
 * each position. Positions are not deleted from the list but replaced with
 * adjacent positions that are better in terms of accuracy. So the resulting
 * position list will have the same number of entries as the initial position
 * list.
 * 
 * @author Joachim von Eichborn
 */
public class ReplacePositionsByAccuracyComparisonTask extends AbstractTrackImprovementTask {
	private final static Logger logger = Logger.getLogger(ReplacePositionsByAccuracyComparisonTask.class.getSimpleName());
	static final String REPLACED_POSITION_NAME = "Accuracy replacement";

	public ReplacePositionsByAccuracyComparisonTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	@Override
	public boolean isEnabled() {
		return options.getReplaceByAccuracyComparison();
	}

	/**
	 * Check whether the first position is a valid replacement for the second
	 * one.<br>
	 * The first position is considered to be a valid replacement if a circle
	 * drawn about the position with the radius of its accuracy lies completely
	 * within the corresponding circle of the second position.
	 * 
	 * @param aFirstPosition
	 *            The first position
	 * @param aSecondPosition
	 *            The second position
	 * @return True if the first position is a valid replacement for the second
	 *         one, false otherwise
	 */
	private boolean isFirstPositionReplacementForSecondPosition(final PositionData aFirstPosition, final PositionData aSecondPosition) {
		final double distanceBetweenPositions = aFirstPosition.getCoordinates().distanceTo(aSecondPosition.getCoordinates());

		if (distanceBetweenPositions + aFirstPosition.getAccuracy() < aSecondPosition.getAccuracy()) {
			logger.fine("Replacing position with accuracy " + aSecondPosition.getAccuracy() + " by a position with accuracy " + aFirstPosition.getAccuracy()
					+ " which is " + distanceBetweenPositions + " away");
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
	private boolean replaceIfBetter(final int indexFirstPosition, final int indexSecondPosition, final List<PositionData> aPositions) {
		final PositionData firstPosition = aPositions.get(indexFirstPosition);
		final PositionData secondPosition = aPositions.get(indexSecondPosition);
		if (isFirstPositionReplacementForSecondPosition(firstPosition, secondPosition)) {
			aPositions.set(indexSecondPosition,
					new PositionData(firstPosition.getCoordinates(), secondPosition.getTimeStamp(), REPLACED_POSITION_NAME, firstPosition.getAccuracy()));
			return true;
		}

		return false;
	}

	/**
	 * Check for every position, if it could be replaced with a better (more
	 * accurate) adjacent positions. This is repeated until no positions are
	 * replaced any more. <br>
	 * Position adjacency is meant in a timely matter, a position can only be
	 * replaced with the previous or next position. If a position is replaced,
	 * the new position has the time stamp of the one that is replaced but the
	 * coordinates and accuracy of the position that is the template for the
	 * replacement.
	 */
	@Override
	void process(final List<PositionData> aPositions) {
		logger.fine("Position replacement by location accuracy activated");

		int chunks = Runtime.getRuntime().availableProcessors();
		logger.fine("Splitting positions in " + chunks + " chunks for replacement by location accuracy");

		final ExecutorService threadPool = Executors.newCachedThreadPool();

		boolean replacementOccured = true;
		int replacementCount = 0;

		final int chunkSize = IntMath.divide(aPositions.size(), chunks, RoundingMode.CEILING);

		while (replacementOccured) {
			replacementOccured = false;
			final List<Future<Integer>> futures = new LinkedList<>();
			for (int i = 0; i < chunks; i++) {
				final int startIndex = i * chunkSize;
				final int endIndex = Math.min(startIndex + chunkSize, aPositions.size() - 1);
				futures.add(threadPool.submit(new Callable<Integer>() {

					@Override
					public Integer call() throws Exception {
						logger.fine("Computing replacements for indices " + startIndex + " to " + endIndex);
						int replacementCount = 0;

						for (int i = startIndex; i < endIndex; i++) {
							if (replaceIfBetter(i, i + 1, aPositions) || replaceIfBetter(i + 1, i, aPositions)) {
								replacementCount++;
							}
						}

						return replacementCount;
					}
				}));

			}

			for (final Future<Integer> future : futures) {
				int newReplacementCount = 0;
				try {
					newReplacementCount = future.get();
				} catch (InterruptedException | ExecutionException e) {
					logger.log(Level.WARNING, "An error occured while computing the improved track", e);
					aPositions.clear();
				}

				if (newReplacementCount > 0)
					replacementOccured = true;
				replacementCount += newReplacementCount;
			}
		}

		logger.info("Replaced " + replacementCount + " positions by location accuracy");
	}
}
