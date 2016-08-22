package joachimeichborn.geotag.refinetracks;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;

public class RemoveIrrelevantPositionsTaskTest {
	@DataProvider
	public Object[][] dataEnabled() {
		final List<Object[]> data = new ArrayList<>();
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setRemoveIrrelevantPositions(true);
			data.add(new Object[] { options.build(), true });
		}
		{
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder()
					.setRemoveIrrelevantPositions(false);
			data.add(new Object[] { options.build(), false });
		}
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataEnabled")
	public void testEnabled(final ImproveTrackOptions aOptions, final boolean aEnabled) {
		final TrackImprovementTask task = new RemoveIrrelevantPositionsTask(aOptions);
		Assert.assertEquals(task.isEnabled(), aEnabled);
	}

	@DataProvider
	public Object[][] dataFilterPositions() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			final List<PositionData> expectedPositions = new ArrayList<>();
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "", 10f));
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "", 10f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1f));
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:03Z", "c", 1f));
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:03Z", "c", 1f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1.1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:03Z", "c", 1f));
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1.1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:03Z", "c", 1f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 5, 3), "2000-01-01T01:01:02Z", "b", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:03Z", "c", 1f));
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 5, 3), "2000-01-01T01:01:02Z", "b", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:03Z", "c", 1f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<>();
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1f));
			inputPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:31:03Z", "c", 1f));
			final List<PositionData> expectedPositions = new ArrayList<>();
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:01Z", "a", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:01:02Z", "b", 1f));
			expectedPositions.add(new PositionData(new Coordinates(1, 2, 3), "2000-01-01T01:31:03Z", "c", 1f));
			final ImproveTrackOptionsBuilder options = new ImproveTrackOptionsBuilder().setRemoveIrrelevantPositions(true);
			input.add(new Object[] { inputPositions, expectedPositions, options.build() });
		}
		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataFilterPositions")
	public void testFilterPositions(final List<PositionData> aPositions, final List<PositionData> aExpectedPositions,
			final ImproveTrackOptions aOptions) {
		final TrackImprovementTask task = new RemoveIrrelevantPositionsTask(aOptions);
		task.execute(aPositions);
		Assert.assertEquals(aPositions, aExpectedPositions);
	}
}
