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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import joachimeichborn.geotag.LifeCycleManager;
import net.miginfocom.swt.MigLayout;

public class GeneralPreferences extends PreferencePage {
	public static final String BACKUP = "backup_pictures";
	public static final String DB_MAX_ENTRIES = "db_max_entries";

	private static final Pattern POSITIVE_INTEGER = Pattern.compile("^[1-9]\\d*$");
	private static final String TITLE = "General";
	private static final boolean DO_BACKUP_FALLBACK = true;
	private static final int MAX_ENTRIES_FALLBACK = 20_000;

	private final IEclipsePreferences preferences;
	private final IEclipsePreferences defaultPreferences;
	private Button doBackup;
	private Text dbMaxEntries;
	private boolean dbMaxEntriesValid = true;

	public GeneralPreferences(final IEclipsePreferences aPreferences) {
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

		doBackup = new Button(composite, SWT.CHECK);
		doBackup.setText("Backup images before changing meta data");
		doBackup.setSelection(
				preferences.getBoolean(BACKUP, defaultPreferences.getBoolean(BACKUP, DO_BACKUP_FALLBACK)));
		doBackup.setLayoutData("span 2");

		new Label(composite, SWT.LEFT).setText("Cache size (pictures):");
		dbMaxEntries = new Text(composite, SWT.BORDER);
		dbMaxEntries.setText(String.valueOf(
				preferences.getInt(DB_MAX_ENTRIES, defaultPreferences.getInt(DB_MAX_ENTRIES, MAX_ENTRIES_FALLBACK))));
		dbMaxEntries.setLayoutData("growx,pushx");
		dbMaxEntries.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (POSITIVE_INTEGER.matcher(dbMaxEntries.getText()).matches()) {
					setTitle(TITLE);
					dbMaxEntriesValid = true;
				} else {
					setTitle("Cache size must be a positive integer");
					dbMaxEntriesValid = false;
				}

				updateValidity();
			}
		});

		return composite;
	}

	private void updateValidity() {
		setValid(dbMaxEntriesValid);
	}

	public boolean performOk() {
		if (doBackup != null) {
			preferences.putBoolean(BACKUP, doBackup.getSelection());
		}
		if (dbMaxEntries != null) {
			preferences.putInt(DB_MAX_ENTRIES, Integer.valueOf(dbMaxEntries.getText()));
		}

		return true;
	}

	@Override
	public void performDefaults() {
		doBackup.setSelection(defaultPreferences.getBoolean(BACKUP, DO_BACKUP_FALLBACK));
		dbMaxEntries.setText(String.valueOf(defaultPreferences.getInt(DB_MAX_ENTRIES, MAX_ENTRIES_FALLBACK)));

		super.performDefaults();
	}
}
