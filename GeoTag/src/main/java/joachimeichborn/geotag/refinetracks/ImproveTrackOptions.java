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

/**
 * Options for track improvement
 * 
 * @author Joachim von Eichborn
 */
public class ImproveTrackOptions {
	public static class ImproveTrackOptionsBuilder {
		private boolean removeDuplicates = false;
		private boolean filterByPairwiseDistance = false;
		private double distanceFactor = Double.MAX_VALUE;
		private boolean filterByAccuracyComparison = false;
		private boolean filterByAccuracyRadius = false;
		private int radiusThreshold = Integer.MAX_VALUE;
		private boolean removeIrrelevantPositions = false;
		private boolean interpolatePositions = false;

		public ImproveTrackOptionsBuilder setRemoveDuplicates(final boolean aRemoveDuplicates) {
			removeDuplicates = aRemoveDuplicates;
			return this;
		}

		public ImproveTrackOptionsBuilder setInterpolatePositions(final boolean aInterpolatePositions) {
			interpolatePositions = aInterpolatePositions;
			return this;
		}

		public ImproveTrackOptionsBuilder setRemoveIrrelevantPositions(final boolean aRemoveIrrelevantPositions) {
			removeIrrelevantPositions = aRemoveIrrelevantPositions;
			return this;
		}

		public ImproveTrackOptionsBuilder setFilterByPairwiseDistance(final boolean aFilterByPairwiseDistance) {
			filterByPairwiseDistance = aFilterByPairwiseDistance;
			return this;
		}

		public ImproveTrackOptionsBuilder setDistanceFactor(final double aDistanceFactor) {
			distanceFactor = aDistanceFactor;
			return this;
		}

		public ImproveTrackOptionsBuilder setFilterByAccuracyComparison(final boolean aFilterByAccuracyComparison) {
			filterByAccuracyComparison = aFilterByAccuracyComparison;
			return this;
		}

		public ImproveTrackOptionsBuilder setFilterByAccuracyRadius(final boolean aFilterByAccuracyRadius) {
			filterByAccuracyRadius = aFilterByAccuracyRadius;
			return this;
		}

		public ImproveTrackOptionsBuilder setRadiusThreshold(final int aRadiusThreshold) {
			radiusThreshold = aRadiusThreshold;
			return this;
		}

		public ImproveTrackOptions build() {
			if (filterByAccuracyRadius && radiusThreshold == Integer.MAX_VALUE) {
				throw new IllegalStateException("Filtering by accuracy radius is activated but no radius threshold is given");
			}

			if (filterByPairwiseDistance&& distanceFactor== Double.MAX_VALUE) {
				throw new IllegalStateException("Filtering by pairwise distance is activated but no factor is given");
			}
			
			return new ImproveTrackOptions(removeDuplicates, filterByPairwiseDistance, distanceFactor,
					filterByAccuracyComparison, filterByAccuracyRadius, radiusThreshold, removeIrrelevantPositions,
					interpolatePositions);
		}
	}

	private final boolean removeDuplicates;
	private final boolean filterByPairwiseDistance;
	private final double distanceFactor;
	private final boolean filterByAccuracyComparison;
	private final boolean filterByAccuracyRadius;
	private final int radiusThreshold;
	private final boolean removeIrrelevantPositions;
	private final boolean interpolatePositions;

	private ImproveTrackOptions(final boolean aRemoveDuplicates, final boolean aFilterByPairwiseDistance,
			final double aDistanceFactor, final boolean aFilterByAccuracyComparison,
			final boolean aFilterByAccuracyRadius, final int aRadiusThreshold, final boolean aRemoveIrrelevantPositions,
			final boolean aInterpolatePositions) {
		removeDuplicates = aRemoveDuplicates;
		filterByPairwiseDistance = aFilterByPairwiseDistance;
		distanceFactor = aDistanceFactor;
		filterByAccuracyComparison = aFilterByAccuracyComparison;
		filterByAccuracyRadius = aFilterByAccuracyRadius;
		radiusThreshold = aRadiusThreshold;
		removeIrrelevantPositions = aRemoveIrrelevantPositions;
		interpolatePositions = aInterpolatePositions;
	}

	public boolean getRemoveDuplicates() {
		return removeDuplicates;
	}

	public boolean getFilterByPairwiseDistance() {
		return filterByPairwiseDistance;
	}

	public Double getDistanceFactor() {
		return distanceFactor;
	}

	public boolean getFilterByAccuracyComparison() {
		return filterByAccuracyComparison;
	}

	public boolean getFilterByAccuracyRadius() {
		return filterByAccuracyRadius;
	}

	public Integer getRadiusThreshold() {
		return radiusThreshold;
	}

	public boolean getRemoveIrrelevantPositions() {
		return removeIrrelevantPositions;
	}

	public boolean getInterpolatePositions() {
		return interpolatePositions;
	}
}
