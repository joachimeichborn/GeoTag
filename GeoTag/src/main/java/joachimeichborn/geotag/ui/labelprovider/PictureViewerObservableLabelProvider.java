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

package joachimeichborn.geotag.ui.labelprovider;

import java.util.List;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

public class PictureViewerObservableLabelProvider extends ObservableMapLabelProvider {
	private final PictureViewerLabelProvider labelProvider;

	public PictureViewerObservableLabelProvider(IObservableMap[] attributeMaps, final List<String> aColumns) {
		super(attributeMaps);

		labelProvider = new PictureViewerLabelProvider(aColumns);
	}

	@Override
	public String getColumnText(final Object aElement, final int aColumnIndex) {
		final String content = labelProvider.getColumnText(aElement, aColumnIndex);

		if (content != null) {
			return content;
		} else {
			return super.getColumnText(aElement, aColumnIndex);
		}
	}
}
