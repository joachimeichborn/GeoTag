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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class PicturesRepo implements PropertyChangeListener {
	private static final Logger LOGGER = Logger.getLogger(PicturesRepo.class.getSimpleName());
	
	public static final String PICTURES_PROPERTY = "pictures";

	private final Map<String, Picture> pictures;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public PicturesRepo() {
		LOGGER.fine("Constructing pictures repo");
		
		pictures = Collections.synchronizedMap(new LinkedHashMap<>());
	}

	public void addPicture(final Picture aPicture) {
		LOGGER.fine("Adding " + aPicture.getFile() + " to picture storage");

		pictures.put(aPicture.getFile().toString(), aPicture);

		propertyChangeSupport.firePropertyChange(PICTURES_PROPERTY, null, null);
	}

	public void removePictures(final List<Picture> aPicturesToRemove) {
		LOGGER.fine("Removing " + aPicturesToRemove.size() + " pictures from picture storage");
		for (final Picture picture : aPicturesToRemove) {
			if (pictures.remove(picture.getFile().toString()) == null) {
				LOGGER.fine("Could not remove picture " + picture);
			}
		}
		propertyChangeSupport.firePropertyChange(PICTURES_PROPERTY, null, null);
	}

	public List<Picture> getPictures() {
		final List<Picture> pictureList = new LinkedList<>();
		synchronized (pictures) {
			pictureList.addAll(pictures.values());
		}
		return pictureList;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent aEvent) {
		propertyChangeSupport.firePropertyChange(aEvent.getPropertyName(), aEvent.getOldValue(), aEvent.getNewValue());
	}

	public void addPropertyChangeListener(final String aPropertyName, final PropertyChangeListener aListener) {
		propertyChangeSupport.addPropertyChangeListener(aPropertyName, aListener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener aListener) {
		propertyChangeSupport.removePropertyChangeListener(aListener);
	}
}
