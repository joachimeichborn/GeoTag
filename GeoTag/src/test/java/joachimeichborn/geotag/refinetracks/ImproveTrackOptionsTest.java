package joachimeichborn.geotag.refinetracks;

import org.testng.Assert;
import org.testng.annotations.Test;

import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;

public class ImproveTrackOptionsTest {
	@Test
	public void testEmptySetting() {
		final ImproveTrackOptionsBuilder builder = new ImproveTrackOptionsBuilder();

		final ImproveTrackOptions options = builder.build();

		Assert.assertFalse(options.getReplaceByAccuracyComparison());
		Assert.assertFalse(options.getFilterByAccuracyRadius());
		Assert.assertFalse(options.getFilterByPairwiseDistance());
		Assert.assertFalse(options.getInterpolatePositions());
		Assert.assertFalse(options.getRemoveDuplicates());
		Assert.assertFalse(options.getRemoveIrrelevantPositions());
	}

	@Test
	public void testInvalidRadiusSetting() {
		final ImproveTrackOptionsBuilder builder = new ImproveTrackOptionsBuilder();
		builder.setFilterByAccuracyRadius(true);

		try {
			builder.build();

			Assert.fail();
		} catch (Exception e) {
			// expected exception occurred
		}
	}

	@Test
	public void testInvalidfactorSetting() {
		final ImproveTrackOptionsBuilder builder = new ImproveTrackOptionsBuilder();
		builder.setFilterByPairwiseDistance(true);

		try {
			builder.build();

			Assert.fail();
		} catch (Exception e) {
			// expected exception occurred
		}
	}

	public void testFullSetting() {
		final ImproveTrackOptionsBuilder builder = new ImproveTrackOptionsBuilder();
		builder.setDistanceFactor(2.5);
		builder.setReplaceByAccuracyComparison(true);
		builder.setFilterByAccuracyRadius(true);
		builder.setFilterByPairwiseDistance(true);
		builder.setInterpolatePositions(true);
		builder.setRadiusThreshold(100);
		builder.setRemoveDuplicates(true);
		builder.setRemoveIrrelevantPositions(true);

		final ImproveTrackOptions options = builder.build();

		Assert.assertTrue(options.getReplaceByAccuracyComparison());
		Assert.assertTrue(options.getFilterByAccuracyRadius());
		Assert.assertTrue(options.getFilterByPairwiseDistance());
		Assert.assertTrue(options.getInterpolatePositions());
		Assert.assertTrue(options.getRemoveDuplicates());
		Assert.assertTrue(options.getRemoveIrrelevantPositions());
		Assert.assertEquals(options.getDistanceFactor(), new Double(2.5));
		Assert.assertEquals(options.getRadiusThreshold(), new Integer(100));
	}
}
