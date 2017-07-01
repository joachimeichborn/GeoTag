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

package joachimeichborn.geotag.ui.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.ui.parts.MapView.ZoomMode;
import net.miginfocom.swt.MigLayout;

public class MapPreferences extends PreferencePage {
	public static final String ZOOM_MODE = "map_zoom_mode";

	private static final String TITLE = "Map";
	private static final String ZOOM_MODE_FALLBACK = ZoomMode.LATEST_SELECTION.getDisplayName();

	private final IEclipsePreferences preferences;
	private final IEclipsePreferences defaultPreferences;
	private Combo modes;

	public MapPreferences(final IEclipsePreferences aPreferences) {
		super(TITLE);
		preferences = aPreferences;
		defaultPreferences = DefaultScope.INSTANCE.getNode(LifeCycleManager.PREFERENCES_NODE);
	}

	/**
	 * Creates the controls for this page
	 */
	protected Control createContents(final Composite aParent) {
		final Composite composite = new Composite(aParent, SWT.NONE);
		composite.setLayout(new MigLayout("wrap 2"));

		final String selectedZoomModeName = preferences.get(ZOOM_MODE,
				defaultPreferences.get(ZOOM_MODE, ZOOM_MODE_FALLBACK));

		new Label(composite, SWT.LEFT).setText("Zoom map based on:");
		modes = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.SIMPLE | SWT.READ_ONLY);
		int count = 0;
		for (final ZoomMode mode : ZoomMode.values()) {
			final String displayName = mode.getDisplayName();
			modes.add(displayName);
			if (displayName.equals(selectedZoomModeName)) {
				modes.select(count);
			}
			count++;
		}

		return composite;
	}

	public boolean performOk() {
		if (modes != null) {
			preferences.put(ZOOM_MODE, modes.getItem(modes.getSelectionIndex()));
		}

		return true;
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
	}
}
