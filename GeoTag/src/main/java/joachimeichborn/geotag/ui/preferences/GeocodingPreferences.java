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
import joachimeichborn.geotag.geocode.GeocodingProvider;
import net.miginfocom.swt.MigLayout;

public class GeocodingPreferences extends PreferencePage {
	public static final String GEOCODING_PROVIDER = "geocoding_provider";

	private static final String TITLE = "Geocoding";
	private static final String GEOCODING_PROVIDER_FALLBACK = GeocodingProvider.MAP_QUEST.getDisplayName();

	private final IEclipsePreferences preferences;
	private final IEclipsePreferences defaultPreferences;

	private Combo providers;

	public GeocodingPreferences(final IEclipsePreferences aPreferences) {
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

		final String selectedProviderName = preferences.get(GEOCODING_PROVIDER,
				defaultPreferences.get(GEOCODING_PROVIDER, GEOCODING_PROVIDER_FALLBACK));

		new Label(composite, SWT.LEFT).setText("Geocoding provider:");
		providers = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.SIMPLE | SWT.READ_ONLY);
		int count = 0;
		for (final GeocodingProvider provider : GeocodingProvider.values()) {
			final String displayName = provider.getDisplayName();
			providers.add(displayName);
			if (displayName.equals(selectedProviderName)) {
				providers.select(count);
			}
			count++;
		}

		return composite;
	}

	public boolean performOk() {
		if (providers != null) {
			preferences.put(GEOCODING_PROVIDER, providers.getItem(providers.getSelectionIndex()));
		}

		return true;
	}

	@Override
	public void performDefaults() {
		final String defaultProviderName = defaultPreferences.get(GEOCODING_PROVIDER, GEOCODING_PROVIDER_FALLBACK);

		int count = 0;
		for (final String providerName : providers.getItems()) {
			if (defaultProviderName.equals(providerName)) {
				providers.select(count);
				break;
			}
		}

		super.performDefaults();
	}
}
