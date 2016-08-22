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

package joachimeichborn.geotag.handlers;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.swt.widgets.Shell;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.ui.preferences.PreferencesDialog;

public class ShowPreferencesHandler {
	@Execute
	public static void execute(final Shell aShell,
			@Preference(nodePath = LifeCycleManager.PREFERENCES_NODE) final IEclipsePreferences aPreferences) {
		final PreferencesDialog dialog = new PreferencesDialog(aPreferences);
		dialog.showDialog();
	}
}
