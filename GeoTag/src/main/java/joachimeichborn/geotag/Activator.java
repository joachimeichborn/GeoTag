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

import org.eclipse.e4.core.di.InjectorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import joachimeichborn.geotag.io.database.DatabaseAccess;
import joachimeichborn.geotag.io.database.DerbyDatabase;

public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext aContext) throws Exception {
		InjectorFactory.getDefault().addBinding(DatabaseAccess.class).implementedBy(DerbyDatabase.class);
	}

	@Override
	public void stop(final BundleContext aContext) throws Exception {
	}
}
