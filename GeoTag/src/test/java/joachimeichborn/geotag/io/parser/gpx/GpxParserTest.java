package joachimeichborn.geotag.io.parser.gpx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import joachimeichborn.geotag.DataProviderList;
import joachimeichborn.geotag.io.parser.TrackParser;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class GpxParserTest {
	@DataProvider
	public Object[][] dataReading() {
		final DataProviderList data = new DataProviderList();
		{
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(56.4191662, 40.4483229, 129.0),
					"2015-07-27T20:08:07+03:00", "2015-07-27T20:08:07.000+03:00", 0f));
			expectedPositions.add(new PositionData(new Coordinates(56.4193077, 40.4483077, 138.0),
					"2015-07-27T20:08:36+03:00", "2015-07-27T17:08:36.000Z", 0f));
			expectedPositions.add(new PositionData(new Coordinates(56.4193075, 40.4483176, 131.0),
					"2015-07-27T20:09:06+03:00", "2015-07-27T20:09:06.000+03:00", 0f));
			data.add("gpx1.gpx", expectedPositions);
		}
		return data.toArray();
	}

	@Test(dataProvider = "dataReading")
	public void testReading(final String aGpxFilename, final List<PositionData> aExpectedPositions) throws IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		final File gpxFile = new File(testDir, aGpxFilename);
		FileUtils.copyURLToFile(GpxParser.class.getResource(aGpxFilename), gpxFile);

		final TrackParser reader = new GpxParser();

		final Track track = reader.read(gpxFile.toPath());
		final List<PositionData> positions = track.getPositions();

		Assert.assertEquals(positions, aExpectedPositions);
	}
}
