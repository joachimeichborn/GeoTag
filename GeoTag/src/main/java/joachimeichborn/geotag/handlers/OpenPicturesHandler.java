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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
	private static final class PicturesReader implements Runnable {
		private final IProgressMonitor monitor;
		private final Path pictureFile;

		private PicturesReader(final IProgressMonitor aMonitor, final Path aPictureFile) {
			monitor = aMonitor;
			pictureFile = aPictureFile;
		}

		@Override
		public void run() {
			final PictureMetadataReader metadata = new PictureMetadataReader(pictureFile);
			final String time = metadata.getTime();
			final Coordinates coordinates = metadata.getCoordinates();
			final Geocoding geoCoding = metadata.getGeocoding();
			final Picture picture = new Picture(pictureFile, time, coordinates, geoCoding);
			PicturesRepo.getInstance().addPicture(picture);

			monitor.worked(1);
		}
	}

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
		final Job job = new Job("Reading pictures") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Reading " + aFiles.size() + " pictures", aFiles.size());

				int threads = 2 * Runtime.getRuntime().availableProcessors();
				logger.fine("Using " + threads + " cores for loading pictures");

				final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

				final List<Future<?>> futures = new LinkedList<>();

				for (final Path file : aFiles) {
					futures.add(threadPool.submit(new PicturesReader(aMonitor, file)));
				}

				final IStatus status = waitForAllPicturesToBeRead(futures);

				aMonitor.done();
				return status;
			}

			private IStatus waitForAllPicturesToBeRead(final List<Future<?>> futures) {
				for (final Future<?> future : futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						logger.log(Level.FINE, "Waiting for piture to be loaded was interrupted", e);
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					} catch (ExecutionException e) {
						logger.log(Level.FINE, "Reading picture failed", e);
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					}
				}

				logger.info("Reading " + futures.size() + " pictures completed");
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
