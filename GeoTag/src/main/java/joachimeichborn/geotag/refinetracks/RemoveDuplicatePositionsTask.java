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
 * Remove duplicate positions from the list
 * 
 * @author Joachim von Eichborn
 */
public class RemoveDuplicatePositionsTask extends AbstractTrackImprovementTask {
	private final static Logger logger = Logger.getLogger(RemoveDuplicatePositionsTask.class.getSimpleName());
	
	public RemoveDuplicatePositionsTask(final ImproveTrackOptions aOptions) {
		super(aOptions);
	}

	/**
	 * Remove duplicate positions from the list.<br>
	 * 
	 * Positions are considered to be duplicates if
	 * {@link PositionData#equals(Object)} returns true.
	 */
	@Override
	void process(List<PositionData> aPositions) {
		logger.fine("Duplicate removal activated");

		int removedDuplicatesCount = 0;

		if (!aPositions.isEmpty()) {
			final Iterator<PositionData> iter = aPositions.iterator();
			PositionData lastPosition = iter.next();
			PositionData currentPosition = null;
			while (iter.hasNext()) {
				currentPosition = iter.next();
				if (currentPosition.equals(lastPosition)) {
					iter.remove();
					removedDuplicatesCount++;
					logger.fine("Removing duplicate position " + currentPosition);
				}
				lastPosition = currentPosition;
			}
		}

		logger.info("Removed " + removedDuplicatesCount + " duplicate positions");
	}

	@Override
	public boolean isEnabled() {
		return options.getRemoveDuplicates();
	}
}
