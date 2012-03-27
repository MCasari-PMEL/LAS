package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
/**
 * This widget will create a map and a set of axes widgets to control all of the XYZT and E(nsemble) dimensions for a given view.
 * @author rhs
 *
 */
public class AxesWidgetGroup extends Composite {
	
	OLMapWidget refMap;
	DateTimeWidget dateTimeWidget;
	AxisWidget zWidget;
	HorizontalPanel layout;
	FlexTable menuWidgets;
	DisclosurePanel panel;
	
	HTML plotAxisMessage;
	String title;
	boolean hasZ;
	boolean hasT;
	boolean hasE;
	boolean panelIsOpen = true;
	List<String> viewAxes = new ArrayList<String>();   // This is just the view, but individual axes
	List<ChangeHandler> zChangeHandlers = new ArrayList<ChangeHandler>();
	List<ChangeHandler> tChangeHandlers = new ArrayList<ChangeHandler>();
	/**
	 * A widget to hold a set of x, y, z, t, and e(nsemble) axis controls and to display them in groups according to the view.  Initially the map
	 * is at the top and z and t are below, but this can be switched.
	 * @param plot_title
	 * @param ortho_title
	 * @param layout
	 */
	public AxesWidgetGroup(String title, String orientation, String width, String panel_title, String tile_server) {		
		layout = new HorizontalPanel();
		menuWidgets = new FlexTable();
		refMap = new OLMapWidget("128px", "256px", tile_server);
		refMap.activateNativeHooks();
		zWidget = new AxisWidget();
		zWidget.setVisible(false);
		dateTimeWidget = new DateTimeWidget();
		dateTimeWidget.setVisible(false);
		layout.add(refMap);
		panel = new DisclosurePanel(title);
		panel.add(layout);
		panel.setOpen(true);
		menuWidgets.setWidget(0, 0, zWidget);
		menuWidgets.setWidget(1, 0, dateTimeWidget);
		menuWidgets.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
		menuWidgets.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        layout.add(menuWidgets);
		initWidget(panel);
	}

	
	public void init(GridSerializable grid) {
		hasZ = grid.hasZ();
		hasT = grid.hasT();
		if ( grid.hasZ() ) {
			zWidget.init(grid.getZAxis());
//			zWidget = new AxisWidget(grid.getZAxis());
		} else {
//			zWidget = new AxisWidget();
			zWidget.setVisible(false);
		}
		if ( grid.hasT() ) {
			dateTimeWidget.init(grid.getTAxis(), false);
//			dateTimeWidget = new DateTimeWidget(grid.getTAxis(), false);
		} else {
//			dateTimeWidget = new DateTimeWidget();
			dateTimeWidget.setVisible(false);
		}
		if ( grid.hasX() && grid.hasY() ) {
			refMap.setDataExtent(Double.valueOf(grid.getYAxis().getLo()), 
					             Double.valueOf(grid.getYAxis().getHi()), 
					             Double.valueOf(grid.getXAxis().getLo()), 
					             Double.valueOf(grid.getXAxis().getHi()));
		}
		addTChangeHandlersToWidget();
		addZChangeHandlersToWidget();
	}
	private void setAxisVisible(String type, boolean visible) {
		if ( type.contains("x") ) {
			refMap.setVisible(visible);
		}
		if ( type.contains("y") ) {
			refMap.setVisible(visible);
		}
		if ( type.equals("z") ) {
			zWidget.setVisible(visible);
		}
		if ( type.equals("t") ) {
			dateTimeWidget.setVisible(visible);
		}
	}
	
	public void setRange(String type, boolean range) {
		// Does not apply to x and y
		if ( type.equals("z") ) {
			zWidget.setRange(range);
		}
		if ( type.equals("t") ) {
			dateTimeWidget.setRange(range);
		}
	}
	public OLMapWidget getRefMap() {
		return refMap;
	}
    public void addZChangeHandler(ChangeHandler zchange) {
    	zChangeHandlers.add(zchange);
    }
    public void addTChangeHandler(ChangeHandler tchange) {
    	tChangeHandlers.add(tchange);
    }
    private void addTChangeHandlersToWidget() {
    	for (Iterator changeIt = tChangeHandlers.iterator(); changeIt.hasNext();) {
			ChangeHandler handler = (ChangeHandler) changeIt.next();
			dateTimeWidget.addChangeHandler(handler);
		}
    }
    private void addZChangeHandlersToWidget() {
    	for (Iterator changeIt = zChangeHandlers.iterator(); changeIt.hasNext();) {
			ChangeHandler handler = (ChangeHandler) changeIt.next();
			zWidget.addChangeHandler(handler);
		}
    }
    public DateTimeWidget getTAxis() {
    	return dateTimeWidget;
    }
    public AxisWidget getZAxis() {
    	return zWidget;
    }
    
    public void showOrthoAxes(String view, List<String> ortho) {
    	for (int i = 0; i < view.length(); i++) {
			setAxisVisible(view.substring(i, i+1), false);
		}
    	for (Iterator orthoIt = ortho.iterator(); orthoIt.hasNext();) {
    		String ax = (String) orthoIt.next();
    		setAxisVisible(ax, true);
    	}
    }
    public List<String> getViewAxes() {
    	return viewAxes;
    }
	public void setOpen(boolean b) {
		panel.setOpen(b);
	}

	public void closePanels() {
		panelIsOpen = panel.isOpen();
		panel.setOpen(false);
	}
	public void restorePanels() {
		panel.setOpen(panelIsOpen);
	}
}
