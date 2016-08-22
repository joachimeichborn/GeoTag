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

import joachimeichborn.geotag.io.database.TableModel.Thumbnail;
import joachimeichborn.geotag.thumbnail.ThumbnailKey;

/**
 * Database implementation using Derby
 * 
 * @author Joachim von Eichborn
 */
class DerbyDatabase implements DatabaseAccess {
	private static final String CREATE_THUMBNAIL_TABLE = "CREATE TABLE " + Thumbnail.TABLE_NAME + " (" + //
			Thumbnail.ID_COLUMN + " INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + //
			Thumbnail.FILE_NAME_COLUMN + " VARCHAR(5000) NOT NULL, " + //
			Thumbnail.WIDTH_COLUM + " SMALLINT NOT NULL, " + //
			Thumbnail.HEIGHT_COLUMN + " SMALLINT NOT NULL, " + //
			Thumbnail.IMAGE_COLUMN + " BLOB NOT NULL, " + //
			"PRIMARY KEY (" + Thumbnail.FILE_NAME_COLUMN + "," + Thumbnail.WIDTH_COLUM + "," + Thumbnail.HEIGHT_COLUMN
			+ ")" + //
			")";

	private static final String SAVE_THUMBNAIL_QUERY = "INSERT INTO " + Thumbnail.TABLE_NAME + //
			"(" + Thumbnail.FILE_NAME_COLUMN + "," + Thumbnail.WIDTH_COLUM + "," + Thumbnail.HEIGHT_COLUMN + ","
			+ Thumbnail.IMAGE_COLUMN + ")" + //
			" VALUES (?,?,?,?)";

	private static final String GET_THUMBNAIL_QUERY = "SELECT " + Thumbnail.IMAGE_COLUMN + //
			" FROM " + Thumbnail.TABLE_NAME + //
			" WHERE " + Thumbnail.FILE_NAME_COLUMN + "=? AND " + Thumbnail.WIDTH_COLUM + "=? AND "
			+ Thumbnail.HEIGHT_COLUMN + "=?";

	private static final String TRIM_THUMBNAIL_TABLE_QUERY = "DELETE FROM " + Thumbnail.TABLE_NAME + //
			" WHERE " + Thumbnail.ID_COLUMN + " IN (" + //
			" SELECT " + Thumbnail.ID_COLUMN + " FROM " + Thumbnail.TABLE_NAME + //
			" ORDER BY " + Thumbnail.ID_COLUMN + " DESC OFFSET ? ROWS)";

	private static Logger logger = Logger.getLogger(DerbyDatabase.class.getSimpleName());
	private Connection readConnection;
	private Connection writeConnection;

	public DerbyDatabase(final String aDriver, final String aUrl) {
		establishConnection(aDriver, aUrl);

		final Set<String> tableNames = getAllTables();

		if (!tableNames.contains(Thumbnail.TABLE_NAME)) {
			createThumbnailsTable();
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

	private void createThumbnailsTable() {
		try {
			writeConnection.prepareStatement(CREATE_THUMBNAIL_TABLE).execute();
		} catch (final SQLException aEx) {
			logger.severe("Could not create thumbnails table: " + aEx.getMessage());
			throw new IllegalStateException(aEx);
		}
	}

	@Override
	public void saveThumbnail(final ThumbnailKey aKey, final BufferedImage aThumbnail) {
		logger.fine("Saving thumbnail for " + aKey);
		synchronized (writeConnection) {
			PreparedStatement statement = null;
			try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(aThumbnail, "png", baos);
				baos.flush();

				statement = writeConnection.prepareStatement(SAVE_THUMBNAIL_QUERY);
				statement.setString(1, aKey.getFile());
				statement.setInt(2, aKey.getWidth());
				statement.setInt(3, aKey.getHeight());
				statement.setBytes(4, baos.toByteArray());

				statement.executeUpdate();
			} catch (final SQLException | IOException aEx) {
				logger.severe("Could not save thumbnail for " + aKey + ": " + aEx.getMessage());

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
	public BufferedImage getThumbnail(final ThumbnailKey aKey) {
		synchronized (readConnection) {
			PreparedStatement statement = null;
			ResultSet result = null;
			try {
				statement = readConnection.prepareStatement(GET_THUMBNAIL_QUERY);
				statement.setString(1, aKey.getFile());
				statement.setInt(2, aKey.getWidth());
				statement.setInt(3, aKey.getHeight());

				result = statement.executeQuery();
				if (!result.next()) {
					return null;
				} else {
					final Blob blob = result.getBlob(Thumbnail.IMAGE_COLUMN);
					return ImageIO.read(blob.getBinaryStream());
				}
			} catch (final SQLException | IOException aEx) {
				logger.severe("Could not get thumbnail for " + aKey + ": " + aEx.getMessage());
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

	public void trim(final int aThumbnailEntries) {
		try {
			final PreparedStatement statement = writeConnection.prepareStatement(TRIM_THUMBNAIL_TABLE_QUERY);
			statement.setInt(1, aThumbnailEntries);
			int affectedRows = statement.executeUpdate();
			logger.fine("Trimming database to " + aThumbnailEntries + " entries affected " + affectedRows + " rows");
		} catch (final SQLException aEx) {
			logger.severe("Could not trim thumbnails table: " + aEx.getMessage());
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
