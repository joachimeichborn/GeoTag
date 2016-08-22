package joachimeichborn.geotag.refinetracks;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;

public class InterpolatePositionsTaskTest {
	@DataProvider
	public Object[][] dataEnabled() {
		final List<Object[]> data = new ArrayList<>();
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(true);
			data.add(new Object[] { options.build(), true });
		}
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(false);
			data.add(new Object[] { options.build(), false });
		}
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataEnabled")
	public void testEnabled(final ImproveTrackOptions aOptions, final boolean aEnabled) {
		final TrackImprovementTask task = new InterpolatePositionsTask(aOptions);
		Assert.assertEquals(task.isEnabled(), aEnabled);
	}

	@DataProvider
	public Object[][] dataInterpolatePositions() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 7, 10), "2000-01-01T01:01:02Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(5, 7, 10), "2000-01-01T01:01:02Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(30, 50, 40), "2000-01-01T01:01:03Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(12, 19, 17), "2000-01-01T01:01:02Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(30, 50, 40), "2000-01-01T01:01:03Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(35, 24, 58), "2000-01-01T01:01:05Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(235, 124, 259), "2000-01-01T01:01:04Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(30, 50, 40), "2000-01-01T01:01:03Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(12, 19, 17), "2000-01-01T01:01:02Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(90, 60, 103), "2000-01-01T01:01:03Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(100, 66, 119), "2000-01-01T01:01:04Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(35, 24, 58), "2000-01-01T01:01:05Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setInterpolatePositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(30, 50, 40), "2000-01-01T01:01:03Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(235, 124, 259), "2000-01-01T01:01:04Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(35, 24, 58), "2000-01-01T01:01:05Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:02Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(30, 50, 40), "2000-01-01T01:01:03Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(235, 124, 259), "2000-01-01T01:01:04Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(35, 24, 58), "2000-01-01T01:01:05Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder();
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataInterpolatePositions")
	public void testInterpolatePositions(final List<PositionData> aPositions,
			final List<PositionData> aExpectedPositions, final ImproveTrackOptions aOptions) {
		final TrackImprovementTask task = new InterpolatePositionsTask(aOptions);
		task.execute(aPositions);
		Assert.assertEquals(aPositions, aExpectedPositions);
	}
}
