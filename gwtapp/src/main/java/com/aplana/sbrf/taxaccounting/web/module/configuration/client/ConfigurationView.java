package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
	
	private List<ConfigTuple> configData;
	
	@UiField
	DataGrid<ConfigTuple> configTable;
	
	interface Binder extends UiBinder<Widget, ConfigurationView> {
	}

	@Inject
	public ConfigurationView(final Binder binder) {
		
		initWidget(binder.createAndBindUi(this));
		
		TextColumn<ConfigTuple> captionColumn = new TextColumn<ConfigTuple>() {
			@Override
			public String getValue(ConfigTuple object) {
				return object.getParam().getCaption();
			}
		};
		
	    Column<ConfigTuple, String> valueColumn = new Column<ConfigTuple, String>(new EditTextCell()) {
			@Override
			public String getValue(ConfigTuple object) {
				return object.getValue() == null ? "" : object.getValue();
			}
		};
	
		valueColumn.setFieldUpdater(new FieldUpdater<ConfigTuple, String>() {
			@Override
			public void update(int index, ConfigTuple object,
					String value) {
				object.setValue(value);
			}
		});
		
		configTable.addColumn(captionColumn, "Наименование свойства");
		configTable.addColumn(valueColumn, "Значение свойства");
		configTable.setSelectionModel(new NoSelectionModel<Object>());

	}
	
	@UiHandler("saveButton")
	void onSaveButtonClick(ClickEvent event) {
		getUiHandlers().save();
	}
	
	
	@UiHandler("reloadButton")
	void onReloadButtonClick(ClickEvent event) {
		getUiHandlers().reload();
	}

	@Override
	public void setConfigData(List<ConfigTuple> data) {
		this.configData = data;
		configTable.setRowData(data);
	}

	@Override
	public List<ConfigTuple> getConfigData() {
		return configData;
	}

}
