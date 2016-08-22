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
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputListener;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;

import com.google.common.collect.Sets;

import joachimeichborn.geotag.LifeCycleManager;
import joachimeichborn.geotag.model.selections.PictureSelection;
import joachimeichborn.geotag.model.selections.PositionSelection;
import joachimeichborn.geotag.model.selections.TrackSelection;
import joachimeichborn.geotag.ui.map.painterhandler.PicturePainterHandler;
import joachimeichborn.geotag.ui.map.painterhandler.PositionPainterHandler;
import joachimeichborn.geotag.ui.map.painterhandler.TrackPainterHandler;
import joachimeichborn.geotag.ui.preferences.MapPreferences;
import net.miginfocom.swt.MigLayout;

public class MapView {
	public static enum ZoomMode {
		LATEST_SELECTION("Latest selection"), //
		ALL_SELECTIONS("All selections");

		private String displayName;

		private ZoomMode(final String aDisplayName) {
			displayName = aDisplayName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public static ZoomMode getByDisplayName(final String aDisplayName) {
			for (final ZoomMode mode : values()) {
				if (mode.getDisplayName().equals(aDisplayName)) {
					return mode;
				}
			}

			throw new IllegalArgumentException("Could not find a zoom mode for display name '" + aDisplayName + "'");
		}
	}

	private class TrackRepaintSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent aEvent) {
			trackPlacemarks.paintSelectedItems();
		}
	}

	private class PositionRepaintSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent aEvent) {
			positionPlacemarks.paintSelectedItems();
		}
	}

	private class PictureRepaintSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent aEvent) {
			picturePlacemarks.paintSelectedItems();
			;
		}
	}

	private static final Logger logger = Logger.getLogger(MapView.class.getSimpleName());
	private static final String PART_ID = "geotag.part.map";

	@Inject
	private EPartService partService;

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	@Preference(nodePath = LifeCycleManager.PREFERENCES_NODE, value = MapPreferences.ZOOM_MODE)
	private String zoomModeName;

	private Button showTrackPlacemarks;
	private Button showTrackRoute;
	private Button showTrackAccuracy;
	private Button showPicturePlacemarks;
	private Button showPictureThumbnails;
	private JXMapViewer mapViewer;
	private Button showPositions;
	private boolean visible;
	private MPart mapPart;
	private TrackPainterHandler trackPlacemarks;
	private PicturePainterHandler picturePlacemarks;
	private PositionPainterHandler positionPlacemarks;
	private Set<GeoPosition> latestGeoPositions;

	@Inject
	public MapView() {
		trackPlacemarks = new TrackPainterHandler(this);
		picturePlacemarks = new PicturePainterHandler(this);
		ContextInjectionFactory.inject(picturePlacemarks, eclipseContext);
		positionPlacemarks = new PositionPainterHandler(this);
		latestGeoPositions = new HashSet<>();
	}

	@PostConstruct
	public void postConstruct(final Composite aParent) {
		final Composite pane = new Composite(aParent, SWT.NONE);
		pane.setLayout(new GridLayout(1, false));

		initializeMap(pane);
		initializeDisplayOptions(pane);

		mapPart = partService.findPart(PART_ID);

		visible = mapPart.isVisible();

		partService.addPartListener(new IPartListener() {
			@Override
			public void partVisible(final MPart aPart) {
				if (!visible && aPart.equals(mapPart)) {
					visible = true;
					trackPlacemarks.paintSelectedItems();
					picturePlacemarks.paintSelectedItems();
					positionPlacemarks.paintSelectedItems();
				}
			}

			@Override
			public void partHidden(final MPart aPart) {
				if (aPart.equals(mapPart)) {
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
			}
		});
	}

	private void initializeMap(final Composite aParent) {
		final Composite mapPanel = new Composite(aParent, SWT.EMBEDDED);
		mapPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TileFactoryInfo info = new OSMTileFactoryInfo();
		final DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);

		final Path cacheDir = LifeCycleManager.WORKING_DIR.resolve("tiles");
		LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir.toFile(), false);

		mapViewer = new JXMapViewer();
		mapViewer.setTileFactory(tileFactory);
		mapViewer.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		final GeoPosition bonn = new GeoPosition(50, 44, 14, 7, 5, 53);

		mapViewer.setZoom(10);
		mapViewer.setAddressLocation(bonn);

		final MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);
		mapViewer.addMouseListener(new CenterMapListener(mapViewer));
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
		mapViewer.addKeyListener(new PanKeyListener(mapViewer));

		final Frame frame = SWT_AWT.new_Frame(mapPanel);
		frame.add(mapViewer);
	}

	private void initializeDisplayOptions(final Composite aParent) {
		final ScrolledComposite scrolledPane = new ScrolledComposite(aParent, SWT.H_SCROLL);
		scrolledPane.setExpandHorizontal(true);
		scrolledPane.setExpandVertical(true);
		scrolledPane.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		final Composite optionsPane = new Composite(scrolledPane, SWT.NONE);
		optionsPane.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		optionsPane.setLayout(new MigLayout());
		scrolledPane.setContent(optionsPane);

		showTrackPlacemarks = new Button(optionsPane, SWT.CHECK);
		showTrackPlacemarks.setText("Show track placemarks");
		showTrackPlacemarks.setSelection(true);
		showTrackPlacemarks.addSelectionListener(new TrackRepaintSelectionListener());

		showTrackRoute = new Button(optionsPane, SWT.CHECK);
		showTrackRoute.setText("Show track route");
		showTrackRoute.setSelection(true);
		showTrackRoute.addSelectionListener(new TrackRepaintSelectionListener());

		showTrackAccuracy = new Button(optionsPane, SWT.CHECK);
		showTrackAccuracy.setText("Show track accuracies");
		showTrackAccuracy.setSelection(true);
		showTrackAccuracy.addSelectionListener(new TrackRepaintSelectionListener());

		showPositions = new Button(optionsPane, SWT.CHECK);
		showPositions.setText("Show positions");
		showPositions.setSelection(true);
		showPositions.addSelectionListener(new PositionRepaintSelectionListener());

		showPicturePlacemarks = new Button(optionsPane, SWT.CHECK);
		showPicturePlacemarks.setText("Show picture placemarks");
		showPicturePlacemarks.setSelection(true);
		showPicturePlacemarks.addSelectionListener(new PictureRepaintSelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent aEvent) {
				showPictureThumbnails.setEnabled(showPicturePlacemarks.getSelection());
				super.widgetSelected(aEvent);
			}
		});

		showPictureThumbnails = new Button(optionsPane, SWT.CHECK);
		showPictureThumbnails.setText("Use picture thumbnails");
		showPictureThumbnails.setSelection(true);
		showPictureThumbnails.addSelectionListener(new PictureRepaintSelectionListener());

		scrolledPane.setMinSize(optionsPane.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional TrackSelection aTrackSelection) {
		if (aTrackSelection != null) {
			trackPlacemarks.setSelectedItems(aTrackSelection.getSelection());

			if (visible) {
				trackPlacemarks.paintSelectedItems();
			}
		}
	}

	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional PictureSelection aPictureSelection) {
		if (aPictureSelection != null) {
			picturePlacemarks.setSelectedItems(aPictureSelection.getSelection());

			if (visible) {
				picturePlacemarks.paintSelectedItems();
			}
		}
	}

	@Inject
	public void setSelection(
			@Named(IServiceConstants.ACTIVE_SELECTION) @Optional PositionSelection aPositionSelection) {
		if (aPositionSelection != null) {
			positionPlacemarks.setSelectedItems(aPositionSelection.getSelection());

			if (visible) {
				positionPlacemarks.paintSelectedItems();
			}
		}
	}

	public void repaint() {
		final List<Painter<JXMapViewer>> painters = new LinkedList<>();
		painters.addAll(trackPlacemarks.getPainters());
		painters.addAll(picturePlacemarks.getPainters());
		painters.addAll(positionPlacemarks.getPainters());

		mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));

		final Set<GeoPosition> geoPositions;
		if (ZoomMode.ALL_SELECTIONS.getDisplayName().equals(zoomModeName)) {
			geoPositions = Sets.union(
					Sets.union(trackPlacemarks.getGeoPositions(), picturePlacemarks.getGeoPositions()),
					positionPlacemarks.getGeoPositions());
		} else {
			geoPositions = latestGeoPositions;
		}

		if (geoPositions.size() == 1) {
			mapViewer.setCenterPosition(geoPositions.iterator().next());
		} else {
			mapViewer.zoomToBestFit(new HashSet<GeoPosition>(geoPositions), 0.7);
		}
		mapViewer.repaint();
		logger.fine("Finished drawing " + latestGeoPositions.size() + " positions");
	}

	public void setLatestGeoPositions(final Set<GeoPosition> aGeoPositions) {
		latestGeoPositions.clear();
		latestGeoPositions.addAll(aGeoPositions);
	}

	public boolean isShowTrackPlacemarks() {
		return showTrackPlacemarks.getSelection();
	}

	public boolean isShowTrackRoute() {
		return showTrackRoute.getSelection();
	}

	public boolean isShowTrackAccuracy() {
		return showTrackAccuracy.getSelection();
	}

	public boolean isShowPicturePlacemarks() {
		return showPicturePlacemarks.getSelection();
	}

	public boolean isShowPictureThumbnails() {
		return showPictureThumbnails.getSelection();
	}

	public boolean isShowPositions() {
		return showPositions.getSelection();
	}
}
