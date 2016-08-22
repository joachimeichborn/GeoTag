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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import joachimeichborn.geotag.io.kml.KmlReader;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.model.TracksRepo;

public class OpenTracksHandler {
	private static final Logger logger = Logger.getLogger(OpenTracksHandler.class.getSimpleName());

	@Execute
	public static void execute(final Shell aShell) {
		final FileDialog openDialog = new FileDialog(aShell, SWT.MULTI | SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.kml;*.Kml;*.KML;", "*.*" });
		openDialog.setFilterNames(new String[] { "KML files", "All files" });
		openDialog.setText("Open Tracks");
		openDialog.setFilterPath(System.getProperty("user.home"));

		if (openDialog.open() != null) {
			final String[] fileNames = openDialog.getFileNames();
			final String path = openDialog.getFilterPath();

			logger.info("Reading " + fileNames.length + " tracks from " + path + " ...");
			openTracks(path, fileNames);
		}
	}

	public static void openTracks(final String aPath, final String[] aFiles) {
		final TracksRepo tracksRepo = TracksRepo.getInstance();

		final Job job = new Job("Reading tracks") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Reading " + aFiles.length + " tracks", aFiles.length);

				for (final String file : aFiles) {
					final Path trackFile = Paths.get(aPath, file);
					final KmlReader parser = new KmlReader(trackFile);
					final Track track = parser.read();
					tracksRepo.addTrack(track);

					aMonitor.worked(1);
				}
				
				aMonitor.done();
				logger.info("Reading " + aFiles.length + " tracks completed");
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
