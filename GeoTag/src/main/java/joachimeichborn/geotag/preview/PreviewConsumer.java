package joachimeichborn.geotag.preview;

import java.awt.image.BufferedImage;

/**
 * Interface to be implemented by classes that use previews. The only function is called once a preview is ready
 * 
 * @author Joachim von Eichborn
 */
public interface PreviewConsumer {
	/**
	 * Called when a preview is ready
	 * @param aKey The key that was used to request the preview
	 * @param aImage the created preview
	 */
	public void previewReady(final PreviewKey aKey, final BufferedImage aImage);
}
