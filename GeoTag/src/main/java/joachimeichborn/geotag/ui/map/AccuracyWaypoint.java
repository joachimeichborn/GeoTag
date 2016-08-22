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

package joachimeichborn.geotag.ui.map;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

public class AccuracyWaypoint implements Waypoint {
	private final DefaultWaypoint waypoint;
	private final int accuracy;

	public AccuracyWaypoint(final GeoPosition aCoordinate, final int aAccuracy) {
		waypoint = new DefaultWaypoint(aCoordinate);
		accuracy = aAccuracy;
	}

	@Override
	public GeoPosition getPosition() {
		return waypoint.getPosition();
	}

	public int getAccuracy() {
		return accuracy;
	}
}
