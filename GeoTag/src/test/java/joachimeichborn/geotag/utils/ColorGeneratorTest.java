package joachimeichborn.geotag.utils;

import org.eclipse.swt.graphics.RGB;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ColorGeneratorTest {
	@Test
	public void testConsecutiveColorsDiffer() {
		final ColorGenerator generator = ColorGenerator.getInstance();

		RGB lastColor = generator.getNextColor();
		for (int i = 0; i < 100; i++) {
			final RGB color = generator.getNextColor();
			Assert.assertNotEquals(lastColor, color);
			lastColor = color;
		}
	}

	@Test
	public void testDifferentGeneratorsColorsDiffer() {
		final ColorGenerator generator1 = ColorGenerator.getInstance();
		final ColorGenerator generator2 = ColorGenerator.getInstance();
		
		Assert.assertNotEquals(generator1.getNextColor(), generator2.getNextColor());
	}
}
