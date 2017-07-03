package joachimeichborn.geotag.misc;

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import joachimeichborn.geotag.misc.PictureOrientation;

public class PictureOrientationTest {
	@DataProvider
	public Object[][] dataOrientation() {
		final List<Object[]> data = new LinkedList<>();
		data.add(new Object[] { 1, PictureOrientation.HORIZONTAL_NORMAL, true });
		data.add(new Object[] { 2, PictureOrientation.MIRROR_HORIZONTAL, true });
		data.add(new Object[] { 3, PictureOrientation.ROTATE_180, true });
		data.add(new Object[] { 4, PictureOrientation.MIRROR_VERTICAL, true });
		data.add(new Object[] { 5, PictureOrientation.MIRROR_HORIZONTAL_AND_ROTATE_270_CW, false });
		data.add(new Object[] { 6, PictureOrientation.ROTATE_270_CW, false });
		data.add(new Object[] { 7, PictureOrientation.MIRROR_HORIZONTAL_AND_ROTATE_90_CW, false });
		data.add(new Object[] { 8, PictureOrientation.ROTATE_90_CW, false });
		data.add(new Object[] { 99, PictureOrientation.HORIZONTAL_NORMAL, true });
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "dataOrientation")
	public void testOrientation(final int aOrientationValue, final PictureOrientation aOrientation,
			final boolean aHorizontal) {
		final PictureOrientation orientation = PictureOrientation.getByMetadataValue(aOrientationValue);

		Assert.assertEquals(orientation, aOrientation);
		Assert.assertEquals(orientation.isHorizontal(), aHorizontal);
	}
}
