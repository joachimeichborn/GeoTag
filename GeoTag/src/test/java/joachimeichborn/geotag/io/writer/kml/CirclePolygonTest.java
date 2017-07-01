package joachimeichborn.geotag.io.writer.kml;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import joachimeichborn.geotag.DataProviderList;
import joachimeichborn.geotag.io.writer.kml.CirclePolygon.CartesianPoint;
import joachimeichborn.geotag.io.writer.kml.CirclePolygon.Radian;

public class CirclePolygonTest {
	@DataProvider
	public Object[][] dataCartesianRadianConversion() {
		final DataProviderList data = new DataProviderList();
		data.add(50.46216856666666, 7.1831944000000005, 0.6315912640718324, 0.07960032834002356, 0.771204423532636);
		data.add(-33.867487, 151.206990, -0.7276713373190071, 0.3999251054582431, -0.5572740213472911);
		data.add(-43.952120, -176.561325, -0.7186238830032816, -0.04318093271776476, -0.6940570018571312);
		return data.toArray();
	}

	@Test(dataProvider = "dataCartesianRadianConversion")
	public void testCartesianRadianConversion(final double aLatitude, final double aLongitude, final double aX,
			final double aY, final double aZ) {
		final CartesianPoint cartesian = CartesianPoint.fromRadian(new Radian(aLatitude, aLongitude));
		Assert.assertEquals(cartesian.getX(), aX, Math.abs(aX / 100));
		Assert.assertEquals(cartesian.getY(), aY, Math.abs(aY / 100));
		Assert.assertEquals(cartesian.getZ(), aZ, Math.abs(aZ / 100));

		final Radian radian = Radian.fromCartesian(cartesian);
		Assert.assertEquals(radian.getLatitude(), aLatitude, Math.abs(aLatitude / 100));
		Assert.assertEquals(radian.getLongitude(), aLongitude, Math.abs(aLongitude / 100));
	}
	
	@Test
	public void testEquals(){
		final Radian radA = new Radian(4.582, 30.2054);
		final Radian radB = new Radian(4.582, 30.2054);
		final Radian radC = new Radian(13.234, 50.233);
		
		Assert.assertTrue(radA.equals(radB));
		Assert.assertFalse(radA.equals(radC));
		
		final CartesianPoint cartA = CartesianPoint.fromRadian(radA);
		final CartesianPoint cartB = CartesianPoint.fromRadian(radB);
		final CartesianPoint cartC = CartesianPoint.fromRadian(radC);
		
		Assert.assertTrue(cartA.equals(cartB));
		Assert.assertFalse(cartA.equals(cartC));
	}
}
