/*
GeoTag

Copyright (C) 2015  Joachim von Eichborn

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package joachimeichborn.geotag.ui.parts;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.selections.PictureSelection;
import joachimeichborn.geotag.thumbnail.ThumbnailConsumer;
import joachimeichborn.geotag.thumbnail.ThumbnailKey;
import joachimeichborn.geotag.thumbnail.ThumbnailRepo;

public class PicturePreviewView implements ThumbnailConsumer {
	private static final String PART_ID = "geotag.part.picturepreview";

	private final ThumbnailRepo thumbnailRepo;
	private ImageIcon thumbnail;
	private JLabel thumbnailLabel;
	private Composite thumbnailContainer;
	private MPart previewPart;
	private boolean visible;

	@Inject
	private EPartService partService;

	private Path lastFile;

	public PicturePreviewView() {
		thumbnailRepo = ThumbnailRepo.getInstance();
		visible = false;
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		thumbnail = new ImageIcon();
		thumbnailLabel = new JLabel(thumbnail);
		thumbnailContainer = new Composite(aParent, SWT.EMBEDDED);
		final Frame frame = SWT_AWT.new_Frame(thumbnailContainer);
		frame.add(thumbnailLabel);
		final Color color = aParent.getBackground();
		frame.setBackground(new java.awt.Color(color.getGreen(), color.getGreen(), color.getBlue()));
		color.dispose();

		previewPart = partService.findPart(PART_ID);

		visible = previewPart.isVisible();

		partService.addPartListener(new IPartListener() {
			@Override
			public void partVisible(final MPart aPart) {
				if (!visible && aPart.equals(previewPart)) {
					visible = true;
					drawPreview();
				}
			}

			@Override
			public void partHidden(final MPart aPart) {
				if (aPart.equals(previewPart)) {
					visible = false;
				}
			}

			@Override
			public void partDeactivated(final MPart aPart) {
			}

			@Override
			public void partBroughtToTop(final MPart aPart) {
			}

			@Override
			public void partActivated(final MPart aPart) {
				if (aPart.equals(previewPart)) {
					drawPreview();
				}
			}
		});
	}

	private void drawPreview() {
		if (lastFile != null && thumbnailContainer != null) {
			final ThumbnailKey key = new ThumbnailKey(lastFile.toString(), thumbnailContainer.getSize().x,
					thumbnailContainer.getSize().y);
			final BufferedImage previewImage = thumbnailRepo.getThumbnail(key, false, this);
			drawPreview(previewImage);
		}

	}

	private void drawPreview(final BufferedImage aImage) {
		thumbnail.setImage(aImage);
		thumbnailContainer.setVisible(true);
		thumbnailLabel.repaint();
	}

	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional PictureSelection aPictureSelection) {
		if (aPictureSelection != null) {
			final List<Picture> pictures = aPictureSelection.getSelection();
			if (pictures.size() == 1) {
				final Picture picture = pictures.get(0);
				lastFile = picture.getFile();

				if (visible) {
					drawPreview();
				}
			}
		}
	}

	@Override
	public void thumbnailReady(final ThumbnailKey aKey, final BufferedImage aImage) {
		if (aKey.getFile().equals(lastFile.toString())) {
			if (aImage.getWidth() > aKey.getWidth() || aImage.getHeight() > aKey.getHeight()) {
				thumbnailRepo.getThumbnail(aKey, false, this);
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						drawPreview(aImage);
					}
				});
			}
		}
	}
}