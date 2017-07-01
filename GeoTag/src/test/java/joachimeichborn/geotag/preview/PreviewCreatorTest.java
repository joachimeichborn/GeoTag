package joachimeichborn.geotag.preview;

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

import joachimeichborn.geotag.preview.PreviewConsumer;
import joachimeichborn.geotag.preview.PreviewCreator;
import joachimeichborn.geotag.preview.PreviewKey;

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
}
