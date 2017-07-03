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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.geocode.Geocoder;
import joachimeichborn.geotag.geocode.GeocodingProvider;
import joachimeichborn.geotag.handlers.PictureLoader;
import joachimeichborn.geotag.io.jpeg.PictureAnnotationException;
import joachimeichborn.geotag.io.jpeg.PictureMetadataWriter;
import joachimeichborn.geotag.model.Geocoding;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PicturesRepo;
import joachimeichborn.geotag.model.selections.PictureSelection;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerLabelProvider;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerObservableLabelProvider;
import joachimeichborn.geotag.ui.preferences.GeocodingPreferences;
import joachimeichborn.geotag.ui.tablecomparators.PictureViewerComparator;

public class GeocodingView {
	private static final Logger LOGGER = Logger.getLogger(GeocodingView.class.getSimpleName());
	
	private static final int MINIMAL_GRID_HEIGHT = 100;
	private static final String PICTURE_SELECTION_MSG = "Selected %d pictures, %d of them already contain geocoding information";
	private static final String GEOCODED_PICTURES_MSG = "%d pictures successfully geocoded";
	private static final String NON_GEOCODED_PICTURES_MSG = "%d pictures could not be geocoded";
	private static final String[] ALL_COLUMNS = new String[] { PictureViewerLabelProvider.NAME_COLUMN,
			PictureViewerLabelProvider.TIME_COLUMN, PictureViewerLabelProvider.COORDINATES_COLUMN,
			PictureViewerLabelProvider.LOCATION_COLUMN, PictureViewerLabelProvider.CITY_COLUMN,
			PictureViewerLabelProvider.SUBLOCATION_COLUMN, PictureViewerLabelProvider.PROVINCE_STATE_COLUMN,
			PictureViewerLabelProvider.COUNTRY_CODE_COLUMN, PictureViewerLabelProvider.COUNTRY_NAME_COLUMN };
	private static final String[] CORE_COLUMNS = new String[] { PictureViewerLabelProvider.NAME_COLUMN,
			PictureViewerLabelProvider.TIME_COLUMN, PictureViewerLabelProvider.COORDINATES_COLUMN };

	@Inject
	@Preference(nodePath = LifeCycleManager.PREFERENCES_NODE, value = GeocodingPreferences.GEOCODING_PROVIDER)
	private String aGeocodingProviderName;

	private final ESelectionService selectionService;
	private final MDirtyable dirtyable;
	private final IEclipseContext eclipseContext;
	private final UISynchronize sync;
	private final PicturesRepo picturesRepo;
	private TableViewer inputPictureViewer;
	private TableViewer geocodedPictureViewer;
	private Button geocodeButton;
	private PictureSelection selectedPictures;
	private List<Picture> geocodedPictures;
	private Label selectedPicturesLabel;
	private Button overwriteButton;
	private Label geocodedPicturesLabel;
	private Button saveGeocodingButton;
	private Button clearGeocodingButton;
	private final List<Picture> nonGeocodedPictures;
	private TableViewer nonGeocodedPictureViewer;
	private Label nonGeocodedPicturesLabel;
	private boolean geocodingInProgress;

	@Inject
	public GeocodingView(final PicturesRepo aPicturesRepo, final UISynchronize aSync, final IEclipseContext aEclipseContext, final MDirtyable aDirtyable, final ESelectionService aSelectionService) {
 		picturesRepo = aPicturesRepo;
		sync = aSync;
		eclipseContext = aEclipseContext;
		dirtyable = aDirtyable;
		selectionService = aSelectionService;

		selectedPictures = new PictureSelection();
		geocodedPictures = new LinkedList<>();
		nonGeocodedPictures = new LinkedList<>();
	}

	@PostConstruct
	public void postConstruct(final Composite aParent) {
		final SashForm pane = new SashForm(aParent, SWT.BORDER | SWT.SMOOTH | SWT.VERTICAL);

		final Composite upperPane = new Composite(pane, SWT.NONE);
		upperPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		initializeInputPane(upperPane);

		final Composite lowerPane = new Composite(pane, SWT.NONE);
		lowerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		initializeResultPane(lowerPane);

		updateButtonStates();
	}

	private void initializeInputPane(final Composite aParent) {
		aParent.setLayout(new GridLayout(2, false));

		new Label(aParent, SWT.NONE).setText("Select pictures");

		initializeInputPictureList(aParent);
		initializeButtons(aParent);

	}

	private void initializeResultPane(final Composite aParent) {
		aParent.setLayout(new GridLayout(2, false));

		final SashForm pane = new SashForm(aParent, SWT.BORDER | SWT.SMOOTH);
		final GridData sashPaneLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		sashPaneLayoutData.horizontalSpan = 2;
		pane.setLayoutData(sashPaneLayoutData);

		final Composite leftPane = new Composite(pane, SWT.NONE);
		leftPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		initializeGeocodedPicturePane(leftPane);

		final Composite rightPane = new Composite(pane, SWT.NONE);
		rightPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		initializeNonGeocodedPicturesPane(rightPane);
		pane.setWeights(new int[] { 3, 1 });

		saveGeocodingButton = new Button(aParent, SWT.NONE);
		saveGeocodingButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		saveGeocodingButton.setText("Save geocoded pictures");
		saveGeocodingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				savePictureGeocoding();
			}
		});

		clearGeocodingButton = new Button(aParent, SWT.NONE);
		clearGeocodingButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		clearGeocodingButton.setText("Discard geocoding");
		clearGeocodingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				geocodedPictures.clear();
				geocodedPictureViewer.setInput(geocodedPictures);
				dirtyable.setDirty(false);
				updateButtonStates();
			}
		});
	}

	private void initializeNonGeocodedPicturesPane(final Composite aParent) {
		aParent.setLayout(new GridLayout(1, false));

		new Label(aParent, SWT.NONE).setText("Non-Geocoded pictures");

		initializeNonGeocodedPicturesList(aParent);

		nonGeocodedPicturesLabel = new Label(aParent, SWT.NONE);
		nonGeocodedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nonGeocodedPicturesLabel.setText(String.format(NON_GEOCODED_PICTURES_MSG, 0));
	}

	private void initializeGeocodedPicturePane(final Composite aParent) {
		aParent.setLayout(new GridLayout(1, false));

		new Label(aParent, SWT.NONE).setText("Geocoded pictures");

		initializeGeocodedPictureList(aParent);

		geocodedPicturesLabel = new Label(aParent, SWT.NONE);
		geocodedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		geocodedPicturesLabel.setText(String.format(GEOCODED_PICTURES_MSG, 0));
	}

	private void initializeNonGeocodedPicturesList(final Composite aParent) {
		nonGeocodedPictureViewer = new TableViewer(aParent,
				SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		final GridData pictureListGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		pictureListGridData.minimumHeight = MINIMAL_GRID_HEIGHT;
		nonGeocodedPictureViewer.getControl().setLayoutData(pictureListGridData);
		nonGeocodedPictureViewer.getTable().setHeaderVisible(true);
		final PictureViewerComparator comparator = new PictureViewerComparator();
		nonGeocodedPictureViewer.setComparator(comparator);
		for (final String columnHeader : CORE_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(nonGeocodedPictureViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					comparator.setColumn(columnHeader);
					nonGeocodedPictureViewer.getTable().setSortDirection(comparator.getDirection());
					nonGeocodedPictureViewer.getTable().setSortColumn(column);
					nonGeocodedPictureViewer.refresh();
				};
			});
		}
		nonGeocodedPictureViewer.getControl().addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					nonGeocodedPictureViewer.setSelection(new StructuredSelection(picturesRepo.getPictures()));
				}
			}
		});
		nonGeocodedPictureViewer.setLabelProvider(new PictureViewerLabelProvider(Arrays.asList(CORE_COLUMNS)));
		nonGeocodedPictureViewer.setContentProvider(new ArrayContentProvider());
		nonGeocodedPictureViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				LOGGER.fine("Selected " + selection.size() + " pictures");
				final PictureSelection pictures = new PictureSelection(selection);
				selectionService.setSelection(pictures);
			}
		});
	}

	@Persist
	public void savePictureGeocoding() {
		LOGGER.info("Writing " + geocodedPictures.size() + " picture geocodings");

		final List<Path> failedPictures = new LinkedList<>();

		final String timestamp = String.valueOf(System.currentTimeMillis());
		for (final Picture picture : geocodedPictures) {
			final PictureMetadataWriter metadataWriter = new PictureMetadataWriter(picture, timestamp);
			ContextInjectionFactory.inject(metadataWriter, eclipseContext);
			try {
				metadataWriter.writeGeocodingMetadata();
			} catch (PictureAnnotationException e) {
				LOGGER.log(Level.SEVERE, "Annotating geocoding for " + picture.getFile() + " failed: " + e.getMessage(),
						e);
				failedPictures.add(picture.getFile());
			}
		}

		LOGGER.info("Finished writing picture geocodings");

		if (!failedPictures.isEmpty()) {
			MessageDialog.openError(new Shell(Display.getCurrent()),
					failedPictures.size() + " pictures failed in geocoding annotation",
					failedPictures.size()
							+ " pictures could not be annotated with their geocoding. Please check the log, if picture corruption might has happened.\n\nAffected pictures are:\n"
							+ StringUtils.join(failedPictures, "\n"));
		}

		final List<Path> reloadPictureFiles = new LinkedList<>();
		geocodedPictures.forEach(picture -> reloadPictureFiles.add(picture.getFile()));

		geocodedPictures.clear();
		geocodedPictureViewer.refresh();
		nonGeocodedPictures.clear();
		nonGeocodedPictureViewer.refresh();
		dirtyable.setDirty(false);
		updateButtonStates();

		final PictureLoader pictureLoader = new PictureLoader();
		ContextInjectionFactory.inject(pictureLoader, eclipseContext);
		pictureLoader.openPictures(reloadPictureFiles);
	}

	private void updateButtonStates() {
		geocodeButton.setEnabled(!geocodingInProgress && !dirtyable.isDirty() && selectedPictures.getSelection().size() > 0);
		saveGeocodingButton.setEnabled(!geocodingInProgress && dirtyable.isDirty());
		clearGeocodingButton.setEnabled(!geocodingInProgress && dirtyable.isDirty());
		inputPictureViewer.getControl().setEnabled(!geocodingInProgress);
		overwriteButton.setEnabled(!geocodingInProgress);
	}

	private void initializeInputPictureList(final Composite aParent) {
		inputPictureViewer = new TableViewer(aParent,
				SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		final GridData pictureListGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		pictureListGridData.minimumHeight = MINIMAL_GRID_HEIGHT;
		pictureListGridData.horizontalSpan = 2;
		inputPictureViewer.getControl().setLayoutData(pictureListGridData);
		inputPictureViewer.getTable().setHeaderVisible(true);
		final PictureViewerComparator comparator = new PictureViewerComparator();
		inputPictureViewer.setComparator(comparator);
		for (final String columnHeader : ALL_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(inputPictureViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					comparator.setColumn(columnHeader);
					inputPictureViewer.getTable().setSortDirection(comparator.getDirection());
					inputPictureViewer.getTable().setSortColumn(column);
					inputPictureViewer.refresh();
				};
			});
		}
		inputPictureViewer.getControl().addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					inputPictureViewer.setSelection(new StructuredSelection(picturesRepo.getPictures()));
				}
			}
		});
		bindPictureViewer();

		inputPictureViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				LOGGER.fine("Selected " + selection.size() + " pictures");

				selectedPictures = new PictureSelection(selection);

				int withGeocoding = 0;
				for (final Picture picture : selectedPictures.getSelection()) {
					if (picture.getGeocoding() != null) {
						withGeocoding++;
					}
				}

				selectedPicturesLabel.setText(String.format(PICTURE_SELECTION_MSG, selectedPictures.getSelection().size(), withGeocoding));
				updateButtonStates();

				selectionService.setSelection(selectedPictures);
			}
		});

		selectedPicturesLabel = new Label(aParent, SWT.NONE);
		selectedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		selectedPicturesLabel.setText(String.format(PICTURE_SELECTION_MSG, 0, 0));

		overwriteButton = new Button(aParent, SWT.CHECK);
		overwriteButton.setText("Overwrite existing geocoding");
	}

	private void bindPictureViewer() {
		final List<IBeanValueProperty> properties = new LinkedList<>();
		properties.add(BeanProperties.value(Picture.class, Picture.FILE_PROPERTY));
		properties.add(BeanProperties.value(Picture.class, Picture.TIME_PROPERTY));
		properties.add(BeanProperties.value(Picture.class, Picture.COORDINATES_PROPERTY));
		final IBeanValueProperty geocoding = BeanProperties.value(Picture.class, Picture.GEOCODING_PROPERTY);
		final IBeanValueProperty locationName = BeanProperties.value(Geocoding.class, Geocoding.LOCATION_NAME_PROPERTY);
		properties.add(geocoding.value(locationName));
		final IBeanValueProperty city = BeanProperties.value(Geocoding.class, Geocoding.CITY_PROPERTY);
		properties.add(geocoding.value(city));
		final IBeanValueProperty sublocation = BeanProperties.value(Geocoding.class, Geocoding.SUBLOCATION_PROPERTY);
		properties.add(geocoding.value(sublocation));
		final IBeanValueProperty province = BeanProperties.value(Geocoding.class, Geocoding.PROVINCE_STATE_PROPERTY);
		properties.add(geocoding.value(province));
		final IBeanValueProperty countryCode = BeanProperties.value(Geocoding.class, Geocoding.COUNTRY_CODE_PROPERTY);
		properties.add(geocoding.value(countryCode));
		final IBeanValueProperty countryName = BeanProperties.value(Geocoding.class, Geocoding.COUNTRY_NAME_PROPERTY);
		properties.add(geocoding.value(countryName));
		final IBeanValueProperty[] propertiesArray = properties.toArray(new IBeanValueProperty[properties.size()]);

		final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		inputPictureViewer.setContentProvider(contentProvider);
		final IObservableMap[] observables = Properties.observeEach(contentProvider.getKnownElements(),
				propertiesArray);
		inputPictureViewer
				.setLabelProvider(new PictureViewerObservableLabelProvider(observables, Arrays.asList(ALL_COLUMNS)));
		final IObservableList input = BeanProperties.list(PicturesRepo.class, PicturesRepo.PICTURES_PROPERTY)
				.observe(picturesRepo);

		inputPictureViewer.setInput(input);
	}

	private void initializeButtons(final Composite aParent) {
		geocodeButton = new Button(aParent, SWT.NONE);
		final GridData geocodeButtonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		geocodeButtonLayoutData.horizontalSpan = 2;
		geocodeButton.setLayoutData(geocodeButtonLayoutData);
		geocodeButton.setText("Geocode pictures");
		geocodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				geocodePictures();
			}
		});
	}

	private void geocodePictures() {
		geocodingInProgress = true;
		updateButtonStates();

		LOGGER.info("Starting geocoding for " + selectedPictures.getSelection().size() + " pictures...");

		final boolean overwrite = overwriteButton.getSelection();
		final GeocodingProvider provider = GeocodingProvider.getByDisplayName(aGeocodingProviderName);
		LOGGER.info("Using " + provider.getDisplayName() + " as geocoding provider");
		final Geocoder geoCoder = provider.getGeocoder();

		final Job job = new Job("Geocoding pictures") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Geocoding " + selectedPictures.getSelection().size() + " pictures", selectedPictures.getSelection().size());

				for (final Picture picture : selectedPictures.getSelection()) {
					if (picture.getGeocoding() != null && !overwrite) {
						LOGGER.fine("Ignoring picture " + picture.getFile().toString() + " with existing geocoding");
						nonGeocodedPictures.add(picture);
					} else {
						final Geocoding geocoding = geoCoder.queryPosition(picture.getCoordinates());
						final Picture codedPicture = new Picture(picture.getFile(), picture.getTime(),
								picture.getCoordinates(), geocoding);

						if (geocoding != null) {
							geocodedPictures.add(codedPicture);
						} else {
							nonGeocodedPictures.add(picture);
						}
					}
					aMonitor.worked(1);
				}

				LOGGER.info("Geocoding " + selectedPictures.getSelection().size() + " pictures completed");

				aMonitor.done();
				geocodingInProgress = false;
				dirtyable.setDirty(geocodedPictures.size() > 0);

				sync.syncExec(new Runnable() {
					@Override
					public void run() {
						geocodedPictureViewer.setInput(geocodedPictures);
						geocodedPictureViewer.refresh();

						nonGeocodedPictureViewer.setInput(nonGeocodedPictures);
						nonGeocodedPictureViewer.refresh();

						updateButtonStates();
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private void initializeGeocodedPictureList(final Composite aParent) {
		geocodedPictureViewer = new TableViewer(aParent,
				SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		final GridData pictureListGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		pictureListGridData.minimumHeight = MINIMAL_GRID_HEIGHT;
		geocodedPictureViewer.getControl().setLayoutData(pictureListGridData);
		geocodedPictureViewer.getTable().setHeaderVisible(true);
		final PictureViewerComparator comparator = new PictureViewerComparator();
		geocodedPictureViewer.setComparator(comparator);
		for (final String columnHeader : ALL_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(geocodedPictureViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					comparator.setColumn(columnHeader);
					geocodedPictureViewer.getTable().setSortDirection(comparator.getDirection());
					geocodedPictureViewer.getTable().setSortColumn(column);
					geocodedPictureViewer.refresh();
				};
			});
		}
		geocodedPictureViewer.getControl().addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					geocodedPictureViewer.setSelection(new StructuredSelection(picturesRepo.getPictures()));
				}
			}
		});
		geocodedPictureViewer.setLabelProvider(new PictureViewerLabelProvider(Arrays.asList(ALL_COLUMNS)));
		geocodedPictureViewer.setContentProvider(new ArrayContentProvider());
		geocodedPictureViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				LOGGER.fine("Selected " + selection.size() + " pictures");
				final PictureSelection pictures = new PictureSelection(selection);
				selectionService.setSelection(pictures);
			}
		});
	}
}