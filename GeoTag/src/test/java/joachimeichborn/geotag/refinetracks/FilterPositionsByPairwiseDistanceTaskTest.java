package joachimeichborn.geotag.refinetracks;

import java.util.ArrayList;
import java.util.List;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FilterPositionsByPairwiseDistanceTaskTest {
	@DataProvider
	public Object[][] dataEnabled() {
		final List<Object[]> data = new ArrayList<>();
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
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
		final TrackImprovementTask task = new FilterPositionsByPairwiseDistanceTask(aOptions);
		Assert.assertEquals(task.isEnabled(), aEnabled);
	}

	@DataProvider
	public Object[][] dataFilterPositions() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 7, 10), "2000-01-01T01:01:02Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(5, 7, 10), "2000-01-01T01:01:02Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(3, 7, 2), "2000-01-01T01:01:04Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:03Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:03Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(3, 7, 2), "2000-01-01T01:01:04Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:03Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(3, 7, 2), "2000-01-01T01:01:04Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(5, 6, 10), "2000-01-01T01:01:03Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(3, 7, 2), "2000-01-01T01:01:04Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(false).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{ // do not filter a position if the two positions flanking the position
			// are identical, because this might be a star-like
			// pattern
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:03Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:03Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{ // if multiple identical positions follow each other treat them as one
			// position
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:03Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:04Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(1, 1, 2), "2000-01-01T01:01:05Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 2), "2000-01-01T01:01:05Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{ // if multiple identical positions follow each other treat them as one
			// position (edge case where multiple identical positions are
			// the last positions
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:03Z", "", 10f));
			inputPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:04Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 1), "2000-01-01T01:01:01Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:02Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:03Z", "", 10f));
			expectedPositions.add(new PositionData(new Coordinates(10, 15, 5), "2000-01-01T01:01:04Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{ // filter a star-like pattern
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(1, 1, 0), "2000-01-01T00:58:00Z", "A", 25));
			inputPositions.add(new PositionData(new Coordinates(2, 2, 0), "2000-01-01T00:59:00Z", "B", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:01:00Z", "C1", 25));
			inputPositions.add(new PositionData(new Coordinates(3, 3, 0), "2000-01-01T01:02:00Z", "O1", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:03:00Z", "C2", 25));
			inputPositions.add(new PositionData(new Coordinates(4, 4, 0), "2000-01-01T01:04:00Z", "O2", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:05:00Z", "C3", 25));
			inputPositions.add(new PositionData(new Coordinates(5, 5, 0), "2000-01-01T01:06:00Z", "O3", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:07:00Z", "C4", 25));
			inputPositions.add(new PositionData(new Coordinates(6, 6, 0), "2000-01-01T01:08:00Z", "X", 25));
			inputPositions.add(new PositionData(new Coordinates(7, 7, 0), "2000-01-01T01:09:00Z", "Y", 25));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 0), "2000-01-01T00:58:00Z", "A", 25));
			expectedPositions.add(new PositionData(new Coordinates(2, 2, 0), "2000-01-01T00:59:00Z", "B", 25));
			expectedPositions.add(new PositionData(new Coordinates(3, 3, 0), "2000-01-01T01:02:00Z", "O1", 25));
			expectedPositions.add(new PositionData(new Coordinates(4, 4, 0), "2000-01-01T01:04:00Z", "O2", 25));
			expectedPositions.add(new PositionData(new Coordinates(5, 5, 0), "2000-01-01T01:06:00Z", "O3", 25));
			expectedPositions.add(new PositionData(new Coordinates(6, 6, 0), "2000-01-01T01:08:00Z", "X", 25));
			expectedPositions.add(new PositionData(new Coordinates(7, 7, 0), "2000-01-01T01:09:00Z", "Y", 25));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{ // filter a star-like pattern where the first center position fits to
			// the preceding positions
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(48, 48, 0), "2000-01-01T00:58:00Z", "A", 25));
			inputPositions.add(new PositionData(new Coordinates(49, 49, 0), "2000-01-01T00:59:00Z", "B", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:01:00Z", "C1", 25));
			inputPositions.add(new PositionData(new Coordinates(1, 1, 0), "2000-01-01T01:02:00Z", "O1", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:03:00Z", "C2", 25));
			inputPositions.add(new PositionData(new Coordinates(2, 2, 0), "2000-01-01T01:04:00Z", "O2", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:05:00Z", "C3", 25));
			inputPositions.add(new PositionData(new Coordinates(3, 3, 0), "2000-01-01T01:06:00Z", "O3", 25));
			inputPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:07:00Z", "C4", 25));
			inputPositions.add(new PositionData(new Coordinates(4, 4, 0), "2000-01-01T01:08:00Z", "X", 25));
			inputPositions.add(new PositionData(new Coordinates(5, 5, 0), "2000-01-01T01:09:00Z", "Y", 25));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(48, 48, 0), "2000-01-01T00:58:00Z", "A", 25));
			expectedPositions.add(new PositionData(new Coordinates(49, 49, 0), "2000-01-01T00:59:00Z", "B", 25));
			expectedPositions.add(new PositionData(new Coordinates(50, 50, 0), "2000-01-01T01:01:00Z", "C1", 25));
			expectedPositions.add(new PositionData(new Coordinates(1, 1, 0), "2000-01-01T01:02:00Z", "O1", 25));
			expectedPositions.add(new PositionData(new Coordinates(2, 2, 0), "2000-01-01T01:04:00Z", "O2", 25));
			expectedPositions.add(new PositionData(new Coordinates(3, 3, 0), "2000-01-01T01:06:00Z", "O3", 25));
			expectedPositions.add(new PositionData(new Coordinates(4, 4, 0), "2000-01-01T01:08:00Z", "X", 25));
			expectedPositions.add(new PositionData(new Coordinates(5, 5, 0), "2000-01-01T01:09:00Z", "Y", 25));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setFilterByPairwiseDistance(true).setDistanceFactor(1.5);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataFilterPositions")
	public void testFilterPositions(final List<PositionData> aPositions, final List<PositionData> aExpectedPositions,
			final ImproveTrackOptions aOptions) {
		final TrackImprovementTask task = new FilterPositionsByPairwiseDistanceTask(aOptions);
		task.execute(aPositions);
		Assert.assertEquals(aPositions, aExpectedPositions);
	}
}
