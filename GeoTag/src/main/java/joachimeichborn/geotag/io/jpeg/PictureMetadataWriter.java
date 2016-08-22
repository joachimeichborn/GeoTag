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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcBlock;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcRecord;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcType;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcTypes;
import org.apache.commons.imaging.formats.jpeg.iptc.JpegIptcRewriter;
import org.apache.commons.imaging.formats.jpeg.iptc.PhotoshopApp13Data;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.eclipse.e4.core.di.extensions.Preference;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Geocoding;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.ui.preferences.GeneralPreferences;

public class PictureMetadataWriter {
	private static final Path BACKUP_DIR = LifeCycleManager.WORKING_DIR.resolve("backup");
	private static final Logger logger = Logger.getLogger(PictureMetadataWriter.class.getSimpleName());

	@Inject
	@Preference(nodePath = LifeCycleManager.PREFERENCES_NODE, value = GeneralPreferences.BACKUP)
	private boolean backup;

	private final Picture picture;
	private final String backupId;

	public PictureMetadataWriter(final Picture aPicture, final String aUniqueId) {
		picture = aPicture;
		backupId = aUniqueId;
	}

	public void writePositionMetadata() throws PictureAnnotationException {
		final Path temporaryFile = prepareAnnotation();

		try {
			setExifGPSTag(temporaryFile);
		} catch (ImageWriteException | ImageReadException | IOException e) {
			throw new PictureAnnotationException("Failed to annotate position to " + picture.getFile()
					+ ", skipping position annotation for this picture", e);
		}

		finishAnnotation(temporaryFile);
	}

	private Path prepareAnnotation() throws PictureAnnotationException {
		if (backup) {
			logger.fine("Picture backup activated");
			try {
				backupFile();
			} catch (final IOException e) {
				throw new PictureAnnotationException(
						"Failed to backup " + picture.getFile() + ", skipping annotation for this picture", e);
			}
		} else {
			logger.fine("Picture backup deactivated");
		}

		Path temporaryFile = null;
		try {
			temporaryFile = Files.createTempFile("geotag", null);
		} catch (IOException e) {
			throw new PictureAnnotationException(
					"Failed to create temporary file, skipping annotation for this picture", e);
		}

		return temporaryFile;
	}

	private void finishAnnotation(final Path aTemporaryFile) throws PictureAnnotationException {
		try {
			replaceOriginalWithAnnotated(picture.getFile(), aTemporaryFile);
		} catch (IOException e) {
			throw new PictureAnnotationException("Failed to replace original file " + picture.getFile()
					+ " with the annotated one. The original file might got corrupted", e);
		}
	}

	private void setExifGPSTag(final Path aTargetFile) throws IOException, ImageWriteException, ImageReadException {
		final Path sourceFile = picture.getFile();

		try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(aTargetFile.toFile()))) {
			TiffOutputSet outputSet = getOutputSet(sourceFile);

			final Coordinates coordinates = picture.getCoordinates();

			outputSet.setGPSInDegrees(coordinates.getLongitude(), coordinates.getLatitude());
			setAltitude(coordinates.getAltitude(), outputSet);

			new ExifRewriter().updateExifMetadataLossless(sourceFile.toFile(), os, outputSet);
		}
	}

	private void replaceOriginalWithAnnotated(final Path originalFile, final Path annotatedFile) throws IOException {
		Files.move(annotatedFile, originalFile, StandardCopyOption.REPLACE_EXISTING);
	}

	private TiffOutputSet getOutputSet(final Path aFile) throws ImageReadException, IOException, ImageWriteException {
		final ImageMetadata metadata = Imaging.getMetadata(aFile.toFile());
		final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		if (null != jpegMetadata) {
			final TiffImageMetadata exif = jpegMetadata.getExif();
			if (exif != null) {
				return exif.getOutputSet();
			}
		}

		return new TiffOutputSet();
	}

	private void setAltitude(final double aAltitude, final TiffOutputSet aOutputSet) throws ImageWriteException {
		final TiffOutputDirectory gpsDirectory = aOutputSet.getOrCreateGPSDirectory();

		final byte altitudeRef = (byte) (aAltitude < 0 ? 1 : 0);
		final double altitude = Math.abs(aAltitude);

		gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
		gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF, altitudeRef);

		gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
		gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE, RationalNumber.valueOf(altitude));
	}

	private void backupFile() throws IOException {
		final Path sourceFile = picture.getFile();
		final String parentPath = sourceFile.getParent().toString();
		final String hashedPath = DigestUtils.md5Hex(parentPath + backupId);

		final Path targetDir = BACKUP_DIR.resolve(hashedPath);
		Files.createDirectories(targetDir);

		Files.copy(sourceFile, targetDir.resolve(sourceFile.getFileName()));
	}

	public void writeGeocodingMetadata() throws PictureAnnotationException {
		final Path temporaryFile = prepareAnnotation();

		try {
			setGeocoding(temporaryFile);
		} catch (ImageWriteException | ImageReadException | IOException e) {
			throw new PictureAnnotationException("Failed to annotate geocoding to " + picture.getFile()
					+ ", skipping geocoding annotation for this picture", e);
		}

		finishAnnotation(temporaryFile);
	}

	private void setGeocoding(final Path aTargetFile) throws IOException, ImageReadException, ImageWriteException {
		final Path sourceFile = picture.getFile();

		final PhotoshopApp13Data photoshopMetadata = getPhotoshopMetadata(sourceFile);
		if (photoshopMetadata == null) {
			throw new ImageReadException("Could not obtain photoshop metadata");
		}

		try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(aTargetFile.toFile()))) {
			final Geocoding geocoding = picture.getGeocoding();

			final EnumSet<IptcTypes> filterTypes = EnumSet.noneOf(IptcTypes.class);
			filterTypes.add(IptcTypes.CONTENT_LOCATION_NAME);
			filterTypes.add(IptcTypes.CITY);
			filterTypes.add(IptcTypes.SUBLOCATION);
			filterTypes.add(IptcTypes.PROVINCE_STATE);
			filterTypes.add(IptcTypes.COUNTRY_PRIMARY_LOCATION_CODE);
			filterTypes.add(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME);

			final List<IptcRecord> records = photoshopMetadata.getRecords();
			final Iterator<IptcRecord> iter = records.iterator();
			while (iter.hasNext()) {
				final IptcRecord record = iter.next();
				final IptcType type = record.iptcType;
				if (filterTypes.contains(type)) {
					iter.remove();
				}
			}

			records.add(new IptcRecord(IptcTypes.CONTENT_LOCATION_NAME,
					geocoding != null ? geocoding.getLocationName() : ""));
			records.add(new IptcRecord(IptcTypes.CITY, geocoding != null ? geocoding.getCity() : ""));
			records.add(new IptcRecord(IptcTypes.SUBLOCATION, geocoding != null ? geocoding.getSublocation() : ""));
			records.add(
					new IptcRecord(IptcTypes.PROVINCE_STATE, geocoding != null ? geocoding.getProvinceState() : ""));
			records.add(new IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_CODE,
					geocoding != null ? geocoding.getCountryCode() : ""));
			records.add(new IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME,
					geocoding != null ? geocoding.getCountryName() : ""));

			final List<IptcBlock> rawBlocks = photoshopMetadata.getRawBlocks();

			new JpegIptcRewriter().writeIPTC(sourceFile.toFile(), os, new PhotoshopApp13Data(records, rawBlocks));
		}
	}

	private PhotoshopApp13Data getPhotoshopMetadata(final Path aFile)
			throws ImageReadException, IOException, ImageWriteException {
		final ImageMetadata metadata = Imaging.getMetadata(aFile.toFile());
		final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		if (jpegMetadata != null) {
			final JpegPhotoshopMetadata photoshopMetadata = jpegMetadata.getPhotoshop();
			if (photoshopMetadata != null) {
				return photoshopMetadata.photoshopApp13Data;
			}
		}

		return null;
	}
}
