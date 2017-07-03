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
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
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
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import joachimeichborn.geotag.handlers.OpenPicturesHandler;
import joachimeichborn.geotag.io.jpeg.PictureAnnotationException;
import joachimeichborn.geotag.io.jpeg.PictureMetadataWriter;
import joachimeichborn.geotag.model.Picture;
import joachimeichborn.geotag.model.PicturesRepo;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.model.TracksRepo;
import joachimeichborn.geotag.model.selections.PictureSelection;
import joachimeichborn.geotag.model.selections.TrackSelection;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerLabelProvider;
import joachimeichborn.geotag.ui.labelprovider.PictureViewerObservableLabelProvider;
import joachimeichborn.geotag.ui.labelprovider.TrackViewerObservableLabelProvider;
import joachimeichborn.geotag.ui.tablecomparators.PictureViewerComparator;
import joachimeichborn.geotag.ui.tablecomparators.TrackViewerComparator;
import joachimeichborn.geotag.utils.ColorPreviewImageGenerator;
import joachimeichborn.geotag.utils.PictureAnnotator;
import net.miginfocom.swt.MigLayout;

public class PositionAnnotationView {
	private static final int MINIMAL_GRID_HEIGHT = 100;
	private static final String NON_ANNOTATED_PICTURES_MSG = "%d pictures could not be mapped to a position";
	private static final String ANNOTATED_PICTURES_MSG = "%d pictures successfully annotated";
	private static final String PICTURE_SELECTION_MSG = "Selected %d pictures with %d position information";
	private static final String TRACK_SELECTION_MSG = "Selected %d tracks with %d positions";

	private class AnnotationViewerSelectionListener implements ISelectionChangedListener {

		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			logger.fine("Selected " + selection.size() + " pictures");
			selectionService.setSelection(new PictureSelection(selection));
		}
	}

	private static final String[] TRACK_VIEWER_COLUMNS = new String[] { TrackViewerObservableLabelProvider.NAME_COLUMN,
			TrackViewerObservableLabelProvider.POSITION_COUNT_COLUMN, TrackViewerObservableLabelProvider.COLOR_COLUMN };
	private static final Logger logger = Logger.getLogger(PositionAnnotationView.class.getSimpleName());
	private static final String[] PICTURE_VIEWER_COLUMNS = new String[] { PictureViewerLabelProvider.NAME_COLUMN,
			PictureViewerLabelProvider.TIME_COLUMN, PictureViewerLabelProvider.COORDINATES_COLUMN };

	@Inject
	private ESelectionService selectionService;

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	private MDirtyable dirtyable;

	@Inject
	private static UISynchronize sync;

	private TableViewer trackViewer;
	private TableViewer pictureViewer;
	private TrackSelection selectedTracks;
	private PictureSelection selectedPictures;
	private Label selectedTracksLabel;
	private Label selectedPicturesLabel;
	private Button computeAnnotationButton;
	private TableViewer annotatedPictureViewer;
	private Button saveAnnotationButton;
	private TableViewer nonAnnotatedPictureViewer;
	private Label annotatedPicturesLabel;
	private Label nonAnnotatedPicturesLabel;
	private List<Picture> annotatedPictures;
	private List<Picture> nonAnnotatedPictures;
	private Button overwriteButton;
	private Scale toleranceScale;
	private Label toleranceLabel;
	private Button clearAnnotationButton;
	private final ImageRegistry registry;
	private final ColorPreviewImageGenerator colorPreviewGenerator;
	private final PicturesRepo picturesRepo;
	private final TracksRepo tracksRepo;
	private boolean annotationInProgress;

	public PositionAnnotationView() {
		selectedTracks = new TrackSelection();
		selectedPictures = new PictureSelection();

		final Display display = Display.getCurrent();
		registry = new ImageRegistry(display);
		colorPreviewGenerator = new ColorPreviewImageGenerator(registry, display);

		tracksRepo = TracksRepo.getInstance();
		picturesRepo = PicturesRepo.getInstance();
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		final SashForm pane = new SashForm(aParent, SWT.BORDER | SWT.SMOOTH | SWT.VERTICAL);

		final Composite upperPane = new Composite(pane, SWT.NONE);
		upperPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		initializeUpperPane(upperPane);

		final Composite lowerPane = new Composite(pane, SWT.NONE);
		lowerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		initializeResultPane(lowerPane);

		updateButtonStates();
	}

	private void initializeResultPane(final Composite aParent) {
		aParent.setLayout(new GridLayout(2, true));

		new Label(aParent, SWT.NONE).setText("Annotated pictures");
		new Label(aParent, SWT.NONE).setText("Non-Annotated pictures");

		final GridData viewerConstraint = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewerConstraint.minimumHeight = MINIMAL_GRID_HEIGHT;

		annotatedPictureViewer = new TableViewer(aParent,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		annotatedPictureViewer.getControl().setLayoutData(viewerConstraint);
		final PictureViewerComparator annotatedPictureViewerComparator = new PictureViewerComparator();
		annotatedPictureViewer.setComparator(annotatedPictureViewerComparator);
		for (final String columnHeader : PICTURE_VIEWER_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(annotatedPictureViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(200);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					annotatedPictureViewerComparator.setColumn(columnHeader);
					annotatedPictureViewer.getTable().setSortDirection(annotatedPictureViewerComparator.getDirection());
					annotatedPictureViewer.getTable().setSortColumn(column);
					annotatedPictureViewer.refresh();
				}
			});
		}
		annotatedPictureViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					annotatedPictureViewer.setSelection(new StructuredSelection(annotatedPictures));
				}
			}
		});
		annotatedPictureViewer.setLabelProvider(new PictureViewerLabelProvider(Arrays.asList(PICTURE_VIEWER_COLUMNS)));
		annotatedPictureViewer.setContentProvider(new ArrayContentProvider());
		annotatedPictureViewer.getTable().setHeaderVisible(true);
		annotatedPictureViewer.addSelectionChangedListener(new AnnotationViewerSelectionListener());

		nonAnnotatedPictureViewer = new TableViewer(aParent,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		nonAnnotatedPictureViewer.getControl().setLayoutData(viewerConstraint);
		final PictureViewerComparator nonAnnotatedPictureViewerComparator = new PictureViewerComparator();
		nonAnnotatedPictureViewer.setComparator(nonAnnotatedPictureViewerComparator);
		for (final String columnHeader : PICTURE_VIEWER_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(nonAnnotatedPictureViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(200);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					nonAnnotatedPictureViewerComparator.setColumn(columnHeader);
					nonAnnotatedPictureViewer.getTable()
							.setSortDirection(nonAnnotatedPictureViewerComparator.getDirection());
					nonAnnotatedPictureViewer.getTable().setSortColumn(column);
					nonAnnotatedPictureViewer.refresh();
				}
			});
		}
		nonAnnotatedPictureViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					nonAnnotatedPictureViewer.setSelection(new StructuredSelection(nonAnnotatedPictures));
				}
			}
		});
		nonAnnotatedPictureViewer
				.setLabelProvider(new PictureViewerLabelProvider(Arrays.asList(PICTURE_VIEWER_COLUMNS)));
		nonAnnotatedPictureViewer.setContentProvider(new ArrayContentProvider());
		nonAnnotatedPictureViewer.getTable().setHeaderVisible(true);
		nonAnnotatedPictureViewer.addSelectionChangedListener(new AnnotationViewerSelectionListener());

		annotatedPicturesLabel = new Label(aParent, SWT.NONE);
		annotatedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		annotatedPicturesLabel.setText(String.format(ANNOTATED_PICTURES_MSG, 0));

		nonAnnotatedPicturesLabel = new Label(aParent, SWT.NONE);
		nonAnnotatedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nonAnnotatedPicturesLabel.setText(String.format(NON_ANNOTATED_PICTURES_MSG, 0));

		saveAnnotationButton = new Button(aParent, SWT.NONE);
		saveAnnotationButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		saveAnnotationButton.setText("Save annotated pictures");
		saveAnnotationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				savePictureAnnotations();
			}
		});

		clearAnnotationButton = new Button(aParent, SWT.NONE);
		clearAnnotationButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		clearAnnotationButton.setText("Discard annotations");
		clearAnnotationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				annotatedPictures.clear();
				annotatedPictureViewer.setInput(annotatedPictures);
				nonAnnotatedPictures.clear();
				nonAnnotatedPictureViewer.setInput(nonAnnotatedPictures);
				dirtyable.setDirty(false);
				updateButtonStates();
			}
		});
	}

	private void initializeUpperPane(final Composite aParent) {
		aParent.setLayout(new GridLayout(2, false));

		new Label(aParent, SWT.NONE).setText("Select tracks");
		new Label(aParent, SWT.NONE).setText("Select Pictures");

		final GridData viewerConstraint = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewerConstraint.minimumHeight = MINIMAL_GRID_HEIGHT;

		trackViewer = new TableViewer(aParent,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		trackViewer.getControl().setLayoutData(viewerConstraint);
		trackViewer.getTable().setHeaderVisible(true);
		final TrackViewerComparator trackViewerComparator = new TrackViewerComparator();
		trackViewer.setComparator(trackViewerComparator);
		for (final String columnHeader : TRACK_VIEWER_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(trackViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					trackViewerComparator.setColumn(columnHeader);
					trackViewer.getTable().setSortDirection(trackViewerComparator.getDirection());
					trackViewer.getTable().setSortColumn(column);
					trackViewer.refresh();
				}
			});
		}
		trackViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					trackViewer.setSelection(new StructuredSelection(tracksRepo.getTracks()));
				}
			}
		});
		bindTrackViewer();
		addTrackViewerSelectionListener();

		pictureViewer = new TableViewer(aParent,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		pictureViewer.getControl().setLayoutData(viewerConstraint);
		pictureViewer.getTable().setHeaderVisible(true);
		final PictureViewerComparator pictureViewerComparator = new PictureViewerComparator();
		pictureViewer.setComparator(pictureViewerComparator);
		for (final String columnHeader : PICTURE_VIEWER_COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(pictureViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(200);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					pictureViewerComparator.setColumn(columnHeader);
					pictureViewer.getTable().setSortDirection(pictureViewerComparator.getDirection());
					pictureViewer.getTable().setSortColumn(column);
					pictureViewer.refresh();
				}
			});
		}
		pictureViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					pictureViewer.setSelection(new StructuredSelection(picturesRepo.getPictures()));
				}
			}
		});
		bindPictureViewer();
		addPictureViewerSelectionListener();

		selectedTracksLabel = new Label(aParent, SWT.NONE);
		selectedTracksLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		selectedTracksLabel.setText(String.format(TRACK_SELECTION_MSG, 0, 0));

		selectedPicturesLabel = new Label(aParent, SWT.NONE);
		selectedPicturesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		selectedPicturesLabel.setText(String.format(PICTURE_SELECTION_MSG, 0, 0));

		final Composite settingsPane = new Composite(aParent, SWT.BORDER);
		final GridData settingsPaneLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		settingsPaneLayoutData.horizontalSpan = 2;
		settingsPane.setLayoutData(settingsPaneLayoutData);
		initializeSettingsPange(settingsPane);

		computeAnnotationButton = new Button(aParent, SWT.NONE);
		final GridData computButtonLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		computButtonLayoutData.horizontalSpan = 2;
		computeAnnotationButton.setLayoutData(computButtonLayoutData);
		computeAnnotationButton.setText("Compute matching positions");
		computeAnnotationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				annotatePictures();
			}
		});
	}

	private void initializeSettingsPange(final Composite aParent) {
		aParent.setLayout(new MigLayout());

		new Label(aParent, SWT.FILL).setText("Match settings:");
		overwriteButton = new Button(aParent, SWT.CHECK);
		overwriteButton.setText("Overwrite existing coordinates");
		overwriteButton.setLayoutData("gap 50 50 0 0");

		new Label(aParent, SWT.NONE).setText("Time tolerance (minutes):");
		toleranceLabel = new Label(aParent, SWT.NONE);
		toleranceLabel.setLayoutData("w 30px");
		toleranceLabel.setText(String.valueOf(getTolerance(51)));
		toleranceScale = new Scale(aParent, SWT.NONE);
		toleranceScale.setMinimum(10);
		toleranceScale.setMaximum(110);
		toleranceScale.setSelection(51);
		toleranceScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				final int tolerance = getTolerance(toleranceScale.getSelection());
				toleranceLabel.setText(String.valueOf(tolerance));
			}
		});
	}

	@Persist
	public void savePictureAnnotations() {
		logger.info("Writing " + annotatedPictures.size() + " position annotations");

		final List<Path> failedPictures = new LinkedList<>();

		final String timestamp = String.valueOf(System.currentTimeMillis());
		for (final Picture picture : annotatedPictures) {
			final PictureMetadataWriter metadataWriter = new PictureMetadataWriter(picture, timestamp);
			ContextInjectionFactory.inject(metadataWriter, eclipseContext);
			try {
				metadataWriter.writePositionMetadata();
			} catch (PictureAnnotationException e) {
				logger.severe("Annotating position metadata for " + picture.getFile() + " failed: " + e.getMessage());
				failedPictures.add(picture.getFile());
			}
		}

		logger.info("Finished writing position annotations");

		if (!failedPictures.isEmpty()) {
			MessageDialog.openError(new Shell(Display.getCurrent()),
					failedPictures.size() + " pictures failed in position annotation",
					failedPictures.size()
							+ " pictures could not be annotated with their position. Please check the log, if picture corruption might has happened.\n\nAffected pictures are:\n"
							+ StringUtils.join(failedPictures, "\n"));
		}

		final List<Path> reloadPictureFiles = new LinkedList<>();
		annotatedPictures.forEach(picture -> reloadPictureFiles.add(picture.getFile()));

		annotatedPictures.clear();
		annotatedPictureViewer.refresh();
		nonAnnotatedPictures.clear();
		nonAnnotatedPictureViewer.refresh();
		dirtyable.setDirty(false);
		updateButtonStates();

		OpenPicturesHandler.openPictures(reloadPictureFiles);
	}

	private int getTolerance(final int aScalePosition) {
		return (int) Math.pow(1.8, aScalePosition / 10.0);
	}

	private void bindTrackViewer() {
		final List<IBeanValueProperty> properties = new LinkedList<>();
		properties.add(BeanProperties.value(Track.class, Track.FILE_PROPERTY));
		properties.add(BeanProperties.value(Track.class, Track.POSITIONS_PROPERTY));
		properties.add(BeanProperties.value(Track.class, Track.COLOR_PROPERTY));
		final IBeanValueProperty[] propertiesArray = properties.toArray(new IBeanValueProperty[properties.size()]);

		final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		trackViewer.setContentProvider(contentProvider);
		final IObservableMap[] observables = Properties.observeEach(contentProvider.getKnownElements(),
				propertiesArray);
		trackViewer.setLabelProvider(new TrackViewerObservableLabelProvider(observables,
				Arrays.asList(TRACK_VIEWER_COLUMNS), colorPreviewGenerator));
		final IObservableList input = BeanProperties.list(TracksRepo.class, TracksRepo.TRACKS_PROPERTY)
				.observe(tracksRepo);

		trackViewer.setInput(input);
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
		pictureViewer.setLabelProvider(
				new PictureViewerObservableLabelProvider(observables, Arrays.asList(PICTURE_VIEWER_COLUMNS)));
		final IObservableList input = BeanProperties.list(PicturesRepo.class, PicturesRepo.PICTURES_PROPERTY)
				.observe(picturesRepo);

		pictureViewer.setInput(input);
	}

	private void addTrackViewerSelectionListener() {
		trackViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				logger.fine("Selected " + selection.size() + " tracks");

				selectedTracks = new TrackSelection(selection);

				int positionSum = 0;
				for (final Track track : selectedTracks.getSelection()) {
					positionSum += track.getPositions().size();
				}

				updateButtonStates();
				selectedTracksLabel.setText(String.format(TRACK_SELECTION_MSG, selectedTracks.getSelection().size(), positionSum));

				selectionService.setSelection(selectedTracks);
			}
		});
	}

	private void addPictureViewerSelectionListener() {
		pictureViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				logger.fine("Selected " + selection.size() + " pictures");

				selectedPictures = new PictureSelection(selection);

				int picturesWithPositions = 0;
				for (final Picture picture : selectedPictures.getSelection()) {
					if (picture.getCoordinates() != null) {
						picturesWithPositions++;
					}
				}

				updateButtonStates();
				selectedPicturesLabel
						.setText(String.format(PICTURE_SELECTION_MSG, selectedPictures.getSelection().size(), picturesWithPositions));

				selectionService.setSelection(selectedPictures);
			}
		});
	}

	private void annotatePictures() {
		annotationInProgress = true;
		updateButtonStates();

		logger.info("Starting annotation of " + selectedPictures.getSelection().size() + " pictures using " + selectedTracks.getSelection().size()
				+ " tracks...");

		final int tolerance = getTolerance(toleranceScale.getSelection());
		final boolean overwrite = overwriteButton.getSelection();

		final Job job = new Job("Annotating pictures") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask(
						"Improving " + selectedPictures.getSelection().size() + " pictures using " + selectedTracks.getSelection().size() + " tracks",
						-1);

				final PictureAnnotator annotator = new PictureAnnotator(selectedTracks.getSelection(), selectedPictures.getSelection(), tolerance,
						overwrite);
				annotator.computeMatches();

				logger.info("Annotating " + selectedPictures.getSelection().size() + " pictures completed");

				aMonitor.done();
				annotationInProgress = false;
				dirtyable.setDirty(annotator.getAnnotatedPictures().size() > 0);

				sync.syncExec(new Runnable() {
					@Override
					public void run() {
						annotatedPictures = annotator.getAnnotatedPictures();
						annotatedPictureViewer.setInput(annotatedPictures);
						annotatedPictureViewer.refresh();

						nonAnnotatedPictures = annotator.getNonAnnotatedPictures();
						nonAnnotatedPictureViewer.setInput(nonAnnotatedPictures);
						nonAnnotatedPictureViewer.refresh();

						annotatedPicturesLabel.setText(String.format(ANNOTATED_PICTURES_MSG, annotatedPictures.size()));
						nonAnnotatedPicturesLabel
								.setText(String.format(NON_ANNOTATED_PICTURES_MSG, nonAnnotatedPictures.size()));

						updateButtonStates();
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private void updateButtonStates() {
		computeAnnotationButton.setEnabled(!annotationInProgress && !dirtyable.isDirty() && selectedTracks.getSelection().size() > 0
				&& selectedPictures.getSelection().size() > 0);
		saveAnnotationButton.setEnabled(!annotationInProgress && dirtyable.isDirty());
		clearAnnotationButton.setEnabled(!annotationInProgress && dirtyable.isDirty());
		trackViewer.getControl().setEnabled(!annotationInProgress);
		pictureViewer.getControl().setEnabled(!annotationInProgress);
		overwriteButton.setEnabled(!annotationInProgress);
		toleranceScale.setEnabled(!annotationInProgress);
	}
}
