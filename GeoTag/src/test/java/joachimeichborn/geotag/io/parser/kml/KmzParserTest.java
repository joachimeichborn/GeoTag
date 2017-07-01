package joachimeichborn.geotag.io.parser.kml;

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
import joachimeichborn.geotag.io.parser.kml.KmzParser;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class KmzParserTest {
	@DataProvider
	public Object[][] dataReading() {
		final DataProviderList data = new DataProviderList();
		{
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(56.4191662, 40.4483229, 129.0),
					"2015-07-27T20:08:07+03:00", "fused1", 8f));
			expectedPositions.add(new PositionData(new Coordinates(56.4193077, 40.4483077, 138.0),
					"2015-07-27T20:08:36+03:00", "fused2", 15f));
			expectedPositions.add(new PositionData(new Coordinates(56.4193075, 40.4483176, 131.0),
					"2015-07-27T20:09:06+03:00", "fused3", 17f));
			expectedPositions.add(new PositionData(new Coordinates(56.9191662, 40.9483229, 129.5),
					"2015-07-28T20:08:07+03:00", "fused1", 8.5f));
			expectedPositions.add(new PositionData(new Coordinates(56.9193077, 40.9483077, 138.5),
					"2015-07-28T20:08:36+03:00", "fused2", 15.5f));
			expectedPositions.add(new PositionData(new Coordinates(56.9193075, 40.9483176, 131.5),
					"2015-07-28T20:09:06+03:00", "fused3", 17.5f));
			data.add("kmz1.kmz", expectedPositions);
		}
		return data.toArray();
	}

	@Test(dataProvider = "dataReading")
	public void testReading(final String aKmzFilename, final List<PositionData> aExpectedPositions) throws IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		final File kmzFile = new File(testDir, aKmzFilename);
		FileUtils.copyURLToFile(KmzParser.class.getResource(aKmzFilename), kmzFile);

		final TrackParser reader = new KmzParser();

		final Track track = reader.read(kmzFile.toPath());
		final List<PositionData> positions = track.getPositions();

		Assert.assertEquals(positions, aExpectedPositions);
	}
}
