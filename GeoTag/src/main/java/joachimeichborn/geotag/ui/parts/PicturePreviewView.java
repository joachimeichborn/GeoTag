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
import joachimeichborn.geotag.preview.PreviewConsumer;
import joachimeichborn.geotag.preview.PreviewKey;
import joachimeichborn.geotag.preview.PreviewRepo;

public class PicturePreviewView implements PreviewConsumer {
	private static final String PART_ID = "geotag.part.picturepreview";

	private final PreviewRepo previewRepo;
	private final EPartService partService;
	private ImageIcon preview;
	private JLabel previewLabel;
	private Composite previewContainer;
	private MPart previewPart;
	private boolean visible;
	private Path lastFile;

	@Inject
	public PicturePreviewView(final PreviewRepo aPreviewRepo, final EPartService aPartService) {
		previewRepo = aPreviewRepo;
		partService = aPartService;
		
		visible = false;
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		preview = new ImageIcon();
		previewLabel = new JLabel(preview);
		previewContainer = new Composite(aParent, SWT.EMBEDDED);
		final Frame frame = SWT_AWT.new_Frame(previewContainer);
		frame.add(previewLabel);
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
		if (lastFile != null && previewContainer != null) {
			final PreviewKey key = new PreviewKey(lastFile.toString(), previewContainer.getSize().x,
					previewContainer.getSize().y);
			final BufferedImage previewImage = previewRepo.getPreview(key, false, this);
			drawPreview(previewImage);
		}

	}

	private void drawPreview(final BufferedImage aImage) {
		preview.setImage(aImage);
		previewContainer.setVisible(true);
		previewLabel.repaint();
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
	public void previewReady(final PreviewKey aKey, final BufferedImage aImage) {
		if (aKey.getFile().equals(lastFile.toString())) {
			if (aImage.getWidth() > aKey.getWidth() || aImage.getHeight() > aKey.getHeight()) {
				previewRepo.getPreview(aKey, false, this);
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
