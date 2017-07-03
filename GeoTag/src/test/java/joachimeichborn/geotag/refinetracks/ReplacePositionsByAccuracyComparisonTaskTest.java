package joachimeichborn.geotag.refinetracks;

import java.util.ArrayList;
import java.util.List;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ReplacePositionsByAccuracyComparisonTaskTest {
	@DataProvider
	public Object[][] dataEnabled() {
		final List<Object[]> data = new ArrayList<>();
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			data.add(new Object[] { options.build(), true });
		}
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(false);
			data.add(new Object[] { options.build(), false });
		}
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataEnabled")
	public void testEnabled(final ImproveTrackOptions aOptions, final boolean aEnabled) {
		final TrackImprovementTask task = new ReplacePositionsByAccuracyComparisonTask(aOptions);
		Assert.assertEquals(task.isEnabled(), aEnabled);
	}

	@DataProvider
	public Object[][] dataFilterPositions() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "B", 2));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 10));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z",
					ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 2));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "B", 2));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 250));
			inputPositions.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:02Z", "B", 30));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:01Z",
					ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 30));
			expectedPositions
					.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:02Z", "B", 30));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 30));
			inputPositions.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:02Z", "B", 250));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 30));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z",
					ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 30));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 250));
			inputPositions.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:02Z", "B", 160));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 250));
			expectedPositions
					.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:02Z", "B", 160));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 250));
			inputPositions.add(new PositionData(new Coordinates(108.001, 34.001, 0), "2000-01-01T01:01:02Z", "B", 250));
			inputPositions.add(new PositionData(new Coordinates(108.003, 34.003, 0), "2000-01-01T01:01:03Z", "C", 250));
			inputPositions.add(new PositionData(new Coordinates(108.002, 34.002, 0), "2000-01-01T01:01:04Z", "D", 30));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 250));
			expectedPositions.add(new PositionData(new Coordinates(108.002, 34.002, 0), "2000-01-01T01:01:02Z",
					ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 30));
			expectedPositions.add(new PositionData(new Coordinates(108.002, 34.002, 0), "2000-01-01T01:01:03Z",
					ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 30));
			expectedPositions
					.add(new PositionData(new Coordinates(108.002, 34.002, 0), "2000-01-01T01:01:04Z", "D", 30));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108.001, 34, 0), "2000-01-01T01:01:01Z", "A", 20));
			inputPositions.add(new PositionData(new Coordinates(108.002, 34, 0), "2000-01-01T01:01:02Z", "B", 140));
			inputPositions.add(new PositionData(new Coordinates(108.003, 34, 0), "2000-01-01T01:01:03Z", "C", 260));
			inputPositions.add(new PositionData(new Coordinates(108.004, 34, 0), "2000-01-01T01:01:04Z", "D", 380));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108.001, 34, 0), "2000-01-01T01:01:01Z", "A", 20));
			expectedPositions.add(new PositionData(new Coordinates(108.001, 34, 0), "2000-01-01T01:01:02Z", ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 20));
			expectedPositions.add(new PositionData(new Coordinates(108.001, 34, 0), "2000-01-01T01:01:03Z", ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 20));
			expectedPositions.add(new PositionData(new Coordinates(108.001, 34, 0), "2000-01-01T01:01:04Z", ReplacePositionsByAccuracyComparisonTask.REPLACED_POSITION_NAME, 20));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setReplaceByAccuracyComparison(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataFilterPositions")
	public void testFilterPositions(final List<PositionData> aPositions, final List<PositionData> aExpectedPositions,
			final ImproveTrackOptions aOptions) {
		final TrackImprovementTask task = new ReplacePositionsByAccuracyComparisonTask(aOptions);
		task.execute(aPositions);
		Assert.assertEquals(aPositions, aExpectedPositions);
	}
}
