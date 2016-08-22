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

package joachimeichborn.geotag.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import joachimeichborn.geotag.utils.ColorGenerator;

/**
 * Representation of a track which is basically a list of positions. The
 * positions in a track are guaranteed to be ordered and immutable
 * 
 * @author Joachim von Eichborn
 */
public class Track implements PropertyChangeListener {
	public static final String FILE_PROPERTY = "file";
	public static final String POSITIONS_PROPERTY = "positions";
	public static final String COLOR_PROPERTY = "color";

	private final Path file;
	private final List<PositionData> positions;
	private RGB color;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public Track(final Path aFile, final List<PositionData> aPositions) {
		this(aFile, aPositions, ColorGenerator.getInstance().getNextColor());
	}

	public Track(final Path aFile, final List<PositionData> aPositions, final RGB aColor) {
		file = aFile;
		positions = new ArrayList<>(aPositions);
		Collections.sort(positions);
		color = aColor;
	}

	public void addPropertyChangeListener(final String aPropertyName, final PropertyChangeListener aListener) {
		propertyChangeSupport.addPropertyChangeListener(aPropertyName, aListener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener aListener) {
		propertyChangeSupport.removePropertyChangeListener(aListener);
	}

	public Path getFile() {
		return file;
	}

	public List<PositionData> getPositions() {
		return Collections.unmodifiableList(positions);
	}

	public RGB getColor() {
		return color;
	}

	public void setColor(final RGB aColor) {
		propertyChangeSupport.firePropertyChange(COLOR_PROPERTY, color, color = aColor);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent aEvent) {
		propertyChangeSupport.firePropertyChange(aEvent.getPropertyName(), aEvent.getOldValue(), aEvent.getNewValue());
	}

	@Override
	public String toString() {
		return "Track [file=" + file + ", " + positions.size() + " positions, color=" + color + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((positions == null) ? 0 : positions.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object aObj) {
		if (this == aObj)
			return true;
		if (aObj == null)
			return false;
		if (getClass() != aObj.getClass())
			return false;
		Track other = (Track) aObj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (positions == null) {
			if (other.positions != null)
				return false;
		} else if (!positions.equals(other.positions))
			return false;
		return true;
	}
}
