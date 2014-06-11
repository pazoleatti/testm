package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.gwt.client.EditTextAreaCell;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
	
	private List<ConfigTuple> configData;
	
	@UiField
    GenericCellTable<ConfigTuple> configTable;
	
	interface Binder extends UiBinder<Widget, ConfigurationView> {
	}
    ListDataProvider<ConfigTuple> dataProvider = new ListDataProvider<ConfigTuple>();

	@Inject
	public ConfigurationView(final Binder binder) {
		
		initWidget(binder.createAndBindUi(this));
		
		TextColumn<ConfigTuple> captionColumn = new TextColumn<ConfigTuple>() {
			@Override
			public String getValue(ConfigTuple object) {
				return object.getParam().getCaption();
			}
		};

        Column<ConfigTuple, String> valueColumn = new Column<ConfigTuple, String>(new EditTextAreaCell()) {
            @Override
            public String getValue(ConfigTuple object) {
				return object.getValue() == null ? "" : object.getValue();
            }
        };

		valueColumn.setFieldUpdater(new FieldUpdater<ConfigTuple, String>() {
            @Override
            public void update(int index, ConfigTuple object, String value) {
                if (value != null && value.trim().isEmpty()) {
                    value = null;
                }
                object.setValue(value);
            }
        });

        configTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
        configTable.addResizableColumn(captionColumn, "Наименование свойства");
		configTable.addResizableColumn(valueColumn, "Значение свойства");
        configTable.setColumnWidth(0, "400px");
		configTable.setSelectionModel(new NoSelectionModel<ConfigTuple>());
        dataProvider.addDataDisplay(configTable);
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
        dataProvider.setList(data);
        configTable.setVisibleRange(new Range(0, data.size()));
	}

	@Override
	public List<ConfigTuple> getConfigData() {
		return configData;
	}
}
