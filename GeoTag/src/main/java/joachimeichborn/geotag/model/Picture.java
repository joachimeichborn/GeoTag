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

/**
 * Immutable representation of a picture
 * 
 * @author Joachim von Eichborn
 */
public class Picture implements PropertyChangeListener {
	public static final String FILE_PROPERTY = "file";
	public static final String TIME_PROPERTY = "time";
	public static final String COORDINATES_PROPERTY = "coordinates";
	public static final String GEOCODING_PROPERTY = "geocoding";

	private Path file;
	private String time;
	private Coordinates coordinates;
	private Geocoding geocoding;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public Picture(final Path aFile, final String aTime, final Coordinates aCoordinates, final Geocoding aGeoCoding) {
		file = aFile;
		time = aTime;
		coordinates = aCoordinates;
		geocoding = aGeoCoding;
	}

	public Path getFile() {
		return file;
	}

	public String getTime() {
		return time;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public Geocoding getGeocoding() {
		return geocoding;
	}

	public void addPropertyChangeListener(final String aPropertyName, final PropertyChangeListener aListener) {
		propertyChangeSupport.addPropertyChangeListener(aPropertyName, aListener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener aListener) {
		propertyChangeSupport.removePropertyChangeListener(aListener);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent aEvent) {
		propertyChangeSupport.firePropertyChange(aEvent.getPropertyName(), aEvent.getOldValue(), aEvent.getNewValue());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((geocoding == null) ? 0 : geocoding.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		Picture other = (Picture) aObj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (geocoding == null) {
			if (other.geocoding != null)
				return false;
		} else if (!geocoding.equals(other.geocoding))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Picture [file=" + file + ", time=" + time + ", coordinates=" + coordinates + ", geoCoding=" + geocoding
				+ ", propertyChangeSupport=" + propertyChangeSupport + "]";
	}
}
