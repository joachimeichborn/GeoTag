package joachimeichborn.geotag.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PictureTest {
	@Test
	public void testEquals() {
		final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2012:01:01 10:22:33", null, null);
		final Picture pic2 = new Picture(Paths.get("pic1.jpg"), "2012:01:01 10:22:33", null, null);
		final Picture pic3 = new Picture(Paths.get("pic3.jpg"), "2012:01:01 10:22:33", null, null);
		final Picture pic4 = new Picture(Paths.get("pic1.jpg"), "2012:01:01 23:22:33", null, null);
		final Picture pic5 = new Picture(Paths.get("pic1.jpg"), "2012:01:01 10:22:33", new Coordinates(1, 2, 3), null);
		final Picture pic6 = new Picture(Paths.get("pic1.jpg"), "2012:01:01 10:22:33", null,
				new Geocoding.Builder().setCity("Dummyvill").build());

		Assert.assertTrue(pic1.equals(pic2));
		Assert.assertFalse(pic1.equals(pic3));
		Assert.assertFalse(pic1.equals(pic4));
		Assert.assertFalse(pic1.equals(pic5));
		Assert.assertFalse(pic1.equals(pic6));
	}

	@Test
	public void testConstructor() {
		final Path file = Paths.get("test.jpg");
		final String time = "2012:01:01 10:22:33";
		final Coordinates coord = new Coordinates(1, 2, 3);
		final Geocoding geocoding = new Geocoding.Builder().setCity("Dummyvill").build();
		final Picture pic = new Picture(file, time, coord, geocoding);

		Assert.assertEquals(pic.getFile(), file);
		Assert.assertEquals(pic.getTime(), time);
		Assert.assertEquals(pic.getCoordinates(), coord);
		Assert.assertEquals(pic.getGeocoding(), geocoding);
	}
}
