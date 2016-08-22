package joachimeichborn.geotag.utils;

import org.testng.Assert;
import org.testng.annotations.Test;


public class LifoBlockingDequeTest {
	@Test
	public void testQueue() throws InterruptedException {
		final LifoBlockingDeque<String> queue = new LifoBlockingDeque<>();
		queue.add("a");
		
		queue.add("b");
		queue.addFirst("c");
		queue.addLast("1");
		queue.offer("d");
		queue.offerFirst("e");
		queue.offerLast("2");
		queue.put("f");
		queue.putFirst("g");
		queue.putLast("3");

		final StringBuilder sb = new StringBuilder();
		for (final String entry : queue) {
			sb.append(entry);
		}

		final String expectedOrder = "gfedcba123";
		Assert.assertEquals(sb.toString(), expectedOrder);
	}
}
