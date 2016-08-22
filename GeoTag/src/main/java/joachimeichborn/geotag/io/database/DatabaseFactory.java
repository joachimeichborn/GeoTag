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

package joachimeichborn.geotag.io.database;

import joachimeichborn.geotag.LifeCycleManager;

/**
 * Factory to get a database instance
 * 
 * @author Joachim von Eichborn
 */
public class DatabaseFactory {
	private static DatabaseAccess INSTANCE;

	/**
	 * @return a database instance, if none exists a new one is created
	 */
	public static DatabaseAccess getDatabaseAccess() {
		if (INSTANCE == null) {
			INSTANCE = new DerbyDatabase("org.apache.derby.jdbc.EmbeddedDriver",
					"jdbc:derby:" + LifeCycleManager.WORKING_DIR.resolve("database").toString() + ";create=true");
		}

		return INSTANCE;
	}
}
