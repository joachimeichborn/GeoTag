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

package joachimeichborn.geotag.logging;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

@Creatable
@Singleton
public class ConsoleViewAppender extends Handler {
	private StyledText console;
	private Display display;
	private final List<LogRecord> records;

	public ConsoleViewAppender() {
		records = new LinkedList<>();
	}

	/**
	 * Set the output console. If a console was already set before, the
	 * assignment will be overwritten. All previous log messages will be shown
	 * in the new console
	 * 
	 * @param aConsole
	 * @param aDisplay
	 */
	public void setConsole(final StyledText aConsole, final Display aDisplay) {
		console = aConsole;
		display = Display.getCurrent();

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (final LogRecord record : records) {
					printMessage(record);
				}
			}
		});
	}

	@Override
	public synchronized void publish(final LogRecord aRecord) {
		if (!isLoggable(aRecord)) {
			return;
		}

		records.add(aRecord);

		if (console != null && !console.isDisposed()) {
			if (Thread.currentThread().equals(Display.getDefault().getThread())) {
				printMessage(aRecord);
			} else {
				// the thread that triggered logging is not the display thread.
				// So we have to ask the display thread to print the log record
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						printMessage(aRecord);
					}
				});
			}
		}
	}

	private void printMessage(final LogRecord aRecord) {
		if (getFormatter() == null) {
			setFormatter(new SimpleFormatter());
		}

		final String message = getFormatter().format(aRecord);

		console.append(message);
		console.setTopIndex(console.getLineCount());

		if (aRecord.getLevel().intValue() >= Level.WARNING.intValue()) {
			final StyleRange styleRange = new StyleRange();
			styleRange.start = console.getCharCount() - (message.length());
			styleRange.length = message.length();
			styleRange.fontStyle = SWT.BOLD;
			if (aRecord.getLevel().intValue() >= Level.SEVERE.intValue()) {
				styleRange.foreground = display.getSystemColor(SWT.COLOR_RED);
			} else {
				styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_RED);
			}
			console.setStyleRange(styleRange);
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}