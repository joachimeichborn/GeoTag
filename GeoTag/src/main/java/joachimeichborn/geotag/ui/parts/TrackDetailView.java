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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import joachimeichborn.geotag.model.PositionData;
import joachimeichborn.geotag.model.Track;
import joachimeichborn.geotag.model.selections.PositionSelection;
import joachimeichborn.geotag.model.selections.TrackSelection;
import joachimeichborn.geotag.ui.labelprovider.PositionsViewerLabelProvider;
import joachimeichborn.geotag.ui.tablecomparators.PositionViewerComparator;

public class TrackDetailView {
	private static final String[] COLUMNS = new String[] { PositionsViewerLabelProvider.NAME_COLUMN,
			PositionsViewerLabelProvider.ACCURACY_COLUMN, PositionsViewerLabelProvider.TIMESTAMP_COLUMN,
			PositionsViewerLabelProvider.COORDINATES_COLUMN };
	private static final String SELECTED_POSIITONS = "%d position(s) selected";
	private static final Logger logger = Logger.getLogger(TrackDetailView.class.getSimpleName());

	@Inject
	private ESelectionService selectionService;

	private TableViewer positionsViewer;
	private Label nameLabel;
	private Label positionCountLabel;
	private Track selectedTrack;

	private Label selectedPositionsLabel;

	@PostConstruct
	public void createPartControl(final Composite aParent) {
		aParent.setLayout(new GridLayout(1, false));

		initializeDetailsSection(aParent);
		
		initializePositionsList(aParent);

		selectedPositionsLabel = new Label(aParent, SWT.NONE);
		selectedPositionsLabel.setText(String.format(SELECTED_POSIITONS, 0));
		selectedPositionsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	private void initializePositionsList(final Composite aParent) {
		positionsViewer = new TableViewer(aParent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		positionsViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		positionsViewer.getTable().setHeaderVisible(true);
		final PositionViewerComparator comparator = new PositionViewerComparator();
		positionsViewer.setComparator(comparator);
		for (final String columnHeader : COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(positionsViewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setWidth(100);
			column.setText(columnHeader);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent aEvent) {
					comparator.setColumn(columnHeader);
					positionsViewer.getTable().setSortDirection(comparator.getDirection());
					positionsViewer.getTable().setSortColumn(column);
					positionsViewer.refresh();
				};
			});
		}
		positionsViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent aEvent) {
				if (aEvent.character == 0x01) {
					positionsViewer.setSelection(new StructuredSelection(selectedTrack.getPositions()));
				}
			}
		});
		positionsViewer.setLabelProvider(new PositionsViewerLabelProvider(Arrays.asList(COLUMNS)));
		positionsViewer.setContentProvider(new ArrayContentProvider());
		positionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				logger.fine("Selected " + selection.size() + " positions");
				final PositionSelection positions = new PositionSelection(selection);
				selectionService.setSelection(positions);
			}
		});
	}

	private void initializeDetailsSection(final Composite aParent) {
		final Composite details = new Composite(aParent, SWT.NONE);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		details.setLayout(new GridLayout(2, false));

		new Label(details, SWT.NONE).setText("Track name: ");
		nameLabel = new Label(details, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(details, SWT.NONE).setText("Position count: ");
		positionCountLabel = new Label(details, SWT.NONE);
		positionCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Inject
	public void setSelection(
			@Named(IServiceConstants.ACTIVE_SELECTION) @Optional final TrackSelection aTrackSelection) {
		if (nameLabel != null && aTrackSelection != null) {
			final List<Track> tracks = aTrackSelection.getSelection();
			if (tracks.size() == 1) {
				selectedTrack = tracks.get(0);
				nameLabel.setText(selectedTrack.getFile().getFileName().toString());
				positionCountLabel.setText(String.valueOf(selectedTrack.getPositions().size()));
				positionsViewer.setInput(selectedTrack.getPositions());
				positionsViewer.refresh();
			} else {
				nameLabel.setText("");
				positionCountLabel.setText("");
				positionsViewer.setInput(null);
				positionsViewer.refresh();
			}
			
			final PositionSelection positions = new PositionSelection();
			selectionService.setSelection(positions);
		}
	}
	
	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional final PositionSelection aPositionSelection) {
		if (selectedPositionsLabel != null && aPositionSelection != null) {
			final List<PositionData> position = aPositionSelection.getSelection();
			selectedPositionsLabel.setText(String.format(SELECTED_POSIITONS, position.size()));
		}
	}
}
