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

package joachimeichborn.geotag.model.selections;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import joachimeichborn.geotag.model.Picture;

public class PictureSelection implements Selection<Picture> {
	private List<Picture> selection;

	public PictureSelection() {
		selection = Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	public PictureSelection(final IStructuredSelection aSelection) {
		selection = Collections.unmodifiableList((List<Picture>) aSelection.toList());
	}

	@Override
	public List<Picture> getSelection() {
		return selection;
	}

}
