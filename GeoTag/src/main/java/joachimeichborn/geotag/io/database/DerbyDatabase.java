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
import java.util.logging.Level;
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
			"PRIMARY KEY (" + Preview.FILE_NAME_COLUMN + "," + Preview.WIDTH_COLUM + "," + Preview.HEIGHT_COLUMN + ")" + //
			")";

	private static final String SAVE_PREVIEW_QUERY = "INSERT INTO " + Preview.TABLE_NAME + //
			"(" + Preview.FILE_NAME_COLUMN + "," + Preview.WIDTH_COLUM + "," + Preview.HEIGHT_COLUMN + "," + Preview.IMAGE_COLUMN + ")" + //
			" VALUES (?,?,?,?)";

	private static final String GET_PREVIEW_QUERY = "SELECT " + Preview.IMAGE_COLUMN + //
			" FROM " + Preview.TABLE_NAME + //
			" WHERE " + Preview.FILE_NAME_COLUMN + "=? AND " + Preview.WIDTH_COLUM + "=? AND " + Preview.HEIGHT_COLUMN + "=?";

	private static final String DOES_PREVIEW_EXIST_QUERY = "SELECT COUNT(" + Preview.FILE_NAME_COLUMN + ") AS c " + //
			" FROM " + Preview.TABLE_NAME + //
			" WHERE " + Preview.FILE_NAME_COLUMN + "=?";

	private static final String GET_PREVIEW_ANY_SIZE_QUERY = "SELECT " + Preview.IMAGE_COLUMN + //
			" FROM " + Preview.TABLE_NAME + //
			" WHERE " + Preview.FILE_NAME_COLUMN + "=?";
			
	private static final String GET_MAX_PREVIEW_ID_QUERY = "SELECT MAX(" + Preview.ID_COLUMN + ") as max_id FROM " + Preview.TABLE_NAME;

	private static final String TRIM_PREVIEW_TABLE_QUERY = "DELETE FROM " + Preview.TABLE_NAME + //
			" WHERE " + Preview.ID_COLUMN + " < ?";

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
			logger.log(Level.SEVERE, "Could not connect to database " + aUrl, aEx);
			close();
			
			throw new IllegalStateException(aEx);
		}
	}

	private Set<String> getAllTables() {
		final Set<String> tableNames = new HashSet<String>();

		try {
			synchronized (readConnection) {
				final DatabaseMetaData metaData = readConnection.getMetaData();
				try (final ResultSet rs = metaData.getTables(null, null, null, new String[] { "TABLE" })) {
					while (rs.next()) {
						tableNames.add(rs.getString("TABLE_NAME").toLowerCase());
					}
				}
			}
		} catch (final SQLException aEx) {
			logger.log(Level.SEVERE, "Could not obtain names of existing tables", aEx);
			throw new IllegalStateException(aEx);
		}

		return tableNames;
	}

	private void createPreviewTable() {
		try {
			synchronized (writeConnection) {
				try (final PreparedStatement statement = writeConnection.prepareStatement(CREATE_PREVIEW_TABLE)) {
					statement.execute();
				}
			}
		} catch (final SQLException aEx) {
			logger.log(Level.SEVERE, "Could not create previews table", aEx);
			throw new IllegalStateException(aEx);
		}
	}

	@Override
	public void savePreview(final PreviewKey aKey, final BufferedImage aPreview) {
		logger.fine("Saving preview for " + aKey);
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(aPreview, "png", baos);
			baos.flush();

			synchronized (writeConnection) {
				try (final PreparedStatement statement = writeConnection.prepareStatement(SAVE_PREVIEW_QUERY)) {
					statement.setString(1, aKey.getFile());
					statement.setInt(2, aKey.getWidth());
					statement.setInt(3, aKey.getHeight());
					statement.setBytes(4, baos.toByteArray());

					statement.executeUpdate();
				}
			}
		} catch (final SQLException | IOException aEx) {
			logger.log(Level.SEVERE, "Could not save preview for " + aKey, aEx);
		}
	}

	@Override
	public BufferedImage getPreview(final PreviewKey aKey) {
		try {
			synchronized (readConnection) {
				try (final PreparedStatement statement = readConnection.prepareStatement(GET_PREVIEW_QUERY)) {
					statement.setString(1, aKey.getFile());
					statement.setInt(2, aKey.getWidth());
					statement.setInt(3, aKey.getHeight());

					try (final ResultSet result = statement.executeQuery()) {
						if (!result.next()) {
							return null;
						} else {
							final Blob blob = result.getBlob(Preview.IMAGE_COLUMN);
							return ImageIO.read(blob.getBinaryStream());
						}
					}
				}
			}
		} catch (final SQLException | IOException aEx) {
			logger.log(Level.SEVERE, "Could not get preview for " + aKey, aEx);
			return null;
		}
	}

	@Override
	public boolean doesPreviewExist(final String aFile) {
		try {
			synchronized (readConnection) {
				try (final PreparedStatement statement = readConnection.prepareStatement(DOES_PREVIEW_EXIST_QUERY)) {
					statement.setString(1, aFile);

					try (final ResultSet result = statement.executeQuery()) {
						if (!result.next()) {
							return false;
						} else {
							return result.getInt("c") > 0;
						}
					}
				}
			}
		} catch (final SQLException aEx) {
			logger.log(Level.SEVERE, "Could not check whether preview exists for " + aFile, aEx);
			return false;
		}
	}

	@Override
	public BufferedImage getPreviewAnySize(final String aFile) {
		try {
			synchronized (readConnection) {
				try (final PreparedStatement statement = readConnection.prepareStatement(GET_PREVIEW_ANY_SIZE_QUERY)) {
					statement.setString(1, aFile);

					try (final ResultSet result = statement.executeQuery()) {
						if (!result.next()) {
							return null;
						} else {
							final Blob blob = result.getBlob(Preview.IMAGE_COLUMN);
							return ImageIO.read(blob.getBinaryStream());
						}
					}
				}
			}
		} catch (final SQLException | IOException aEx) {
			logger.log(Level.SEVERE, "Could not get preview for " + aFile, aEx);
			return null;
		}
	}

	public void trim(final int aPreviewEntries) {
		logger.finer("Trimming database to " + aPreviewEntries + " entries");

		long maxId = 0L;
		try {
			synchronized (readConnection) {
				try (final PreparedStatement statement = readConnection.prepareStatement(GET_MAX_PREVIEW_ID_QUERY)) {

					try (final ResultSet result = statement.executeQuery()) {
						if (!result.next()) {
							logger.severe("Could not get result for maximal preview id");
						} else {
							maxId = result.getLong("max_id");
						}
					}
				}
			}
		} catch (final SQLException aEx) {
			logger.log(Level.SEVERE, "Error while getting maximal preview id", aEx);
		}

		if (maxId > 0L) {
			long minSurvivingEntryId = maxId - aPreviewEntries;
			logger.finer("Maximal preview id is " + maxId + ", thus deleting all entries with ids lower than " + minSurvivingEntryId);

			try {
				synchronized (writeConnection) {
					try (final PreparedStatement statement = writeConnection.prepareStatement(TRIM_PREVIEW_TABLE_QUERY)) {
						statement.setLong(1, minSurvivingEntryId);
						int affectedRows = statement.executeUpdate();
						logger.fine("Trimming database to " + aPreviewEntries + " entries affected " + affectedRows + " rows");
					}
				}
			} catch (final SQLException aEx) {
				logger.log(Level.SEVERE, "Trimming previews table in database failed", aEx);
			}
		}
	}


	@Override
	public void close() {
		if (readConnection != null) {
			synchronized (readConnection) {
				try {
					readConnection.close();
				} catch (SQLException e) {
					logger.log(Level.SEVERE, "Could not close database read connection", e);
				}
			}
		}

		if (writeConnection != null) {
			synchronized (writeConnection) {
				try {
					writeConnection.close();
				} catch (SQLException e) {
					logger.log(Level.SEVERE, "Could not close database write connection", e);
				}
			}
		}
	}
}
