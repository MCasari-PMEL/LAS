package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.rpc.RPCServiceAsync;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * A button that will open a data picker that has automatically initialized itself via RPC with the data sets
 * for the LAS.
 * @author rhs
 *
 */
public class DatasetButton extends Composite {
	PushButton choose;
	PopupPanel datasetPanel;
	Label dataset_name = new Label("");
	Label variable_name = new Label("");
	DecoratorPanel selection = new DecoratorPanel();
	VerticalPanel verticalPanel = new VerticalPanel();
	DatasetWidget datasetWidget;
	VariableSerializable selectedVariable;
	Grid popupGrid;
	Button close;
	int offset = 0;
	public DatasetButton () {
		choose = new PushButton("Data Set");
		choose.setWidth("55px");
		choose.addClickHandler(openClick);
		datasetPanel = new PopupPanel(false);
		datasetWidget = new DatasetWidget();
		datasetWidget.addTreeListener(new TreeListener() {
			public void onTreeItemSelected(TreeItem item) {
				Object u = item.getUserObject();
				if ( u instanceof VariableSerializable ) {
					selectedVariable = (VariableSerializable) u;
					dataset_name.setText(selectedVariable.getDSName());
					variable_name.setText(selectedVariable.getName());
				}		
			}
			public void onTreeItemStateChanged(TreeItem item) {
			}
			
		});
		datasetWidget.init();
		popupGrid = new Grid(3, 1);		
		close = new Button("close");
		close.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				datasetPanel.hide();
			}
		});
		verticalPanel.add(dataset_name);
		verticalPanel.add(variable_name);
		selection.add(verticalPanel);
		popupGrid.setWidget(0, 0, close);
		popupGrid.setWidget(1, 0, selection);
		popupGrid.setWidget(2, 0, datasetWidget);
		datasetPanel.add(popupGrid);
		datasetPanel.hide();
		initWidget(choose);
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public ClickHandler openClick = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			datasetPanel.setPopupPosition(choose.getAbsoluteLeft()+offset, choose.getAbsoluteTop());
			datasetPanel.show();
		}
	};
	public VariableSerializable getSelectedVariable() {
		return selectedVariable;
	}
	public HandlerRegistration addSelectionHandler(SelectionHandler<TreeItem> handler) {
		return datasetWidget.addSelectionHandler(handler);
	}
	public void addOpenClickHandler(ClickHandler handler) {
		choose.addClickHandler(handler);
	}
	public void addCloseClickHandler(ClickHandler handler) {
		close.addClickHandler(handler);
	}
	public HandlerRegistration addOpenHandler(OpenHandler<TreeItem> handler) {
		return datasetWidget.addOpenHandler(handler);
	}
	public void setOpenID(String openid) {
		datasetWidget.setOpenID(openid);
	}
	public void setAuthenticated(String auth_url, String value) {
		for (int i = 0; i < datasetWidget.getItemCount(); i++ ) {
			TreeItem item = datasetWidget.getItem(i);
			Object u = item.getUserObject();
			if ( u instanceof CategorySerializable ) {
				CategorySerializable cat = (CategorySerializable) u;
				String url = cat.getAttributes().get("remote_las");
				if ( url != null && url.equals(auth_url) ) {
					cat.setAttribute("authenticated", value);
				}
			}
		}
	}
}
