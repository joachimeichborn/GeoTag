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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import joachimeichborn.geotag.io.jpeg.PictureMetadataReader;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PicturesRepo;

public class OpenPicturesHandler {
	private static final Logger logger = Logger.getLogger(OpenPicturesHandler.class.getSimpleName());

	@Execute
	public static void execute(final Shell aShell) {
		final FileDialog openDialog = new FileDialog(aShell, SWT.MULTI | SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.jpg;*.Jpg;*.JPG;*.jpeg;*.Jpeg;*.JPEG;", "*.*" });
		openDialog.setFilterNames(new String[] { "Picture files", "All files" });
		openDialog.setText("Open Pictures");
		openDialog.setFilterPath(System.getProperty("user.home"));

		if (openDialog.open() != null) {
			final String[] fileNames = openDialog.getFileNames();
			final String path = openDialog.getFilterPath();

			logger.info("Reading " + fileNames.length + " pictures from " + path + " ...");
			openPictures(path, fileNames);
		}
	}

	private static void openPictures(final String aPath, final String[] aFiles) {
		final List<Path> pictureFiles = new LinkedList<>();

		for (final String file : aFiles) {
			pictureFiles.add(Paths.get(aPath, file));
		}

		openPictures(pictureFiles);
	}

	public static void openPictures(final List<Path> aFiles) {
		final PicturesRepo picturesRepo = PicturesRepo.getInstance();

		final Job job = new Job("Reading pictures") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Reading " + aFiles.size() + " pictures", aFiles.size());

				int cores = 2 * Runtime.getRuntime().availableProcessors();
				logger.fine("Using " + cores + " cores for loading pictures");

				final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(cores, cores, 0L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>());

				for (final Path file : aFiles) {
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							final PictureMetadataReader metadata = new PictureMetadataReader(file);
							final String time = metadata.getTime();
							final Coordinates coordinates = metadata.getCoordinates();
							final Geocoding geoCoding = metadata.getGeocoding();
							final Picture picture = new Picture(file, time, coordinates, geoCoding);
							picturesRepo.addPicture(picture);

							aMonitor.worked(1);
						}
					});
				}
				
				threadPool.shutdown();
				try {
					threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					logger.log(Level.FINE, "Waiting for pictures to be loaded was interrupted", e);
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}

				aMonitor.done();
				logger.info("Reading " + aFiles.size() + " pictures completed");
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
