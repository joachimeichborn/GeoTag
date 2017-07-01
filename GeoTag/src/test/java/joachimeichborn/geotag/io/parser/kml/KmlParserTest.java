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
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class KmlParserTest {
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
			data.add("kml1.kml", expectedPositions);
		}
		return data.toArray();
	}

	@Test(dataProvider = "dataReading")
	public void testReading(final String aKmlFilename, final List<PositionData> aExpectedPositions) throws IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		final File kmlFile = new File(testDir, aKmlFilename);
		FileUtils.copyURLToFile(KmlParser.class.getResource(aKmlFilename), kmlFile);

		final KmlParser reader = new KmlParser();

		final Track track = reader.read(kmlFile.toPath());
		final List<PositionData> positions = track.getPositions();

		Assert.assertEquals(positions, aExpectedPositions);
	}
}
