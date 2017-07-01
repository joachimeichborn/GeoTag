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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;

import joachimeichborn.geotag.io.database.TableModel.Preview;
import joachimeichborn.geotag.preview.PreviewKey;

/**
 * Database implementation using Derby
 * 
 * @author Joachim von Eichborn
 */
class DerbyDatabase implements DatabaseAccess {
	private static final String CREATE_PREVIEW_TABLE = "CREATE TABLE " + Preview.TABLE_NAME + " (" + //
			Preview.ID_COLUMN + " INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + //
			Preview.FILE_NAME_COLUMN + " VARCHAR(5000) NOT NULL, " + //
			Preview.WIDTH_COLUM + " SMALLINT NOT NULL, " + //
			Preview.HEIGHT_COLUMN + " SMALLINT NOT NULL, " + //
			Preview.IMAGE_COLUMN + " BLOB NOT NULL, " + //
			"PRIMARY KEY (" + Preview.FILE_NAME_COLUMN + "," + Preview.WIDTH_COLUM + "," + Preview.HEIGHT_COLUMN
			+ ")" + //
			")";

	private static final String SAVE_PREVIEW_QUERY = "INSERT INTO " + Preview.TABLE_NAME + //
			"(" + Preview.FILE_NAME_COLUMN + "," + Preview.WIDTH_COLUM + "," + Preview.HEIGHT_COLUMN + ","
			+ Preview.IMAGE_COLUMN + ")" + //
			" VALUES (?,?,?,?)";

	private static final String GET_PREVIEW_QUERY = "SELECT " + Preview.IMAGE_COLUMN + //
			" FROM " + Preview.TABLE_NAME + //
			" WHERE " + Preview.FILE_NAME_COLUMN + "=? AND " + Preview.WIDTH_COLUM + "=? AND "
			+ Preview.HEIGHT_COLUMN + "=?";

	private static final String TRIM_PREVIEW_TABLE_QUERY = "DELETE FROM " + Preview.TABLE_NAME + //
			" WHERE " + Preview.ID_COLUMN + " IN (" + //
			" SELECT " + Preview.ID_COLUMN + " FROM " + Preview.TABLE_NAME + //
			" ORDER BY " + Preview.ID_COLUMN + " DESC OFFSET ? ROWS)";

	private static Logger logger = Logger.getLogger(DerbyDatabase.class.getSimpleName());
	private Connection readConnection;
	private Connection writeConnection;

	public DerbyDatabase(final String aDriver, final String aUrl) {
		establishConnection(aDriver, aUrl);

		final Set<String> tableNames = getAllTables();

		if (!tableNames.contains(Preview.TABLE_NAME)) {
			createPreviewTable();
		}
	}

	private void establishConnection(final String aDriver, final String aUrl) {
		try {
			Class.forName(aDriver).newInstance();
			readConnection = DriverManager.getConnection(aUrl);
			writeConnection = DriverManager.getConnection(aUrl);
		} catch (final InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException aEx) {
			logger.severe("Could not connect database: " + aEx.getMessage());
			throw new IllegalStateException(aEx);
		}
	}

	private Set<String> getAllTables() {
		final Set<String> tableNames = new HashSet<String>();

		try {
			final DatabaseMetaData metaData = readConnection.getMetaData();
			final ResultSet rs = metaData.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				tableNames.add(rs.getString("TABLE_NAME").toLowerCase());
			}
		} catch (final SQLException aEx) {
			logger.severe("Could not obtain names of existing tables: " + aEx.getMessage());
			throw new IllegalStateException(aEx);
		}

		return tableNames;
	}

	private void createPreviewTable() {
		try {
			writeConnection.prepareStatement(CREATE_PREVIEW_TABLE).execute();
		} catch (final SQLException aEx) {
			logger.severe("Could not create previews table: " + aEx.getMessage());
			throw new IllegalStateException(aEx);
		}
	}

	@Override
	public void savePreview(final PreviewKey aKey, final BufferedImage aPreview) {
		logger.fine("Saving preview for " + aKey);
		synchronized (writeConnection) {
			PreparedStatement statement = null;
			try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(aPreview, "png", baos);
				baos.flush();

				statement = writeConnection.prepareStatement(SAVE_PREVIEW_QUERY);
				statement.setString(1, aKey.getFile());
				statement.setInt(2, aKey.getWidth());
				statement.setInt(3, aKey.getHeight());
				statement.setBytes(4, baos.toByteArray());

				statement.executeUpdate();
			} catch (final SQLException | IOException aEx) {
				logger.severe("Could not save preview for " + aKey + ": " + aEx.getMessage());

			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (final SQLException aEx) {
						// nothing to do
					}
				}
			}
		}
	}

	@Override
	public BufferedImage getPreview(final PreviewKey aKey) {
		synchronized (readConnection) {
			PreparedStatement statement = null;
			ResultSet result = null;
			try {
				statement = readConnection.prepareStatement(GET_PREVIEW_QUERY);
				statement.setString(1, aKey.getFile());
				statement.setInt(2, aKey.getWidth());
				statement.setInt(3, aKey.getHeight());

				result = statement.executeQuery();
				if (!result.next()) {
					return null;
				} else {
					final Blob blob = result.getBlob(Preview.IMAGE_COLUMN);
					return ImageIO.read(blob.getBinaryStream());
				}
			} catch (final SQLException | IOException aEx) {
				logger.severe("Could not get preview for " + aKey + ": " + aEx.getMessage());
				return null;
			} finally {
				try {
					if (statement != null) {
						statement.close();
					}
					if (result != null) {
						result.close();
					}
				} catch (final SQLException aEx) {
					// nothing to do
				}
			}
		}
	}

	public void trim(final int aPreviewEntries) {
		try {
			final PreparedStatement statement = writeConnection.prepareStatement(TRIM_PREVIEW_TABLE_QUERY);
			statement.setInt(1, aPreviewEntries);
			int affectedRows = statement.executeUpdate();
			logger.fine("Trimming database to " + aPreviewEntries + " entries affected " + affectedRows + " rows");
		} catch (final SQLException aEx) {
			logger.severe("Could not trim previews table: " + aEx.getMessage());
			throw new IllegalStateException(aEx);
		}
	}

	@Override
	public void close() {
		try {
			readConnection.close();
		} catch (SQLException e) {
			logger.severe("Could not close database readConnection");
		}
	}
}
