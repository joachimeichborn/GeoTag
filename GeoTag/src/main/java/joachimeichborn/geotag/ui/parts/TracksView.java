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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import joachimeichborn.geotag.misc.ColorPreviewImageGenerator;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.model.TracksRepo;
import joachimeichborn.geotag.model.selections.TrackSelection;
import joachimeichborn.geotag.ui.labelprovider.TrackViewerObservableLabelProvider;
import joachimeichborn.geotag.ui.tablecomparators.TrackViewerComparator;

public class TracksView {
	private static final Logger LOGGER = Logger.getLogger(TracksView.class.getSimpleName());

	private static final String TRACKS_PART_ID = "geotag.part.tracks";

	private static final String[] COLUMNS = new String[] { TrackViewerObservableLabelProvider.NAME_COLUMN,
			TrackViewerObservableLabelProvider.POSITION_COUNT_COLUMN, TrackViewerObservableLabelProvider.COLOR_COLUMN };
	private static final String SELECTED_TRACKS = "%d track(s) selected";

	private final ESelectionService selectionService;
	private final EPartService partService;
	private final TracksRepo tracksRepo;
	private final ImageRegistry registry;
	private TableViewer trackViewer;
	private Label nameLabel;
	private Label pathLabel;
	private Label positionCountLabel;
	private Label colorLabel;
	private Composite colorContainer;
	private ColorPreviewImageGenerator colorPreviewGenerator;

	private Label selectedTracksLabel;

	@Inject
	public TracksView(final ESelectionService aSelectionService, final EPartService aPartService, final TracksRepo aTracksRepo) {
		tracksRepo = aTracksRepo;
		selectionService = aSelectionService;
		partService = aPartService;
	
		final Display display = Display.getCurrent();
		registry = new ImageRegistry(display);
		colorPreviewGenerator = new ColorPreviewImageGenerator(registry, display);
	}

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		aParent.setLayout(new GridLayout(1, false));

		initializeTracksList(aParent);

		selectedTracksLabel = new Label(aParent, SWT.NONE);
		selectedTracksLabel.setText(String.format(SELECTED_TRACKS, 0));
		selectedTracksLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		initializeDetailsSection(aParent);
	}

	@PreDestroy
	public void dispose() {
		LOGGER.fine("Disposing track view registry");
		registry.dispose();
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
				switch (aEvent.character) {
					case 0x01: {
						trackViewer.setSelection(new StructuredSelection(tracksRepo.getTracks()));
						break;
					}
					case SWT.DEL: {
						final TrackSelection selectedTracks = new TrackSelection(trackViewer.getStructuredSelection());
						trackViewer.setSelection(StructuredSelection.EMPTY);
 						tracksRepo.removeTracks(selectedTracks.getSelection());
						break;
					}
				}
			}
		});

		bindTracksViewer();

		trackViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				LOGGER.fine("Selected " + selection.size() + " tracks");
				final TrackSelection tracks = new TrackSelection(selection);
				selectionService.setSelection(tracks);
			}
		});
	}

	private void bindTracksViewer() {
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

	private void initializeDetailsSection(final Composite aParent) {
		final Composite details = new Composite(aParent, SWT.BORDER);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		details.setLayout(new GridLayout(2, false));

		new Label(details, SWT.NONE).setText("Name: ");
		nameLabel = new Label(details, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Path: ");
		pathLabel = new Label(details, SWT.NONE);
		pathLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Position count: ");
		positionCountLabel = new Label(details, SWT.NONE);
		positionCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Color: ");
		colorContainer = new Composite(details, SWT.FILL);
		colorContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		colorContainer.setVisible(false);
		final GridLayout colorLayout = new GridLayout(2, false);
		colorLayout.marginWidth = 0;
		colorContainer.setLayout(colorLayout);
		colorLabel = new Label(colorContainer, SWT.NONE);
		colorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		colorLabel.setImage(colorPreviewGenerator.getColorPreview(new RGB(0, 0, 0)));
		final Button colorButton = new Button(colorContainer, SWT.PUSH);
		colorButton.setText("Change color");
		colorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				selectTrackColor();
			}
		});
	}

	private void selectTrackColor() {
		final TrackSelection selectedTracks = new TrackSelection(trackViewer.getStructuredSelection());

		if (selectedTracks.getSelection().size() == 1) {
			final Shell shell = new Shell(Display.getCurrent());
			final ColorDialog colorDialog = new ColorDialog(shell);
			colorDialog.setRGB(selectedTracks.getSelection().get(0).getColor());
			colorDialog.setText("Choose track color");

			final RGB rgb = colorDialog.open();
			if (rgb != null) {
				selectedTracks.getSelection().get(0).setColor(rgb);
				selectionService.setSelection(selectedTracks);
			}
		}
	}

	@Inject
	public void setSelection(
			@Named(IServiceConstants.ACTIVE_SELECTION) @Optional final TrackSelection aTrackSelection) {
		final MPart activePart = partService.getActivePart();
		if (activePart != null && activePart.getElementId().equals(TRACKS_PART_ID)) {
			if (nameLabel != null && aTrackSelection != null) {
				final List<Track> tracks = aTrackSelection.getSelection();
				selectedTracksLabel.setText(String.format(SELECTED_TRACKS, tracks.size()));
				if (tracks.size() == 1) {
					final Track track = tracks.get(0);
					nameLabel.setText(track.getFile().getFileName().toString());
					pathLabel.setText(track.getFile().getParent().toString());
					positionCountLabel.setText(String.valueOf(track.getPositions().size()));
					colorLabel.setImage(colorPreviewGenerator.getColorPreview(track.getColor()));
					colorContainer.setVisible(true);
				} else {
					nameLabel.setText("");
					pathLabel.setText("");
					positionCountLabel.setText("");
					colorLabel.setImage(null);
					colorContainer.setVisible(false);
				}
			}
		}
	}
}
