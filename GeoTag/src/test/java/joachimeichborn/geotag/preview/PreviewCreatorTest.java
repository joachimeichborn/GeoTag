package joachimeichborn.geotag.preview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import joachimeichborn.geotag.preview.PreviewCreator.Worker;

public class PreviewCreatorTest {
	@Test
	public void testCreation() throws InterruptedException, IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		FileUtils.copyURLToFile(PreviewCreator.class.getResource("img1.jpg"), new File(testDir, "img1.jpg"));
		FileUtils.copyURLToFile(PreviewCreator.class.getResource("img2.jpg"), new File(testDir, "img2.jpg"));
		FileUtils.copyURLToFile(PreviewCreator.class.getResource("img1.jpg"), new File(testDir, "img3.jpg"));
		FileUtils.copyURLToFile(PreviewCreator.class.getResource("img2.jpg"), new File(testDir, "img4.jpg"));

		final Set<PreviewKey> unprocessedKeys = new HashSet<>();
		final CountDownLatch allProcessedLatch = new CountDownLatch(1);

		final PreviewKey key1 = new PreviewKey(new File(testDir, "img1.jpg").getAbsolutePath(), 200, 100);
		final PreviewKey key2 = new PreviewKey(new File(testDir, "img2.jpg").getAbsolutePath(), 200, 100);
		final PreviewKey key3 = new PreviewKey(new File(testDir, "img3.jpg").getAbsolutePath(), 200, 100);
		final PreviewKey key4 = new PreviewKey(new File(testDir, "img4.jpg").getAbsolutePath(), 200, 100);

		final PreviewConsumer consumer = new PreviewConsumer() {
			@Override
			public void previewReady(final PreviewKey aKey, final BufferedImage aImage) {
				if (!unprocessedKeys.remove(aKey)) {
					Assert.fail("Unexpected key " + aKey);
				}

				if (aKey.equals(key1)) {
					Assert.assertEquals(aImage.getWidth(), 133);
					Assert.assertEquals(aImage.getHeight(), 100);
				} else if (aKey.equals(key2)) {
					Assert.assertEquals(aImage.getWidth(), 75);
					Assert.assertEquals(aImage.getHeight(), 100);
				} else if (aKey.equals(key3)) {
					Assert.assertEquals(aImage.getWidth(), 133);
					Assert.assertEquals(aImage.getHeight(), 100);
				} else if (aKey.equals(key4)) {
					Assert.assertEquals(aImage.getWidth(), 100);
					Assert.assertEquals(aImage.getHeight(), 133);
				}

				if (unprocessedKeys.isEmpty()) {
					allProcessedLatch.countDown();
				}
			}
		};

		final PreviewCreator creator = new PreviewCreator(consumer);

		creator.requestPreview(key1, false);
		unprocessedKeys.add(key1);
		creator.requestPreview(key2, false);
		unprocessedKeys.add(key2);
		creator.requestPreview(key3, true);
		unprocessedKeys.add(key3);
		creator.requestPreview(key4, true);
		unprocessedKeys.add(key4);

		allProcessedLatch.await();
	}

	@DataProvider
	public Object[][] sizingData() {
		final List<Object[]> dpl = new LinkedList<>();
		dpl.add(new Object[] { "4to5_clockwise0.jpg", false, 384 / (480 / 225d), 225d });
		dpl.add(new Object[] { "4to5_clockwise90.jpg", false, 480 / (384 / 225d), 225d });
		dpl.add(new Object[] { "4to5_clockwise270.jpg", false, 480 / (384 / 225d), 225d });
		dpl.add(new Object[] { "16to9_clockwise0.jpg", false, 300d, 360 / (640 / 300d) });
		dpl.add(new Object[] { "16to9_clockwise90.jpg", false, 360 / (640 / 225d), 225d });
		dpl.add(new Object[] { "16to9_clockwise270.jpg", false, 360 / (640 / 225d), 225d });

		dpl.add(new Object[] { "4to5_clockwise0.jpg", true, 225d, 480 / (384 / 225d) });
		dpl.add(new Object[] { "4to5_clockwise90.jpg", true, 480 / (384 / 225d), 225d });
		dpl.add(new Object[] { "4to5_clockwise270.jpg", true, 480 / (384 / 225d), 225d });
		dpl.add(new Object[] { "16to9_clockwise0.jpg", true, 300d, 360 / (640 / 300d) });
		dpl.add(new Object[] { "16to9_clockwise90.jpg", true, 360 / (640 / 300d), 300d });
		dpl.add(new Object[] { "16to9_clockwise270.jpg", true, 360 / (640 / 300d), 300d });

		return dpl.toArray(new Object[dpl.size()][]);

	}

	@Test(dataProvider = "sizingData")
	public void sizingTest(final String aFileName, final boolean aRotatable, final double aExpectedWidth,
			final double aExpectedHeight) throws IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		final File testPicture = new File(testDir, aFileName);
		FileUtils.copyURLToFile(PreviewCreator.class.getResource(aFileName), testPicture);

		// each preview is requested twice, once in a size that is smaller than
		// the original picture and once in a size that is bigger than the
		// original picture
		final PreviewKey key1 = new PreviewKey(testPicture.getAbsolutePath(), 300, 225);
		final PreviewKey key2 = new PreviewKey(testPicture.getAbsolutePath(), 300 * 3, 225 * 3);

		final Set<PreviewKey> consumerInvocations = new HashSet<>();

		final PreviewConsumer consumer = new PreviewConsumer() {
			@Override
			public void previewReady(final PreviewKey aKey, final BufferedImage aImage) {
				consumerInvocations.add(aKey);

				if (aKey.equals(key1)) {
					Assert.assertEquals(aImage.getWidth(), (int) aExpectedWidth);
					Assert.assertEquals(aImage.getHeight(), (int) aExpectedHeight);
				} else if (aKey.equals(key2)) {
					Assert.assertEquals(aImage.getWidth(), (int) (aExpectedWidth * 3));
					Assert.assertEquals(aImage.getHeight(), (int) (aExpectedHeight * 3));
				} else {
					Assert.fail("Unexpected key " + aKey);
				}
			}
		};

		new Worker(key1, aRotatable, consumer).run();
		new Worker(key2, aRotatable, consumer).run();

		Assert.assertEquals(consumerInvocations.size(), 2);
	}
}
