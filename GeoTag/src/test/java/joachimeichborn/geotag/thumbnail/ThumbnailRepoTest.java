package joachimeichborn.geotag.thumbnail;

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

public class ThumbnailRepoTest {
	@Test
	public void testRepo() throws IOException, InterruptedException {
		final File testDir = Files.createTempDir();
		testDir.deleteOnExit();

		FileUtils.copyURLToFile(ThumbnailCreator.class.getResource("img1.jpg"), new File(testDir, "img1.jpg"));
		FileUtils.copyURLToFile(ThumbnailCreator.class.getResource("img2.jpg"), new File(testDir, "img2.jpg"));

		final CountDownLatch latch = new CountDownLatch(2);

		final ThumbnailKey key1 = new ThumbnailKey(new File(testDir, "img1.jpg").getAbsolutePath(), 200, 100);
		final ThumbnailKey key2 = new ThumbnailKey(new File(testDir, "img2.jpg").getAbsolutePath(), 200, 100);
		final ThumbnailKey key3 = new ThumbnailKey(new File(testDir, "img1.jpg").getAbsolutePath(), 100, 200);
		final ThumbnailKey key4 = new ThumbnailKey(new File(testDir, "img2.jpg").getAbsolutePath(), 100, 200);

		final ThumbnailConsumer consumer = new ThumbnailConsumer() {
			@Override
			public void thumbnailReady(ThumbnailKey aKey, BufferedImage aImage) {
				latch.countDown();
			}
		};

		final DatabaseAccess database = new DatabaseAccess() {
			private final Map<ThumbnailKey, BufferedImage> data = new HashMap<>();

			@Override
			public void trim(final int aMaxNumberEntries) {
				throw new UnsupportedOperationException();
			}

			@Override
			public synchronized void saveThumbnail(final ThumbnailKey aKey, final BufferedImage aThumbnail) {
				data.put(aKey, aThumbnail);
			}

			@Override
			public synchronized BufferedImage getThumbnail(final ThumbnailKey aKey) {
				return data.get(aKey);
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}
		};

		final ThumbnailRepo repo = ThumbnailRepo.getInstance();
		repo.setDatabase(database);

		// using the return value, we can see if an existing thumbnail has been
		// used (in that case an image not equal to the placeholder is returned)
		BufferedImage image = null;
		image = repo.getThumbnail(key1, false, consumer);
		Assert.assertEquals(image, repo.placeholder);
		image = repo.getThumbnail(key2, false, consumer);
		Assert.assertEquals(image, repo.placeholder);

		// wait until both thumbnails have been computed
		latch.await();

		image = repo.getThumbnail(key1, false, consumer);
		Assert.assertNotEquals(image, repo.placeholder);
		image = repo.getThumbnail(key2, false, consumer);
		Assert.assertNotEquals(image, repo.placeholder);

		image = repo.getThumbnail(key3, true, consumer);
		Assert.assertEquals(image, repo.placeholder);
		image = repo.getThumbnail(key4, true, consumer);
		Assert.assertNotEquals(image, repo.placeholder);
	}
}
