package joachimeichborn.geotag.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PositionDataTest {
	@Test
	public void testConstructor() {
		{
			// constructor PositionData (final Coordinate aCoordinate, final
			// String aTime)
			final PositionData data = new PositionData(new Coordinates(113.123, 24.21, 4.3),
					"2013-09-05T10:26:28-05:00", "a", 1f);
			final DateTime withTimeZone = new DateTime().withZone(DateTimeZone.forOffsetHours(-5)).withDate(2013, 9, 05)
					.withTime(10, 26, 28, 0);
			final DateTime withoutTimeZone = new DateTime().withZone(DateTimeZone.UTC).withDate(2013, 9, 05)
					.withTime(10, 26, 28, 0);

			Assert.assertEquals(data.getCoordinates().getLatitude(), 113.123);
			Assert.assertEquals(data.getCoordinates().getLongitude(), 24.21);
			Assert.assertEquals(data.getCoordinates().getAltitude(), 4.3);
			Assert.assertEquals(data.getTimeStamp(), withTimeZone);
			Assert.assertEquals(data.getTimeStampWithoutTimeZone(), withoutTimeZone);
			Assert.assertEquals(data.getName(), "a");
			Assert.assertEquals(data.getAccuracy(), 1f);
		}
	}

	@DataProvider
	public Object[][] dataEquals() {
		final List<Object[]> input = new ArrayList<Object[]>();
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, true });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"a", 1.2f);
			input.add(new Object[] { data, false });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"b", 1f);
			input.add(new Object[] { data, false });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.44, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, false });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.44, 4.3),
					"2013-09-05T10:26:28-05:00", "a", 1f);
			input.add(new Object[] { data, false });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.44),
					"2013-09-05T10:26:28-05:00", "a", 1f);
			input.add(new Object[] { data, false });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-04:00",
					"a", 1f);
			input.add(new Object[] { data, false });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T15:26:28-00:00",
					"a", 1f);
			input.add(new Object[] { data, true });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T11:26:28-04:00",
					"a", 1f);
			input.add(new Object[] { data, true });
		}
			{
				final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T09:26:28-06:00",
						"a", 1f);
				input.add(new Object[] { data, true });
			}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T15:26:28Z", "a",
					1f);
		input.add(new Object[] { data, true });
		}

		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataEquals")
	public void testEquals(final PositionData aData, final boolean aResult) {
		final PositionData dataRef = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
				"a", 1f);

		Assert.assertEquals(aData.equals(dataRef), aResult);
	}

	@DataProvider
	public Object[][] dataCompareTo() {
		final List<Object[]> input = new ArrayList<Object[]>();

		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(123.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(103.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:38-05:00",
					"a", 1f);
			input.add(new Object[] { data, 1 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:25:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, -1 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-10-05T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, 1 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-03T10:26:28-05:00",
					"a", 1f);
			input.add(new Object[] { data, -1 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-04:00",
					"a", 1f);
			input.add(new Object[] { data, -1 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T11:26:28-04:00",
					"a", 1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T12:26:28-04:00",
					"a", 1f);
			input.add(new Object[] { data, 1 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T15:26:28-00:00",
					"a", 1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T15:26:28Z", "a",
					1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T15:26:28Z", "b",
					1f);
			input.add(new Object[] { data, 0 });
		}
		{
			final PositionData data = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T15:26:28Z", "a",
					3f);
			input.add(new Object[] { data, 0 });
		}

		return input.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataCompareTo")
	public void testCompareTo(final PositionData aData, final int aResult) {
		final PositionData dataRef = new PositionData(new Coordinates(113.123, 12.3, 4.3), "2013-09-05T10:26:28-05:00",
				"a", 1f);

		Assert.assertEquals(aData.compareTo(dataRef), aResult);
	}
}
