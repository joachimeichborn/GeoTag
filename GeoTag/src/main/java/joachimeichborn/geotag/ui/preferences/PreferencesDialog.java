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

/* 
 * Handler to open up a configured preferences dialog.
 * Written by Brian de Alwis, Manumitting Technologies.
 * Placed in the public domain.
 */
package joachimeichborn.geotag.ui.preferences;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;

public class PreferencesDialog {
	private static final Logger logger = Logger.getLogger(PreferencesDialog.class.getSimpleName());
	private final PreferenceDialog dialog;

	public PreferencesDialog(final IEclipsePreferences aPreferences) {
		final PreferenceManager manager = new PreferenceManager();

		final PreferenceNode general = new PreferenceNode("general", new GeneralPreferences(aPreferences));
		final PreferenceNode map = new PreferenceNode("map", new MapPreferences(aPreferences));
		final PreferenceNode geocoding = new PreferenceNode("geocoding", new GeocodingPreferences(aPreferences));

		manager.addToRoot(general);
		manager.addToRoot(map);
		manager.addToRoot(geocoding);
		dialog = new PreferenceDialog(null, manager);
	}

	public void showDialog() {
		final PreferenceStore store = new PreferenceStore("joachimeichborn.geotag.preferences");
		try {
			store.load();
		} catch (IOException e) {
			logger.fine("Could not load preferences store: " + e.getMessage());
		}
		logger.fine("STORE: " + store);
		dialog.setPreferenceStore(store);
		dialog.open();

		try {
			store.save();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not save preferences: " + e.getMessage(), e);
		}
	}
}
