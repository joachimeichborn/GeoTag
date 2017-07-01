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

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;

public class LongLogFormat extends Formatter {
	@Override
	public String format(final LogRecord aRecord) {
		final StringBuilder sb = new StringBuilder();
		sb.append(new DateTime(aRecord.getMillis()).toString());
		sb.append(" [").append(aRecord.getLevel().getName().charAt(0)).append("] ");
		sb.append(aRecord.getMessage());
		sb.append(" <").append(aRecord.getLoggerName()).append(">");
		sb.append(System.lineSeparator());
		if (aRecord.getThrown() != null) {
			sb.append(ExceptionUtils.getStackTrace(aRecord.getThrown()));
		}
		return sb.toString();
	}
}