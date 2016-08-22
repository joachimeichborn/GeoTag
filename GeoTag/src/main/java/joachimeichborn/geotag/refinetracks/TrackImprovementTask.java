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

public interface TrackImprovementTask {
	/**
	 * Check the improvement parameters whether the task should be executed
	 * 
	 * @param aPositions
	 * @return true if processing should be performed, false if not
	 */
	boolean isEnabled();

	/**
	 * Trigger processing of the given positions
	 * 
	 * @param aPositions
	 *            The positions that should be processed
	 */
	void execute(final List<PositionData> aPositions);
}
