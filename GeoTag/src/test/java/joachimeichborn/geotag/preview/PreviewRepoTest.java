package joachimeichborn.geotag.preview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import joachimeichborn.geotag.io.database.DatabaseAccess;
import joachimeichborn.geotag.preview.PreviewConsumer;
import joachimeichborn.geotag.preview.PreviewCreator;
import joachimeichborn.geotag.preview.PreviewKey;
import joachimeichborn.geotag.preview.PreviewRepo;

public class PreviewRepoTest {
	@Test
	public void testRepo() throws IOException, InterruptedException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		FileUtils.copyURLToFile(PreviewCreator.class.getResource("img1.jpg"), new File(testDir, "img1.jpg"));
		FileUtils.copyURLToFile(PreviewCreator.class.getResource("img2.jpg"), new File(testDir, "img2.jpg"));

		final CountDownLatch latch = new CountDownLatch(2);

		final PreviewKey key1 = new PreviewKey(new File(testDir, "img1.jpg").getAbsolutePath(), 200, 100);
		final PreviewKey key2 = new PreviewKey(new File(testDir, "img2.jpg").getAbsolutePath(), 200, 100);
		final PreviewKey key3 = new PreviewKey(new File(testDir, "img1.jpg").getAbsolutePath(), 100, 200);
		final PreviewKey key4 = new PreviewKey(new File(testDir, "img2.jpg").getAbsolutePath(), 100, 200);

		final PreviewConsumer consumer = new PreviewConsumer() {
			@Override
			public void previewReady(PreviewKey aKey, BufferedImage aImage) {
				latch.countDown();
			}
		};

		final DatabaseAccess database = new DatabaseAccess() {
			private final Map<PreviewKey, BufferedImage> data = new HashMap<>();

			@Override
			public void trim(final int aMaxNumberEntries) {
				throw new UnsupportedOperationException();
			}

			@Override
			public synchronized void savePreview(final PreviewKey aKey, final BufferedImage aPreview) {
				data.put(aKey, aPreview);
			}

			@Override
			public synchronized BufferedImage getPreview(final PreviewKey aKey) {
				return data.get(aKey);
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}
		};

		final PreviewRepo repo = PreviewRepo.getInstance();
		repo.setDatabase(database);

		// using the return value, we can see if an existing preview has been
		// used (in that case an image not equal to the placeholder is returned)
		BufferedImage image = null;
		image = repo.getPreview(key1, false, consumer);
		Assert.assertEquals(image, repo.placeholder);
		image = repo.getPreview(key2, false, consumer);
		Assert.assertEquals(image, repo.placeholder);

		// wait until both previews have been computed
		latch.await();

		image = repo.getPreview(key1, false, consumer);
		Assert.assertNotEquals(image, repo.placeholder);
		image = repo.getPreview(key2, false, consumer);
		Assert.assertNotEquals(image, repo.placeholder);

		image = repo.getPreview(key3, true, consumer);
		Assert.assertEquals(image, repo.placeholder);
		image = repo.getPreview(key4, true, consumer);
		Assert.assertNotEquals(image, repo.placeholder);
	}
}
