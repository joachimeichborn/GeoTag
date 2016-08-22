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

package joachimeichborn.geotag;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import joachimeichborn.geotag.io.database.DatabaseAccess;
import joachimeichborn.geotag.io.database.DatabaseFactory;
import joachimeichborn.geotag.logging.ConsoleAppender;
import joachimeichborn.geotag.logging.ConsoleViewAppender;
import joachimeichborn.geotag.logging.JoachimEichbornFilter;
import joachimeichborn.geotag.logging.LongLogFormat;
import joachimeichborn.geotag.logging.ShortLogFormat;
import joachimeichborn.geotag.ui.preferences.GeneralPreferences;

public class LifeCycleManager {
	public static final int VERSION_MAJOR = 1;
	public static final int VERSION_MINOR = 0;
	public static final int VERSION_PATCH = 0;
	public static final Path WORKING_DIR = Paths.get(System.getProperty("user.home"), ".geotag");
	public static final String PREFERENCES_NODE = "joachimeichborn.geotag";
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

	private static final Logger logger = Logger.getLogger(LifeCycleManager.class.getSimpleName());

	@PostContextCreate
	void postContextCreate(final IApplicationContext aAppContext, final Display aDisplay) {
		initializeLogging();

		logger.fine("Starting up");
		logger.fine("Working directory is " + WORKING_DIR);
		// request database access to have it set up
		DatabaseFactory.getDatabaseAccess();

		forceHookInFileDialogs();
	}

	/**
	 * This is quite an ugly hack for a limitation in the SWT FileDialog. The
	 * FileDialog creates a buffer where it wants to store the names of the
	 * selected files. If the buffer is to small for all selected names, the
	 * dialog simply closes as if the user had canceled the dialog.However,
	 * internally it remembers that the buffer was too small and that it should
	 * use a callback hook to circumvent this problem the next time. Therefore,
	 * the next time it is opened, the hook is used and everything works as
	 * expected.
	 * 
	 * This method uses reflection to set the flag to use the callback hook
	 * right away
	 */
	private void forceHookInFileDialogs() {
		try {
			final Field useHook = FileDialog.class.getDeclaredField("USE_HOOK");
			useHook.setAccessible(true);
			final Shell shell = new Shell();
			useHook.set(new FileDialog(shell), true);
			shell.dispose();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.info("Could not enforce using callback hook for file dialogs: " + e.getMessage());
		}
	}

	private void initializeLogging() {
		LogManager.getLogManager().reset();
		final Logger rootLogger = Logger.getLogger("");
		rootLogger.setLevel(Level.FINER);

		final Handler consoleAppender = new ConsoleAppender();
		consoleAppender.setLevel(Level.FINE);
		consoleAppender.setFormatter(new LongLogFormat());
		consoleAppender.setFilter(new JoachimEichbornFilter());
		rootLogger.addHandler(consoleAppender);

		final String dateTime = formatter.format(LocalDateTime.now());
		final Path logFile = WORKING_DIR.resolve("geotag_" + dateTime + ".log");
		try {
			final Handler fileAppender = new FileHandler(logFile.toString());
			fileAppender.setLevel(Level.FINER);
			fileAppender.setFormatter(new LongLogFormat());
			fileAppender.setFilter(new JoachimEichbornFilter());
			rootLogger.addHandler(fileAppender);
		} catch (SecurityException | IOException e) {
			logger.severe("Could not create log file: " + e.getMessage());
		}

		final ConsoleViewAppender viewAppender = ConsoleViewAppender.getInstance();
		viewAppender.setFormatter(new ShortLogFormat());
		viewAppender.setLevel(Level.INFO);
		viewAppender.setFilter(new JoachimEichbornFilter());
		rootLogger.addHandler(viewAppender);
	}

	@PreSave
	void preSave(
			@Preference(nodePath = PREFERENCES_NODE, value = GeneralPreferences.DB_MAX_ENTRIES) final int aMaxEntries,
			@Preference(nodePath = PREFERENCES_NODE) final IEclipsePreferences aPreferences) {
		final DatabaseAccess database = DatabaseFactory.getDatabaseAccess();
		database.trim(aMaxEntries);
		database.close();

		try {
			aPreferences.flush();
		} catch (BackingStoreException e) {
			logger.log(Level.SEVERE, "Could not save preferences: " + e.getMessage(), e);
		}

		Job.getJobManager().cancel(null);

		logger.info("Shuttdown completed");
		LogManager.getLogManager().reset();
	}
}
