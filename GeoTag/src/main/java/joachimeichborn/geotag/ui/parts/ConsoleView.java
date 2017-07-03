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

package joachimeichborn.geotag.ui.parts;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import joachimeichborn.geotag.logging.ConsoleViewAppender;

public class ConsoleView {
	private final ConsoleViewAppender logWriter;
	private StyledText console;

	@Inject
	public ConsoleView(final ConsoleViewAppender aLogWriter) {
		logWriter = aLogWriter;
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		aParent.setLayout(new FillLayout(SWT.HORIZONTAL));

		console = new StyledText(aParent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		console.setEditable(false);

		logWriter.setConsole(console, Display.getCurrent());
	}

	@PreDestroy
	public void dispose() {
		final Logger rootLogger = Logger.getLogger("");
		rootLogger.removeHandler(logWriter);
	}
}
