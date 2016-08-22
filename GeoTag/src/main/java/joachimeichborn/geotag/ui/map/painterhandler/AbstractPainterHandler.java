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

package joachimeichborn.geotag.ui.map.painterhandler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import joachimeichborn.geotag.ui.parts.MapView;

public abstract class AbstractPainterHandler<E> implements PainterHandler<E> {
	final MapView mapView;
	final List<Painter<JXMapViewer>> painters ;
	final Set<GeoPosition> geoPositions ;
	final List<E> selectedItems = new LinkedList<>();
	
	public AbstractPainterHandler(final MapView aMapView) {
		mapView = aMapView;
		painters= new LinkedList<>();
		geoPositions= new HashSet<>();
	}
	
	@Override
	public List<Painter<JXMapViewer>> getPainters() {
		return painters;
	}

	@Override
	public Set<GeoPosition> getGeoPositions() {
		return geoPositions;
	}
	
	@Override
	public void setSelectedItems(List<E> aSelectedItems) {
		selectedItems.clear();
		selectedItems.addAll(aSelectedItems);
	}
	
	@Override
	public void paintSelectedItems(){
		computeContents();
		mapView.repaint();
	}
	
	abstract void computeContents();
}
