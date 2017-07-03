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

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import joachimeichborn.geotag.handlers.OpenTracksHandler;
import joachimeichborn.geotag.io.TrackFileFormat;
import joachimeichborn.geotag.io.writer.TrackWriter;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.model.TracksRepo;
import joachimeichborn.geotag.model.selections.TrackSelection;
import joachimeichborn.geotag.refinetracks.ImproveTrackOptions.ImproveTrackOptionsBuilder;
import joachimeichborn.geotag.refinetracks.TrackRefiner;
import joachimeichborn.geotag.ui.labelprovider.TrackViewerObservableLabelProvider;
import joachimeichborn.geotag.ui.tablecomparators.TrackViewerComparator;
import joachimeichborn.geotag.utils.ColorPreviewImageGenerator;
import net.miginfocom.swt.MigLayout;

public class TrackImprovementView {
	private static final String[] COLUMNS = new String[] { TrackViewerObservableLabelProvider.NAME_COLUMN,
			TrackViewerObservableLabelProvider.POSITION_COUNT_COLUMN, TrackViewerObservableLabelProvider.COLOR_COLUMN };
	private static final Logger logger = Logger.getLogger(TrackImprovementView.class.getSimpleName());
	private static final String INDENT_LEFT = "20";

	@Inject
	private ESelectionService selectionService;

	@Inject
	private MDirtyable dirtyable;

	private Button removeDuplicates;
	private Button filterByPairwiseDistance;
	private Button replaceByAccuracyComparison;
	private Button filterByAccuracyRadius;
	private Button removeIrrelevantPositions;
	private Button interpolatePositions;
	private Button refineButton;
	private TrackSelection selectedTracks;
	private final LinkedList<Track> inputTracks;
	protected Track refinedTrack;
	private Button showRefinedOnMap;
	private Button saveRefinedTrack;
	private Button discardRefinedTrack;
	private Label resultInfo;
	private Scale radiusThresholdScale;
	private Scale distanceFactorScale;
	private TableViewer trackViewer;
	private final ImageRegistry registry;
	private final ColorPreviewImageGenerator colorPreviewGenerator;
	private final TracksRepo tracksRepo;
	private boolean improvementInProgress;

	@Inject
	private static UISynchronize sync;

	public TrackImprovementView() {
		selectedTracks = new TrackSelection();
		inputTracks = new LinkedList<>();

		final Display display = Display.getCurrent();
		registry = new ImageRegistry(display);
		colorPreviewGenerator = new ColorPreviewImageGenerator(registry, display);

		tracksRepo = TracksRepo.getInstance();
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		final ScrolledComposite scrolledPane = new ScrolledComposite(aParent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledPane.setExpandVertical(true);
		scrolledPane.setExpandHorizontal(true);
		final Composite pane = new Composite(scrolledPane, SWT.NONE);
		scrolledPane.setContent(pane);
		pane.setLayout(new GridLayout(2, false));

		new Label(pane, SWT.NONE).setText("Select tracks for improvement");
		new Label(pane, SWT.NONE).setText("Improvement options");

		initializeTracksList(pane);

		final Composite optionsSection = new Composite(pane, SWT.NONE);
		optionsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		initializeOptionsSection(optionsSection);

		refineButton = new Button(pane, SWT.PUSH);
		refineButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		refineButton.setText("Perform Improvement");
		refineButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				startImprovement();
			};
		});

		resultInfo = new Label(pane, SWT.CHECK);
		resultInfo.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));

		showRefinedOnMap = new Button(pane, SWT.CHECK);
		showRefinedOnMap.setText("Show refined track on map");
		showRefinedOnMap.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				final List<Track> tracks = new LinkedList<>(selectedTracks.getSelection());
				if (showRefinedOnMap.getSelection()) {
					tracks.add(refinedTrack);
				}
				selectionService.setSelection(new TrackSelection(tracks));
			}
		});

		saveRefinedTrack = new Button(pane, SWT.PUSH);
		saveRefinedTrack.setText("Save refined track");
		saveRefinedTrack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		saveRefinedTrack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveRefinedTrack();
			}
		});

		discardRefinedTrack = new Button(pane, SWT.PUSH);
		discardRefinedTrack.setText("Discard refined track");
		discardRefinedTrack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		discardRefinedTrack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				discardRefinedTrack();
			}
		});

		scrolledPane.setMinSize(pane.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		updateButtonStates();
	}

	private void initializeTracksList(final Composite aParent) {
		trackViewer = new TableViewer(aParent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		trackViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trackViewer.getTable().setHeaderVisible(true);
		final TrackViewerComparator comparator = new TrackViewerComparator();
		trackViewer.setComparator(comparator);
		for (final String columnHeader : COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(trackViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					comparator.setColumn(columnHeader);
					trackViewer.getTable().setSortDirection(comparator.getDirection());
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
	}

	private void startImprovement() {
		final ImproveTrackOptionsBuilder builder = new ImproveTrackOptionsBuilder();
		builder.setRemoveDuplicates(removeDuplicates.getSelection());
		builder.setFilterByPairwiseDistance(filterByPairwiseDistance.getSelection());
		builder.setDistanceFactor(getDistanceFactor(distanceFactorScale.getSelection()));
		builder.setReplaceByAccuracyComparison(replaceByAccuracyComparison.getSelection());
		builder.setFilterByAccuracyRadius(filterByAccuracyRadius.getSelection());
		builder.setRadiusThreshold(getRadiusThreshold(radiusThresholdScale.getSelection()));
		builder.setRemoveIrrelevantPositions(removeIrrelevantPositions.getSelection());
		builder.setInterpolatePositions(interpolatePositions.getSelection());

		improvementInProgress = true;
		updateButtonStates();

		inputTracks.clear();
		inputTracks.addAll(selectedTracks.getSelection());

		logger.info("Starting improvement for " + inputTracks.size() + " tracks...");

		final Job job = new Job("Improving tracks") {
			@Override
			protected IStatus run(final IProgressMonitor aMonitor) {
				aMonitor.beginTask("Improving " + inputTracks.size() + " tracks", -1);

				final TrackRefiner refiner = new TrackRefiner(builder.build(), inputTracks);
				refinedTrack = refiner.refine();

				logger.info("Improving " + inputTracks.size() + " tracks completed");

				aMonitor.done();
				improvementInProgress = false;
				dirtyable.setDirty(true);

				sync.syncExec(new Runnable() {
					@Override
					public void run() {
						int positionSum = 0;
						for (final Track track : inputTracks) {
							positionSum += track.getPositions().size();
						}

						resultInfo.setText("Refined " + inputTracks.size() + " track(s) with " + positionSum
								+ " positions to one track with " + refinedTrack.getPositions().size() + " positions.");

						updateButtonStates();
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public void discardRefinedTrack() {
		refinedTrack = null;
		resultInfo.setText("");
		dirtyable.setDirty(false);
		updateButtonStates();
	}

	@Persist
	public void saveRefinedTrack() {
		final FileDialog saveDialog = new FileDialog(new Shell(Display.getCurrent()), SWT.SAVE);
		saveDialog.setFileName("Refined track");
		saveDialog.setFilterExtensions(new String[] { "*.kmz", "*.kml" });
		saveDialog.setFilterNames(new String[] { "KMZ file", "KML file" });
		saveDialog.setText("Save refined track");
		saveDialog.setFilterPath(System.getProperty("user.home"));
		if (saveDialog.open() != null) {
			final File file = new File(saveDialog.getFilterPath(), saveDialog.getFileName());
			final String extension = FilenameUtils.getExtension(saveDialog.getFileName());
			final TrackFileFormat format = TrackFileFormat.getByExtension(extension);
			final TrackWriter writer = format.getWriter();

			try {
				writer.write(refinedTrack, file.toPath());
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to write refined track: " + e.getMessage(), e);
				return;
			}

			logger.info("Saved refined track to " + file.getPath());
			refinedTrack = null;
			inputTracks.clear();
			dirtyable.setDirty(false);
			updateButtonStates();

			OpenTracksHandler.openTracks(file.getParent(), new String[] { file.getName() });
		}
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
		trackViewer.setLabelProvider(
				new TrackViewerObservableLabelProvider(observables, Arrays.asList(COLUMNS), colorPreviewGenerator));
		final IObservableList input = BeanProperties.list(TracksRepo.class, TracksRepo.TRACKS_PROPERTY)
				.observe(tracksRepo);

		trackViewer.setInput(input);
	}

	private void addTrackViewerSelectionListener() {
		trackViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				logger.fine("Selected " + selection.size() + " tracks");

				selectedTracks = new TrackSelection(selection);

				updateButtonStates();

				final List<Track> extendedSelection = new LinkedList<>(selectedTracks.getSelection());
				if (refinedTrack != null && showRefinedOnMap.getSelection()) {
					extendedSelection.add(refinedTrack);
				}

				selectionService.setSelection(new TrackSelection(extendedSelection));
			}
		});
	}

	private void initializeOptionsSection(final Composite aParent) {
		aParent.setLayout(new MigLayout("wrap 1"));

		removeDuplicates = new Button(aParent, SWT.CHECK);
		removeDuplicates.setText("Remove Duplicates");
		removeDuplicates.setSelection(true);

		filterByPairwiseDistance = new Button(aParent, SWT.CHECK);
		filterByPairwiseDistance.setText("Filter by Pairwise Distances");
		filterByPairwiseDistance.setSelection(true);

		final Composite distanceFactorSection = new Composite(aParent, SWT.NONE);
		distanceFactorSection.setLayout(new MigLayout("insets 0 " + INDENT_LEFT + " 0 0, wrap 2"));
		distanceFactorScale = new Scale(distanceFactorSection, SWT.NONE);
		distanceFactorScale.setMinimum(10);
		distanceFactorScale.setMaximum(100);
		distanceFactorScale.setIncrement(1);
		distanceFactorScale.setPageIncrement(10);
		distanceFactorScale.setSelection(15);
		distanceFactorScale.setLayoutData("span 2");
		new Label(distanceFactorSection, SWT.NONE).setText("Distance factor");
		final Label distanceFactor = new Label(distanceFactorSection, SWT.RIGHT);
		distanceFactor.setLayoutData("growx");
		distanceFactor.setText(String.format("%.1f", getDistanceFactor(distanceFactorScale.getSelection())));
		filterByPairwiseDistance.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent aEvent) {
				distanceFactorScale.setEnabled(filterByPairwiseDistance.getSelection());
			}
		});
		distanceFactorScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				final int rawValue = distanceFactorScale.getSelection();
				distanceFactor.setText(String.format("%.1f", getDistanceFactor(rawValue)));
			}
		});

		replaceByAccuracyComparison = new Button(aParent, SWT.CHECK);
		replaceByAccuracyComparison.setText("Replace by Accuracy Comparison");
		replaceByAccuracyComparison.setSelection(true);

		filterByAccuracyRadius = new Button(aParent, SWT.CHECK);
		filterByAccuracyRadius.setText("Filter by Accuracy Radius");

		final Composite radiusThresholdSection = new Composite(aParent, SWT.NONE);
		radiusThresholdSection.setLayout(new MigLayout("insets 0 " + INDENT_LEFT + " 0 0, wrap 2"));
		radiusThresholdScale = new Scale(radiusThresholdSection, SWT.NONE);
		radiusThresholdScale.setMinimum(20);
		radiusThresholdScale.setMaximum(200);
		radiusThresholdScale.setIncrement(2);
		radiusThresholdScale.setPageIncrement(20);
		radiusThresholdScale.setLayoutData("span 2");
		radiusThresholdScale.setSelection(70);
		radiusThresholdScale.setEnabled(false);
		new Label(radiusThresholdSection, SWT.NONE).setText("Radius threshold");
		final Label radiusThreshold = new Label(radiusThresholdSection, SWT.RIGHT);
		radiusThreshold.setText(String.valueOf(getRadiusThreshold(radiusThresholdScale.getSelection())));
		radiusThreshold.setLayoutData("growx");
		filterByAccuracyRadius.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				radiusThresholdScale.setEnabled(filterByAccuracyRadius.getSelection());
			}
		});
		radiusThresholdScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent aEvent) {
				final int rawValue = radiusThresholdScale.getSelection();
				radiusThreshold.setText(String.valueOf(getRadiusThreshold(rawValue)));
			}
		});

		removeIrrelevantPositions = new Button(aParent, SWT.CHECK);
		removeIrrelevantPositions.setText("Remove Irrelevant Positions");
		removeIrrelevantPositions.setSelection(true);

		interpolatePositions = new Button(aParent, SWT.CHECK);
		interpolatePositions.setText("Interpolate Positions");
	}

	private void updateButtonStates() {
		refineButton.setEnabled(!improvementInProgress && !dirtyable.isDirty() && selectedTracks.getSelection().size() > 0);
		showRefinedOnMap.setEnabled(!improvementInProgress && dirtyable.isDirty());
		saveRefinedTrack.setEnabled(!improvementInProgress && dirtyable.isDirty());
		discardRefinedTrack.setEnabled(!improvementInProgress && dirtyable.isDirty());
		removeDuplicates.setEnabled(!improvementInProgress);
		filterByPairwiseDistance.setEnabled(!improvementInProgress);
		distanceFactorScale.setEnabled(!improvementInProgress);
		replaceByAccuracyComparison.setEnabled(!improvementInProgress);
		filterByAccuracyRadius.setEnabled(!improvementInProgress);
		radiusThresholdScale.setEnabled(!improvementInProgress);
		removeIrrelevantPositions.setEnabled(!improvementInProgress);
		interpolatePositions.setEnabled(!improvementInProgress);
		trackViewer.getControl().setEnabled(!improvementInProgress);
	}

	private double getDistanceFactor(final int aScalePosition) {
		return aScalePosition / 10.0;
	}

	private int getRadiusThreshold(final int aScalePosition) {
		return aScalePosition * aScalePosition / 10;
	}
}