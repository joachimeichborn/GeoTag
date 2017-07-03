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

import java.util.logging.Logger;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenPicturesHandler {
	static final Logger logger = Logger.getLogger(OpenPicturesHandler.class.getSimpleName());

	@Execute
	public static void execute(final Shell aShell, final IEclipseContext aEclipseContext) {
		final FileDialog openDialog = new FileDialog(aShell, SWT.MULTI | SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.jpg;*.Jpg;*.JPG;*.jpeg;*.Jpeg;*.JPEG;", "*.*" });
		openDialog.setFilterNames(new String[] { "Picture files", "All files" });
		openDialog.setText("Open Pictures");
		openDialog.setFilterPath(System.getProperty("user.home"));

		if (openDialog.open() != null) {
			final String[] fileNames = openDialog.getFileNames();
			final String path = openDialog.getFilterPath();

			logger.info("Reading " + fileNames.length + " pictures from " + path + " ...");
			final PictureLoader pictureLoader = new PictureLoader();
			ContextInjectionFactory.inject(pictureLoader, aEclipseContext);
			pictureLoader.openPictures(path, fileNames);
		}
	}
}
