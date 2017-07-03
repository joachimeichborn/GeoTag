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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import joachimeichborn.geotag.misc.ColorPreviewImageGenerator;
import joachimeichborn.geotag.model.Track;

public class TrackViewerObservableLabelProvider extends ObservableMapLabelProvider {
	public static final String NAME_COLUMN = "Name";
	public static final String POSITION_COUNT_COLUMN = "# Positions";
	public static final String COLOR_COLUMN = "Color";

	private final int nameColumnIndex;
	private final int positionCountColumnIndex;
	private final int colorColumnIndex;
	private final ColorPreviewImageGenerator colorPreviewGenerator;

	public TrackViewerObservableLabelProvider(final IObservableMap[] aAttributeMaps, final List<String> aColumns) {
		this(aAttributeMaps, aColumns, null);
	}

	public TrackViewerObservableLabelProvider(final IObservableMap[] aAttributeMaps, final List<String> aColumns,
			final ColorPreviewImageGenerator aColorPreviewGenerator) {
		super(aAttributeMaps);

		nameColumnIndex = aColumns.indexOf(NAME_COLUMN);
		positionCountColumnIndex = aColumns.indexOf(POSITION_COUNT_COLUMN);
		colorColumnIndex = aColumns.indexOf(COLOR_COLUMN);

		colorPreviewGenerator = aColorPreviewGenerator;
	}

	@Override
	public String getColumnText(final Object aElement, final int aColumnIndex) {
		if (aElement == null) {
			return null;
		}

		final Track track = (Track) aElement;

		if (aColumnIndex == nameColumnIndex) {
			return track.getFile().getFileName().toString();
		} else if (aColumnIndex == positionCountColumnIndex) {
			return String.valueOf(track.getPositions().size());
		} else if (aColumnIndex == colorColumnIndex) {
			if (colorPreviewGenerator == null) {
				final RGB color = track.getColor();
				return "(" + color.red + "," + color.green + "," + color.red + ")";
			} else {
				return "";
			}
		} else {
			return super.getColumnText(aElement, aColumnIndex);
		}
	}

	@Override
	public Image getColumnImage(final Object aElement, final int aColumnIndex) {
		if (aElement == null) {
			return null;
		}

		final Track track = (Track) aElement;

		if (aColumnIndex == colorColumnIndex && colorPreviewGenerator != null) {
			return colorPreviewGenerator.getColorPreview(track.getColor());
		} else {
			return super.getColumnImage(aElement, aColumnIndex);
		}
	}
}
