package joachimeichborn.geotag.model;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CoordinatesTest {
	@Test
	public void testConstructor() {
		final Coordinates coord = new Coordinates(12.3, 113.123, 4.3);

		AssertJUnit.assertEquals(coord.getLatitude(), 12.3);
		AssertJUnit.assertEquals(coord.getLongitude(), 113.123);
		AssertJUnit.assertEquals(coord.getAltitude(), 4.3);
	}

	@Test
	public void testDistanceTo() {
		final Coordinates coord1 = new Coordinates(12.3, 113.123, 4.3);
		final Coordinates coord2 = new Coordinates(24.21, 41.3, 12.03);

		AssertJUnit.assertEquals(Double.doubleToLongBits(coord1.distanceTo(coord2)),
				Double.doubleToLongBits(7624762.009587235));
		AssertJUnit.assertEquals(Double.doubleToLongBits(coord2.distanceTo(coord1)),
				Double.doubleToLongBits(7624762.009587235));
		AssertJUnit.assertEquals(Double.doubleToLongBits(coord1.distanceTo(coord1)), Double.doubleToLongBits(0));
	}

	@DataProvider
	public Object[][] dataCompareTo() {
		final List<Object[]> data = new ArrayList<>();
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(5, 5, 5);
			final int order = 0;
			data.add(new Object[] { coord1, coord2, order });
		}
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(6, 9, 9);
			final int order = -1;
			data.add(new Object[] { coord1, coord2, order });
		}
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(4, 9, 9);
			final int order = 1;
			data.add(new Object[] { coord1, coord2, order });
		}
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(5, 6, 9);
			final int order = -1;
			data.add(new Object[] { coord1, coord2, order });
		}
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(5, 4, 9);
			final int order = 1;
			data.add(new Object[] { coord1, coord2, order });
		}
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(5, 5, 6);
			final int order = -1;
			data.add(new Object[] { coord1, coord2, order });
		}
		{
			final Coordinates coord1 = new Coordinates(5, 5, 5);
			final Coordinates coord2 = new Coordinates(5, 5, 4);
			final int order = 1;
			data.add(new Object[] { coord1, coord2, order });
		}
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataCompareTo")
	public void testCompareTo(final Coordinates aCoord1, final Coordinates aCoord2, final int aOrder) {
		Assert.assertEquals(aCoord1.compareTo(aCoord2), aOrder);
	}

	@Test
	public void testEquals() {
		final Coordinates coord1 = new Coordinates(5, 5, 5);
		final Coordinates coord2 = new Coordinates(5, 5, 5);
		final Coordinates coord3 = new Coordinates(6, 5, 5);
		final Coordinates coord4 = new Coordinates(5, 6, 5);
		final Coordinates coord5 = new Coordinates(5, 5, 6);

		Assert.assertTrue(coord1.equals(coord2));
		Assert.assertFalse(coord1.equals(coord3));
		Assert.assertFalse(coord1.equals(coord4));
		Assert.assertFalse(coord1.equals(coord5));
	}
}
