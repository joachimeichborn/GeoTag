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

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import joachimeichborn.geotag.model.PositionData;

public abstract class AbstractTrackImprovementTask implements TrackImprovementTask {
	private final static Logger logger = Logger.getLogger(AbstractTrackImprovementTask.class.getSimpleName());

	final ImproveTrackOptions options;

	public AbstractTrackImprovementTask(final ImproveTrackOptions aOptions) {
		options = aOptions;
	}

	public void execute(final List<PositionData> aPositions) {
		logger.fine("Starting improvement task " + this.getClass().getSimpleName());

		if (isEnabled()) {
			Collections.sort(aPositions);
			process(aPositions);
		}

		logger.fine("Finished improvement task " + this.getClass().getSimpleName());
	}

	/**
	 * Process the given positions to fulfill the task's purpose.
	 * 
	 * @param aPositions
	 *            The positions that should be processed
	 */
	abstract void process(List<PositionData> aPositions);
}
