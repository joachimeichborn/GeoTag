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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import joachimeichborn.geotag.model.PositionData;

/**
 * Remove positions with an accuracy radius worse than a given threshold from
 * the list
 * 
 * @author Joachim von Eichborn
 */
public class FilterPositionsByAccuracyRadiusTask extends AbstractTrackImprovementTask {
	private final static Logger logger = Logger.getLogger(FilterPositionsByAccuracyRadiusTask.class.getSimpleName());
	
	public FilterPositionsByAccuracyRadiusTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	public boolean isEnabled() {
		return options.getFilterByAccuracyRadius();
	}

	@Override
	void process(final List<PositionData> aPositions) {
		logger.fine("Position filtering by location accuracy activated");

		int removalCount = 0;

		final Iterator<PositionData> iter = aPositions.iterator();
		while (iter.hasNext()) {
			final PositionData position = iter.next();

			if (position.getAccuracy() > options.getRadiusThreshold()) {
				iter.remove();
				removalCount++;
				logger.fine("Removing position " + position);
			}
		}

		logger.info("Removed " + removalCount + " positions with an accuracy radius greater than "
				+ options.getRadiusThreshold());
	}
}
