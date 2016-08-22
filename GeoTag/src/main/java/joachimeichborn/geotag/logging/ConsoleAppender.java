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

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ConsoleAppender extends Handler {
	public ConsoleAppender() {
	}

	@Override
	public synchronized void publish(final LogRecord aRecord) {
		if (!isLoggable(aRecord)) {
			return;
		}

		if (getFormatter() == null) {
			setFormatter(new SimpleFormatter());
		}

		final String message = getFormatter().format(aRecord);
		try {
			if (aRecord.getLevel().intValue() >= Level.WARNING.intValue()) {
				System.err.write(message.getBytes());
			} else {
				System.out.write(message.getBytes());
			}
		} catch (final Exception exception) {
			reportError(null, exception, ErrorManager.WRITE_FAILURE);
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}
