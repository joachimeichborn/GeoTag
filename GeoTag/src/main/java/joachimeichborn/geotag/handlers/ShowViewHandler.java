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

import java.util.logging.Logger;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class ShowViewHandler {
	private static final Logger logger = Logger.getLogger(ShowViewHandler.class.getSimpleName());

	@Execute
	public void execute(final EPartService aPartService,
			@Named("geotag.commandparameter.viewId") final String aPartName) {
		logger.fine("Activating part " + aPartName);
		final MPart part = aPartService.findPart(aPartName);
		aPartService.activate(part, true);
	}
}
