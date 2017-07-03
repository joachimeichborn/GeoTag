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

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import joachimeichborn.geotag.io.database.DatabaseAccess;
import joachimeichborn.geotag.io.jpeg.PictureMetadataReader;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PicturesRepo;
import joachimeichborn.geotag.preview.PreviewKey;
import joachimeichborn.geotag.preview.PreviewRequester;

public class PictureLoader {
	private static class PicturesReader implements Runnable {
		private final IProgressMonitor monitor;
		private final Path pictureFile;
		private final PicturesRepo picturesRepo;

		public PicturesReader(final IProgressMonitor aMonitor, final Path aPictureFile, final PicturesRepo aPicturesRepo) {
			monitor = aMonitor;
			pictureFile = aPictureFile;
			picturesRepo = aPicturesRepo;
		}

		@Override
		public void run() {
			final PictureMetadataReader metadata = new PictureMetadataReader(pictureFile);
			final String time = metadata.getTime();
			final Coordinates coordinates = metadata.getCoordinates();
			final Geocoding geoCoding = metadata.getGeocoding();
			final Picture picture = new Picture(pictureFile, time, coordinates, geoCoding);
			picturesRepo.addPicture(picture);

			monitor.worked(1);
		}
	}

	@Inject
	private DatabaseAccess dbAccess;
	
	@Inject
	private PicturesRepo picturesRepo;

	void openPictures(final String aPath, final String[] aFiles) {
		final List<Path> pictureFiles = new LinkedList<>();

		for (final String file : aFiles) {
			pictureFiles.add(Paths.get(aPath, file));
		}

		openPictures(pictureFiles);
	}

	public void openPictures(final List<Path> aFiles) {
		final Job job = new Job("Reading pictures") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Reading " + aFiles.size() + " pictures", aFiles.size());

				int threads = 2 * Runtime.getRuntime().availableProcessors();
				OpenPicturesHandler.logger.fine("Using " + threads + " cores for loading pictures");

				final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

				final List<Future<?>> futures = new LinkedList<>();

				for (final Path file : aFiles) {
					futures.add(threadPool.submit(new PicturesReader(aMonitor, file, picturesRepo)));
				}

				final IStatus status = waitForAllPicturesToBeRead(futures);

				aMonitor.done();

				final PreviewRequester previewRepo = new PreviewRequester(dbAccess);
				for (final Path file : aFiles) {
					final PreviewKey key = new PreviewKey(file.toString(), 160, 120);
					previewRepo.triggerPreviewCreation(key);
				}

				OpenPicturesHandler.logger.fine("Triggering preview creation for " + aFiles.size() + " pictures done");

				return status;
			}

			private IStatus waitForAllPicturesToBeRead(final List<Future<?>> futures) {
				for (final Future<?> future : futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						OpenPicturesHandler.logger.log(Level.FINE, "Waiting for piture to be loaded was interrupted", e);
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					} catch (ExecutionException e) {
						OpenPicturesHandler.logger.log(Level.FINE, "Reading picture failed", e);
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					}
				}

				OpenPicturesHandler.logger.info("Reading " + futures.size() + " pictures completed");

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}