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

package joachimeichborn.geotag.io.jpeg;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcRecord;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcTypes;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

import joachimeichborn.geotag.misc.PictureOrientation;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;

/**
 * Representation of EXIF image meta data
 * 
 * @author Joachim von Eichborn
 */
public class PictureMetadataReader {
	static final Logger logger = Logger.getLogger(PictureMetadataReader.class.getSimpleName());

	private final JpegImageMetadata jpegMetadata;
	private final Path pictureFile;
	private boolean fetchedTime = false;
	private String time;
	private boolean fetchedCoordinates = false;
	private Coordinates coordinates;
	private boolean fetchedOrientation = false;
	private PictureOrientation orientation;
	private boolean fetchedGeocoding = false;
	private Geocoding geocoding;
	private boolean fetchedThumbnail = false;
	private BufferedImage thumbnail;

	/**
	 * Constructor
	 * 
	 * @param aPicture
	 *            The pictureFile whose meta data is represented by the
	 *            constructed object
	 */
	public PictureMetadataReader(final Path aPicture) {
		pictureFile = aPicture;
		ImageMetadata metadata = null;
		try {
			metadata = Imaging.getMetadata(aPicture.toFile());
		} catch (ImageReadException | IOException e) {
			logger.severe("Could not get metadata section from file '" + pictureFile + "': " + e.getMessage());
		}

		if (metadata instanceof JpegImageMetadata) {
			jpegMetadata = (JpegImageMetadata) metadata;
		} else {
			jpegMetadata = null;
			logger.warning("Could not get JPEG metadate for file " + pictureFile);
		}
	}

	/**
	 * Extract the time that is stored in the EXIF tag
	 * {@link ExifTagConstants#EXIF_TAG_DATE_TIME_ORIGINAL}
	 */
	public String getTime() {
		if (!fetchedTime) {
			fetchedTime = true;
			time = getTagStringValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
		}

		return time;
	}

	public Coordinates getCoordinates() {
		if (!fetchedCoordinates) {
			fetchedCoordinates = true;
			try {
				extractGpsData();
			} catch (ImageReadException e) {
				logger.severe("Could not get gps data from file '" + pictureFile + "': " + e.getMessage());
			}
		}

		return coordinates;
	}

	/**
	 * Extract existing position information from the EXIF section
	 * 
	 * @throws ImageReadException
	 */
	private void extractGpsData() throws ImageReadException {
		if (jpegMetadata != null) {
			final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (exifMetadata != null) {
				final double altitude = getAltitude(exifMetadata);

				final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
				if (gpsInfo != null) {
					final double longitude = gpsInfo.getLongitudeAsDegreesEast();
					final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

					coordinates = new Coordinates(latitude, longitude, altitude);
				}
			}
		}
	}

	private double getAltitude(final TiffImageMetadata aMetadata) throws ImageReadException {
		final byte[] altitudeRefArray = getTagByteValue(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
		final double[] altitudeArray = getTagRationalValue(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);

		if (altitudeRefArray == null || altitudeRefArray.length == 0 || altitudeArray == null
				|| altitudeArray.length == 0) {
			return 0;
		}

		double altitude = altitudeArray[0];

		if (altitudeRefArray[0] == 1) {
			altitude *= -1;
		}

		return altitude;
	}

	public PictureOrientation getOrientation() {
		if (!fetchedOrientation) {
			fetchedOrientation = true;
			extractOrientation();
		}

		return orientation;
	}

	private void extractOrientation() {
		final int[] rawOrientation = getTagIntValue(TiffTagConstants.TIFF_TAG_ORIENTATION);

		if (rawOrientation == null || rawOrientation.length == 0) {
			logger.info("Could not get orientation for '" + pictureFile + "', assuming horizontal orientation");
			orientation = PictureOrientation.HORIZONTAL_NORMAL;
			return;
		}

		orientation = PictureOrientation.getByMetadataValue(rawOrientation[0]);
	}

	public Geocoding getGeocoding() {
		if (!fetchedGeocoding) {
			fetchedGeocoding = true;
			extractGeocoding();
		}

		return geocoding;
	}

	public void extractGeocoding() {
		if (jpegMetadata != null) {
			final JpegPhotoshopMetadata photoshopMetadata = jpegMetadata.getPhotoshop();
			if (photoshopMetadata != null) {
				final Geocoding.Builder builder = new Geocoding.Builder();
				final List<IptcRecord> records = photoshopMetadata.photoshopApp13Data.getRecords();

				for (final IptcRecord record : records) {
					if (record.iptcType.equals(IptcTypes.CONTENT_LOCATION_NAME)) {
						builder.setLocationName(record.getValue());
					} else if (record.iptcType.equals(IptcTypes.CITY)) {
						builder.setCity(record.getValue());
					} else if (record.iptcType.equals(IptcTypes.SUBLOCATION)) {
						builder.setSublocation(record.getValue());
					} else if (record.iptcType.equals(IptcTypes.PROVINCE_STATE)) {
						builder.setProvinceState(record.getValue());
					} else if (record.iptcType.equals(IptcTypes.COUNTRY_PRIMARY_LOCATION_CODE)) {
						builder.setCountryCode(record.getValue());
					} else if (record.iptcType.equals(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME)) {
						builder.setCountryName(record.getValue());
					}
				}

				geocoding = builder.build();
			}
		}
	}

	public BufferedImage getThumbnail() {
		if (!fetchedThumbnail) {
			fetchedThumbnail = true;
			extractThumbnail();
		}

		return thumbnail;
	}

	private void extractThumbnail() {
		if (jpegMetadata != null) {
			try {
				thumbnail = jpegMetadata.getEXIFThumbnail();
			} catch (ImageReadException | IOException e) {
				logger.fine("Failed to extract exiv thumbnail from " + pictureFile);
			}
		}
	}

	private String getTagStringValue(final TagInfo aTagInfo) {
		if (jpegMetadata != null) {
			final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(aTagInfo);
			if (field == null) {
				logger.fine("Field " + aTagInfo + " is null for picture " + pictureFile);
				return null;
			}
			
			final FieldType fieldType = field.getFieldType();
			if (fieldType.getType() == FieldType.ASCII.getType()) {
				try {
					return field.getStringValue();
				} catch (ImageReadException e) {
					logger.fine("Failed to read " + aTagInfo + " from " + pictureFile + ": " + e.getMessage());
				}
			}
		}
		return null;
	}

	private double[] getTagRationalValue(final TagInfo aTagInfo) {
		if (jpegMetadata != null) {
			final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(aTagInfo);
			if (field != null) {
				final FieldType fieldType = field.getFieldType();
				if (fieldType.getType() == FieldType.RATIONAL.getType()) {
					try {
						return field.getDoubleArrayValue();
					} catch (ImageReadException e) {
						logger.fine("Failed to read " + aTagInfo + " from " + pictureFile + ": " + e.getMessage());
					}
				}
			}
		}
		return null;
	}

	private byte[] getTagByteValue(final TagInfo aTagInfo) {
		if (jpegMetadata != null) {
			final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(aTagInfo);
			if (field != null) {
				final FieldType fieldType = field.getFieldType();
				if (fieldType.getType() == FieldType.BYTE.getType()) {
					return field.getByteArrayValue();
				}
			}
		}
		return null;
	}

	private int[] getTagIntValue(final TagInfo aTagInfo) {
		if (jpegMetadata != null) {
			final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(aTagInfo);
			if (field != null) {
				final FieldType fieldType = field.getFieldType();
				if (fieldType.getType() == FieldType.SHORT.getType()) {
					try {
						return field.getIntArrayValue();
					} catch (ImageReadException e) {
						logger.fine("Failed to read " + aTagInfo + " from " + pictureFile + ": " + e.getMessage());
					}
				}
			}
		}
		return null;
	}
}
