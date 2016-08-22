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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.ui.parts.MapView.ZoomMode;
import net.miginfocom.swt.MigLayout;

public class MapPreferences extends PreferencePage {
	public static final String THUMBNAIL_SIZE = "map_thumbnail_size";
	public static final String ZOOM_MODE = "map_zoom_mode";

	private static final Pattern POSITIVE_INTEGER = Pattern.compile("^[1-9]\\d*$");
	private static final String TITLE = "Map";
	private static final int THUMBNAIL_SIZE_FALLBACK = 150;
	private static final String ZOOM_MODE_FALLBACK = ZoomMode.LATEST_SELECTION.getDisplayName();

	private final IEclipsePreferences preferences;
	private final IEclipsePreferences defaultPreferences;
	private Text thumbnailSize;
	private boolean thumbnailSizeValid = true;
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

		new Label(composite, SWT.LEFT).setText("Thumbnail size:");
		thumbnailSize = new Text(composite, SWT.BORDER);
		thumbnailSize.setText(String.valueOf(preferences.getInt(THUMBNAIL_SIZE,
				defaultPreferences.getInt(THUMBNAIL_SIZE, THUMBNAIL_SIZE_FALLBACK))));
		thumbnailSize.setLayoutData("growx,pushx");
		thumbnailSize.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (POSITIVE_INTEGER.matcher(thumbnailSize.getText()).matches()) {
					setTitle(TITLE);
					thumbnailSizeValid = true;
				} else {
					setTitle("Thumbnail size must be a positive integer");
					thumbnailSizeValid = false;
				}

				updateValidity();
			}
		});

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

	private void updateValidity() {
		setValid(thumbnailSizeValid);
	}

	public boolean performOk() {
		if (thumbnailSize != null) {
			preferences.putInt(THUMBNAIL_SIZE, Integer.valueOf(thumbnailSize.getText()));
		}

		if (modes != null) {
			preferences.put(ZOOM_MODE, modes.getItem(modes.getSelectionIndex()));
		}

		return true;
	}

	@Override
	public void performDefaults() {
		thumbnailSize.setText(String.valueOf(defaultPreferences.getInt(THUMBNAIL_SIZE, THUMBNAIL_SIZE_FALLBACK)));

		super.performDefaults();
	}
}
