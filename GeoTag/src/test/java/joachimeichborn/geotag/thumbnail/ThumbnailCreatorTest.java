package joachimeichborn.geotag.thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class ThumbnailCreatorTest {
	@Test
	public void testCreation() throws InterruptedException, IOException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		FileUtils.copyURLToFile(ThumbnailCreator.class.getResource("img1.jpg"), new File(testDir, "img1.jpg"));
		FileUtils.copyURLToFile(ThumbnailCreator.class.getResource("img2.jpg"), new File(testDir, "img2.jpg"));
		FileUtils.copyURLToFile(ThumbnailCreator.class.getResource("img1.jpg"), new File(testDir, "img3.jpg"));
		FileUtils.copyURLToFile(ThumbnailCreator.class.getResource("img2.jpg"), new File(testDir, "img4.jpg"));

		final Set<ThumbnailKey> unprocessedKeys = new HashSet<>();
		final CountDownLatch allProcessedLatch = new CountDownLatch(1);

		final ThumbnailKey key1 = new ThumbnailKey(new File(testDir, "img1.jpg").getAbsolutePath(), 200, 100);
		final ThumbnailKey key2 = new ThumbnailKey(new File(testDir, "img2.jpg").getAbsolutePath(), 200, 100);
		final ThumbnailKey key3 = new ThumbnailKey(new File(testDir, "img3.jpg").getAbsolutePath(), 200, 100);
		final ThumbnailKey key4 = new ThumbnailKey(new File(testDir, "img4.jpg").getAbsolutePath(), 200, 100);

		final ThumbnailConsumer consumer = new ThumbnailConsumer() {
			@Override
			public void thumbnailReady(final ThumbnailKey aKey, final BufferedImage aImage) {
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

		final ThumbnailCreator creator = new ThumbnailCreator(consumer);

		creator.requestThumbnail(key1, false);
		unprocessedKeys.add(key1);
		creator.requestThumbnail(key2, false);
		unprocessedKeys.add(key2);
		creator.requestThumbnail(key3, true);
		unprocessedKeys.add(key3);
		creator.requestThumbnail(key4, true);
		unprocessedKeys.add(key4);

		allProcessedLatch.await();
	}
}
