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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import joachimeichborn.geotag.model.Geocoding;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PicturesRepo;
import joachimeichborn.geotag.model.selections.PictureSelection;
import joachimeichborn.geotag.thumbnail.ThumbnailConsumer;
import joachimeichborn.geotag.thumbnail.ThumbnailKey;
import joachimeichborn.geotag.thumbnail.ThumbnailRepo;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerLabelProvider;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerObservableLabelProvider;
import joachimeichborn.geotag.ui.tablecomparators.PictureViewerComparator;

public class PicturesView implements ThumbnailConsumer {
	private static final String PICTURES_PART_ID = "geotag.part.pictures";
	private static final String[] COLUMNS = new String[] { PictureViewerLabelProvider.NAME_COLUMN,
			PictureViewerLabelProvider.TIME_COLUMN, PictureViewerLabelProvider.COORDINATES_COLUMN };
	private static final String SELECTED_PICTURES = "%d picture(s) selected";
	private static final Logger logger = Logger.getLogger(PicturesView.class.getSimpleName());

	@Inject
	private ESelectionService selectionService;

	@Inject
	private EPartService partService;

	private TableViewer pictureViewer;
	private PicturesRepo picturesRepo;
	private ThumbnailRepo thumbnailRepo;
	private Label nameLabel;
	private Label pathLabel;
	private Composite thumbnailContainer;
	private Label locationNameLabel;
	private Label cityLabel;
	private Label sublocationLabel;
	private Label provinceLabel;
	private Label countryLabel;
	private ImageIcon thumbnail;
	private JLabel thumbnailLabel;
	private ThumbnailKey lastKey;
	private Label selectedPicturesLabel;

	public PicturesView() {
		picturesRepo = PicturesRepo.getInstance();
		thumbnailRepo = ThumbnailRepo.getInstance();
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		aParent.setLayout(new GridLayout(1, false));

		initializePictureList(aParent);

		selectedPicturesLabel = new Label(aParent, SWT.NONE);
		selectedPicturesLabel.setText(String.format(SELECTED_PICTURES, 0));
		selectedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		initializeDetailsSection(aParent);
	}

	private void initializePictureList(final Composite aParent) {
		pictureViewer = new TableViewer(aParent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		pictureViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pictureViewer.getTable().setHeaderVisible(true);
		final PictureViewerComparator comparator = new PictureViewerComparator();
		pictureViewer.setComparator(comparator);
		for (final String columnHeader : COLUMNS) {
			final TableViewerColumn viewverColumn = new TableViewerColumn(pictureViewer, SWT.NONE);
			final TableColumn column = viewverColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent aEvent) {
					comparator.setColumn(columnHeader);
					pictureViewer.getTable().setSortDirection(comparator.getDirection());
					pictureViewer.getTable().setSortColumn(column);
					pictureViewer.refresh();
				}
			});
		}
		pictureViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent aEvent) {
				switch (aEvent.character) {
					case 0x01: {
						pictureViewer.setSelection(new StructuredSelection(picturesRepo.getPictures()));
						break;
					}
					case SWT.DEL: {
						final List<Picture> selectedPictures = pictureViewer.getStructuredSelection().toList();
						picturesRepo.removePictures(selectedPictures);
						break;
					}
				}
			}
		});

		bindPictureViewer();

		pictureViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				logger.fine("Selected " + selection.size() + " pictures");
				final PictureSelection pictures = new PictureSelection(selection.toList());
				selectionService.setSelection(pictures);
			}
		});
	}

	private void bindPictureViewer() {
		final List<IBeanValueProperty> properties = new LinkedList<>();
		properties.add(BeanProperties.value(Picture.class, Picture.FILE_PROPERTY));
		properties.add(BeanProperties.value(Picture.class, Picture.TIME_PROPERTY));
		properties.add(BeanProperties.value(Picture.class, Picture.COORDINATES_PROPERTY));
		final IBeanValueProperty[] propertiesArray = properties.toArray(new IBeanValueProperty[properties.size()]);

		final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		pictureViewer.setContentProvider(contentProvider);
		final IObservableMap[] observables = Properties.observeEach(contentProvider.getKnownElements(),
				propertiesArray);
		pictureViewer.setLabelProvider(new PictureViewerObservableLabelProvider(observables, Arrays.asList(COLUMNS)));
		final IObservableList input = BeanProperties.list(PicturesRepo.class, PicturesRepo.PICTURES_PROPERTY)
				.observe(picturesRepo);

		pictureViewer.setInput(input);
	}

	private void initializeDetailsSection(final Composite aParent) {
		final Composite details = new Composite(aParent, SWT.BORDER);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		details.setLayout(new GridLayout(3, false));

		thumbnail = new ImageIcon();
		thumbnailLabel = new JLabel(thumbnail);
		thumbnailContainer = new Composite(details, SWT.EMBEDDED);
		final GridData thumbnailGridData = new GridData();
		thumbnailGridData.verticalSpan = 7;
		thumbnailGridData.heightHint = 160;
		thumbnailGridData.widthHint = 160;
		thumbnailContainer.setLayoutData(thumbnailGridData);
		final Frame frame = SWT_AWT.new_Frame(thumbnailContainer);
		frame.add(thumbnailLabel);
		final Color color = details.getBackground();
		frame.setBackground(new java.awt.Color(color.getGreen(), color.getGreen(), color.getBlue()));
		color.dispose();

		new Label(details, SWT.NONE).setText("Name: ");
		nameLabel = new Label(details, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Path: ");
		pathLabel = new Label(details, SWT.NONE);
		pathLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Location name: ");
		locationNameLabel = new Label(details, SWT.NONE);
		locationNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("City: ");
		cityLabel = new Label(details, SWT.NONE);
		cityLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Sublocation: ");
		sublocationLabel = new Label(details, SWT.NONE);
		sublocationLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Provice: ");
		provinceLabel = new Label(details, SWT.NONE);
		provinceLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Country: ");
		countryLabel = new Label(details, SWT.NONE);
		countryLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Inject
	public void setSelection(
			@Named(IServiceConstants.ACTIVE_SELECTION) @Optional final PictureSelection aPictureSelection) {
		final MPart activePart = partService.getActivePart();
		if (activePart != null && activePart.getElementId().equals(PICTURES_PART_ID)) {
			if (nameLabel != null && aPictureSelection != null) {
				final List<Picture> pictures = aPictureSelection.getSelection();
				selectedPicturesLabel.setText(String.format(SELECTED_PICTURES, pictures.size()));
				if (pictures.size() == 1) {
					final Picture picture = pictures.get(0);
					final Path file = picture.getFile();
					lastKey = new ThumbnailKey(file.toString(), 160, 120);
					thumbnail.setImage(thumbnailRepo.getThumbnail(lastKey, true, this));
					thumbnailContainer.setVisible(true);
					thumbnailLabel.repaint();
					nameLabel.setText(file.getFileName().toString());
					pathLabel.setText(file.getParent().toString());
					fillGeocodingDetails(picture.getGeocoding());
				} else {
					nameLabel.setText("");
					pathLabel.setText("");
					thumbnailContainer.setVisible(false);
					fillGeocodingDetails(null);
				}
			}
		}
	}

	private void fillGeocodingDetails(final Geocoding aGeoCoding) {
		if (aGeoCoding != null) {
			locationNameLabel.setText(aGeoCoding.getLocationName());
			cityLabel.setText(aGeoCoding.getCity());
			sublocationLabel.setText(aGeoCoding.getSublocation());
			provinceLabel.setText(aGeoCoding.getProvinceState());
			final String countryCode = aGeoCoding.getCountryCode();
			final String countryName = aGeoCoding.getCountryName();
			countryLabel.setText(
					StringUtils.join(countryCode, countryCode != null && countryName != null ? ", " : "", countryName));
		} else {
			locationNameLabel.setText("");
			cityLabel.setText("");
			sublocationLabel.setText("");
			provinceLabel.setText("");
			countryLabel.setText("");
		}
	}

	@Override
	public void thumbnailReady(final ThumbnailKey aKey, final BufferedImage aImage) {
		if (aKey.getFile().equals(lastKey.getFile())) {
			thumbnail.setImage(aImage);
			thumbnailLabel.repaint();
		}
	}
}
