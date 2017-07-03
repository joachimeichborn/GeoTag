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

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import joachimeichborn.geotag.io.TrackFileFormat;
import joachimeichborn.geotag.io.parser.TrackParser;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.model.TracksRepo;

public class OpenTracksHandler {
	private static final class TrackReader implements Runnable {
		private final IProgressMonitor monitor;
		private final Path trackFile;

		private TrackReader(final IProgressMonitor aMonitor, final Path aTrackFile) {
			monitor = aMonitor;
			trackFile = aTrackFile;
		}

		@Override
		public void run() {
			logger.info("Reading track from " + trackFile.getFileName());

			try {
				final String extension = FilenameUtils.getExtension(trackFile.getFileName().toString());
				final TrackFileFormat format = TrackFileFormat.getByExtension(extension.toLowerCase());

				final TrackParser parser = format.getParser();
				final Track track = parser.read(trackFile);
				if (track != null) {
					TracksRepo.getInstance().addTrack(track);
				}

				logger.info("Completed reading track " + trackFile.getFileName());
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to read track from " + trackFile.getFileName(), e);
			}

			monitor.worked(1);
		}
	}

	private static final Logger logger = Logger.getLogger(OpenTracksHandler.class.getSimpleName());

	@Execute
	public static void execute(final Shell aShell) {
		final FileDialog openDialog = new FileDialog(aShell, SWT.MULTI | SWT.OPEN);
		openDialog.setFilterExtensions(new String[] { "*.kml;*.kmz;*.gpx" });
		openDialog.setFilterNames(new String[] { "Track files (KML, KMZ or GPX)" });
		openDialog.setText("Open Tracks");
		openDialog.setFilterPath(System.getProperty("user.home"));

		if (openDialog.open() != null) {
			final String[] fileNames = openDialog.getFileNames();
			final String path = openDialog.getFilterPath();

			logger.fine("Reading " + fileNames.length + " tracks from " + path + " ...");
			openTracks(path, fileNames);
		}
	}

	public static void openTracks(final String aPath, final String[] aFiles) {
		final Job job = new Job("Reading tracks") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Reading " + aFiles.length + " tracks", aFiles.length);

				int threads = 2 * Runtime.getRuntime().availableProcessors();
				logger.fine("Using " + threads + " cores for loading tracks");

				final ExecutorService threadPool =Executors.newFixedThreadPool(threads);

				final List<Future<?>> futures = new LinkedList<>();

				for (final String file : aFiles) {
					final Path trackFile = Paths.get(aPath, file);
					futures.add(threadPool.submit(new TrackReader(aMonitor, trackFile)));
				}

				final IStatus status = waitForAllTracksToBeRead(futures);

				aMonitor.done();
				return status;
			}

			private IStatus waitForAllTracksToBeRead(final List<Future<?>> futures) {
				for (final Future<?> future : futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						logger.log(Level.FINE, "Waiting for track to be loaded was interrupted", e);
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					} catch (ExecutionException e) {
						logger.log(Level.FINE, "Reading track failed", e);
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					}
				}
				
				logger.info("Reading " + futures.size() + " tracks completed");
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
