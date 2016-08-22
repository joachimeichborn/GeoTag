package joachimeichborn.geotag.utils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import joachimeichborn.geotag.model.Coordinates;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;

public class PictureAnnotatorTest {
	@DataProvider
	public Object[][] testAnnotationProvider() {
		final List<Object[]> data = new ArrayList<>();

		final Coordinates coord1 = new Coordinates(1.1, 1.2, 1.3);
		final Coordinates coord2 = new Coordinates(2.1, 2.2, 2.3);
		final Coordinates coord3 = new Coordinates(3.1, 3.2, 3.3);
		{
			final List<Track> tracks = Collections.emptyList();

			final List<Picture> pictures = Collections.emptyList();
			final List<Picture> annotatedPictures = Collections.emptyList();
			final List<Picture> nonAnnotatedPictures = Collections.emptyList();

			final int tolerance = 30_000;
			final boolean overwrite = false;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		{
			final List<PositionData> positions1 = Lists
					.newArrayList(new PositionData(coord1, "2000-01-01T05:00:00Z", "A", 25));
			final List<PositionData> positions2 = Lists
					.newArrayList(new PositionData(coord2, "2000-01-01T06:00:00Z", "A", 40));
			final List<Track> tracks = Lists.newArrayList(new Track(Paths.get("track1.kml"), positions1),
					new Track(Paths.get("track2.kml"), positions2));
			final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2000:01:01 05:20:00", null, null);
			final List<Picture> pictures = Lists.newArrayList(pic1);
			final Picture pic1Annotated = new Picture(pic1.getFile(), pic1.getTime(), coord1, null);
			final List<Picture> annotatedPictures = Lists.newArrayList(pic1Annotated);
			final List<Picture> nonAnnotatedPictures = Collections.emptyList();

			final int tolerance = 20;
			final boolean overwrite = false;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		{
			final List<PositionData> positions1 = Lists
					.newArrayList(new PositionData(coord1, "2000-01-01T05:00:00Z", "A", 25));
			final List<PositionData> positions2 = Lists
					.newArrayList(new PositionData(coord2, "2000-01-01T06:00:00Z", "A", 40));
			final List<Track> tracks = Lists.newArrayList(new Track(Paths.get("track1.kml"), positions1),
					new Track(Paths.get("track2.kml"), positions2));
			final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2000:01:01 05:40:00", null, null);
			final List<Picture> pictures = Lists.newArrayList(pic1);
			final Picture pic1Annotated = new Picture(pic1.getFile(), pic1.getTime(), coord2, null);
			final List<Picture> annotatedPictures = Lists.newArrayList(pic1Annotated);
			final List<Picture> nonAnnotatedPictures = Collections.emptyList();

			final int tolerance = 20;
			final boolean overwrite = false;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		{
			final List<PositionData> positions1 = Lists
					.newArrayList(new PositionData(coord1, "2000-01-01T05:00:00Z", "A", 25));
			final List<PositionData> positions2 = Lists
					.newArrayList(new PositionData(coord2, "2000-01-01T06:00:00Z", "A", 40));
			final List<Track> tracks = Lists.newArrayList(new Track(Paths.get("track1.kml"), positions1),
					new Track(Paths.get("track2.kml"), positions2));
			final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2000:01:01 05:40:00", coord3, null);
			final List<Picture> pictures = Lists.newArrayList(pic1);
			final List<Picture> annotatedPictures = Collections.emptyList();
			final List<Picture> nonAnnotatedPictures = Lists.newArrayList(pic1);

			final int tolerance = 20;
			final boolean overwrite = false;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		{
			final List<PositionData> positions1 = Lists
					.newArrayList(new PositionData(coord1, "2000-01-01T05:00:00Z", "A", 25));
			final List<PositionData> positions2 = Lists
					.newArrayList(new PositionData(coord2, "2000-01-01T06:00:00Z", "A", 40));
			final List<Track> tracks = Lists.newArrayList(new Track(Paths.get("track1.kml"), positions1),
					new Track(Paths.get("track2.kml"), positions2));
			final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2000:01:01 05:40:00", coord3, null);
			final List<Picture> pictures = Lists.newArrayList(pic1);
			final Picture pic1Annotated = new Picture(pic1.getFile(), pic1.getTime(), coord2, null);
			final List<Picture> annotatedPictures = Lists.newArrayList(pic1Annotated);
			final List<Picture> nonAnnotatedPictures = Collections.emptyList();

			final int tolerance = 20;
			final boolean overwrite = true;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		{
			final List<PositionData> positions1 = Lists
					.newArrayList(new PositionData(coord1, "2000-01-01T05:00:00Z", "A", 25));
			final List<PositionData> positions2 = Lists
					.newArrayList(new PositionData(coord2, "2000-01-01T06:00:00Z", "A", 40));
			final List<Track> tracks = Lists.newArrayList(new Track(Paths.get("track1.kml"), positions1),
					new Track(Paths.get("track2.kml"), positions2));
			final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2000:01:01 05:21:00", null, null);
			final List<Picture> pictures = Lists.newArrayList(pic1);
			final List<Picture> annotatedPictures = Collections.emptyList();
			final List<Picture> nonAnnotatedPictures = Lists.newArrayList(pic1);

			final int tolerance = 20;
			final boolean overwrite = false;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		{
			final List<PositionData> positions1 = Lists
					.newArrayList(new PositionData(coord1, "2000-01-01T05:00:00Z", "A", 25));
			final List<PositionData> positions2 = Lists.newArrayList(
					new PositionData(coord2, "2000-01-01T06:00:00Z", "A", 40),
					new PositionData(coord3, "2000-01-01T07:00:00Z", "A", 10));
			final List<Track> tracks = Lists.newArrayList(new Track(Paths.get("track1.kml"), positions1),
					new Track(Paths.get("track2.kml"), positions2));
			final Picture pic1 = new Picture(Paths.get("pic1.jpg"), "2000:01:01 05:15:10", coord3, null);
			final Picture pic2 = new Picture(Paths.get("pic2.jpg"), "2000:01:01 05:15:20", null, null);
			final Picture pic3 = new Picture(Paths.get("pic3.jpg"), "2000:01:01 05:39:00", null, null);
			final Picture pic4 = new Picture(Paths.get("pic4.jpg"), "2000:01:01 05:40:00", null, null);
			final Picture pic5 = new Picture(Paths.get("pic5.jpg"), "2000:01:01 06:45:00", null, null);
			final List<Picture> pictures = Lists.newArrayList(pic1, pic2, pic3, pic4, pic5);
			final Picture pic2Annotated = new Picture(pic2.getFile(), pic2.getTime(), coord1, null);
			final Picture pic4Annotated = new Picture(pic4.getFile(), pic4.getTime(), coord2, null);
			final Picture pic5Annotated = new Picture(pic5.getFile(), pic5.getTime(), coord3, null);
			final List<Picture> annotatedPictures = Lists.newArrayList(pic2Annotated, pic4Annotated, pic5Annotated);
			final List<Picture> nonAnnotatedPictures = Lists.newArrayList(pic1, pic3);

			final int tolerance = 20;
			final boolean overwrite = false;

			data.add(new Object[] { tracks, pictures, tolerance, overwrite, annotatedPictures, nonAnnotatedPictures });
		}
		return data.toArray(new Object[0][0]);
	}

	@Test(dataProvider = "testAnnotationProvider")
	public void testAnnotation(final List<Track> aTracks, final List<Picture> aPictures, final int aTolerance,
			final boolean aOverwrite, final List<Picture> aExpectedAnnotated,
			final List<Picture> aExpectedNonAnnotated) {
		final PictureAnnotator annotator = new PictureAnnotator(aTracks, aPictures, aTolerance, aOverwrite);

		annotator.computeMatches();
		
		Assert.assertEquals(annotator.getAnnotatedPictures(), aExpectedAnnotated);
		Assert.assertEquals(annotator.getNonAnnotatedPictures(), aExpectedNonAnnotated);
	}
}
