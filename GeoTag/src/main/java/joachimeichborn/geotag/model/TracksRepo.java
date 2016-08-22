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

public class TracksRepo implements PropertyChangeListener {
	public static final String TRACKS_PROPERTY = "tracks";

	private static final TracksRepo INSTANCE = new TracksRepo();
	private static final Logger logger = Logger.getLogger(TracksRepo.class.getSimpleName());

	private final Map<String, Track> tracks;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private TracksRepo() {
		tracks = Collections.synchronizedMap(new LinkedHashMap<>());
	}

	public static TracksRepo getInstance() {
		return INSTANCE;
	}

	public void addTrack(final Track aTrack) {
		logger.fine("Adding track " + aTrack.getFile() + " to track storage");
		synchronized (tracks) {
			tracks.put(aTrack.getFile().toString(), aTrack);
		}
		propertyChangeSupport.firePropertyChange(TRACKS_PROPERTY, null, null);
	}

	public void removeTracks(final List<Track> aTracksToRemove) {
		logger.fine("Removing " + aTracksToRemove + " tracks from track storage");
		for (final Track track : aTracksToRemove) {
			final Track removedTrack;
			synchronized (tracks) {
				removedTrack = tracks.remove(track.getFile().toString());
			}
			if (removedTrack == null) {
				logger.fine("Could not remove track " + track);
			}
		}
		propertyChangeSupport.firePropertyChange(TRACKS_PROPERTY, null, null);
	}

	public List<Track> getTracks() {
		final List<Track> tracksList = new LinkedList<>();
		synchronized (tracks) {
			tracksList.addAll(tracks.values());
		}
		return tracksList;
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
