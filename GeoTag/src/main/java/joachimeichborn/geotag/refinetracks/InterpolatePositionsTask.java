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

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;

/**
 * Interpolate position coordinates to get a smoother position path. This can
 * help to get rid of small inaccuracies in the recorded positions
 * 
 * @author Joachim von Eichborn
 */
public class InterpolatePositionsTask extends AbstractTrackImprovementTask {
	public InterpolatePositionsTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	public boolean isEnabled() {
		return options.getInterpolatePositions();
	}

	/**
	 * Interpolate the positions present in the position list. Each existing
	 * position is replaced with a corresponding interpolated position.
	 */
	@Override
	void process(final List<PositionData> aPositions) {
		logger.fine("Position interpolation activated");

		if (!aPositions.isEmpty()) {
			final List<PositionData> clonedPositions = new ArrayList<PositionData>(aPositions);
			aPositions.clear();
			aPositions.add(clonedPositions.get(0));

			for (int i = 1; i < clonedPositions.size() - 1; i++) {
				final PositionData position = clonedPositions.get(i);

				final Coordinates iMinus1Coordinates = clonedPositions.get(i - 1).getCoordinates();
				final Coordinates iCoordinates = clonedPositions.get(i).getCoordinates();
				final Coordinates iPlus1Coordinates = clonedPositions.get(i + 1).getCoordinates();
				final double longitude = (iMinus1Coordinates.getLongitude() + iCoordinates.getLongitude()
						+ iPlus1Coordinates.getLongitude()) / 3.0;
				final double latitude = (iMinus1Coordinates.getLatitude() + iCoordinates.getLatitude()
						+ iPlus1Coordinates.getLatitude()) / 3.0;
				final double altitude = (iMinus1Coordinates.getAltitude() + iCoordinates.getAltitude()
						+ iPlus1Coordinates.getAltitude()) / 3.0;

				final Coordinates coordinates = new Coordinates(latitude, longitude, altitude);
				aPositions.add(new PositionData(coordinates, position.getTimeStamp(), position.getName(),
						position.getAccuracy()));
			}

			if (clonedPositions.size() > 1) {
				aPositions.add(clonedPositions.get(clonedPositions.size() - 1));
			}
		}

		logger.info("Interpolated positions");
	}
}
