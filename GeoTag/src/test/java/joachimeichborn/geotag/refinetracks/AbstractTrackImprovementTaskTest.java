package joachimeichborn.geotag.refinetracks;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.PositionData;

public class AbstractTrackImprovementTaskTest {
	@DataProvider
	public Object[][] data() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			input.add(new Object[] { inputPositions, expectedPositions, true });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:03Z", "C", 10));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:04Z", "B", 300));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "D", 400));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "D", 400));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:03Z", "C", 10));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:04Z", "B", 300));
			input.add(new Object[] { inputPositions, expectedPositions, true });
		}
		{
			final List<PositionData> inputPositions = new ArrayList<PositionData>();
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:03Z", "C", 10));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:04Z", "B", 300));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			inputPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "D", 400));
			final List<PositionData> expectedPositions = new ArrayList<PositionData>();
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:03Z", "C", 10));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:04Z", "B", 300));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:01Z", "A", 2));
			expectedPositions.add(new PositionData(new Coordinates(108, 34, 0), "2000-01-01T01:01:02Z", "D", 400));
			input.add(new Object[] { inputPositions, expectedPositions, false });
		}
		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "data")
	public void test(final List<PositionData> aPositions, final List<PositionData> aExpectedPositions,
			final boolean aEnabled) {
		final TrackImprovementTask task = new AbstractTrackImprovementTask(null) {
			@Override
			public boolean isEnabled() {
				return aEnabled;
			}

			@Override
			void process(List<PositionData> aPositions) {
				// do nothing
			}

		};

		task.execute(aPositions);

		Assert.assertEquals(aPositions, aExpectedPositions);
	}
}
