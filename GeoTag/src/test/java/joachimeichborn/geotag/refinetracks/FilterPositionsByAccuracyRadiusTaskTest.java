package joachimeichborn.geotag.refinetracks;

import java.util.ArrayList;
import java.util.List;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FilterPositionsByAccuracyRadiusTaskTest {
	@DataProvider
	public Object[][] dataEnabled() {
		final List<Object[]> data = new ArrayList<>();
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setFilterByAccuracyRadius(true)
					.setRadiusThreshold(1000);
			data.add(new Object[] { options.build(), true });
		}
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByAccuracyRadius(false);
			data.add(new Object[] { options.build(), false });
		}
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataEnabled")
	public void testEnabled(final ImproveTrackOptions aOptions, final boolean aEnabled) {
		final TrackImprovementTask task = new FilterPositionsByAccuracyRadiusTask(aOptions);
		Assert.assertEquals(task.isEnabled(), aEnabled);
	}

	@DataProvider
	public Object[][] dataFilterPositions() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setFilterByAccuracyRadius(true)
					.setRadiusThreshold(250);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:03Z", "C", 10));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "B", 300));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "D", 400));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:03Z", "C", 10));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setFilterByAccuracyRadius(true)
					.setRadiusThreshold(250);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataFilterPositions")
	public void testFilterPositions(final List<PositionData> aPositions, final List<PositionData> aExpectedPositions,
			final ImproveTrackOptions aOptions) {
		final TrackImprovementTask task = new FilterPositionsByAccuracyRadiusTask(aOptions);
		task.execute(aPositions);
		Assert.assertEquals(aPositions, aExpectedPositions);
	}
}
