package joachimeichborn.geotag.io.writer.kml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import joachimeichborn.geotag.DataProviderList;
import joachimeichborn.geotag.io.parser.TrackParser;
import joachimeichborn.geotag.io.parser.kml.KmlParser;
import joachimeichborn.geotag.io.writer.TrackWriter;
import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class KmlWriterTest {
	@DataProvider
	public Object[][] dataWriting() {
		final DataProviderList data = new DataProviderList();
		{
			final List<PositionData> positions = new ArrayList<>();
			positions.add(new PositionData(new Coordinates(56.4191662, 40.4483229, 129.0),
					"2015-07-27T20:08:07+03:00", "fused1", 8));
			positions.add(new PositionData(new Coordinates(56.4193077, 40.4483077, 138.0),
					"2015-07-27T20:08:36+03:00", "fused2", 15));
			positions.add(new PositionData(new Coordinates(56.4193075, 40.4483176, 131.0),
					"2015-07-27T20:09:06+03:00", "fused3", 17));
			data.add(positions);
		}
		return data.toArray();
	}

	@Test(dataProvider="dataWriting")
	public void testWriting(final List<PositionData> aPositions) throws IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		final Track track = new Track(Paths.get("dummy.kml"), aPositions);
		
		final File kmlFile = new File(testDir, "test.kml");
		final TrackWriter writer = new KmlWriter();
		writer.write(track, kmlFile.toPath());

		final TrackParser reader = new KmlParser();

		final List<PositionData> readPositions = reader.read(kmlFile.toPath()).getPositions();
		
		Assert.assertEquals(readPositions, aPositions);
	}
}
