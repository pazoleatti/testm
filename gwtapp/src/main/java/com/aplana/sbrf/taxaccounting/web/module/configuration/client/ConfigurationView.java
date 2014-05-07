package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import java.util.Comparator;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers> implements MyView {
	
	private List<ConfigTuple> configData;
	
	@UiField
    GenericDataGrid<ConfigTuple> configTable;
	
	interface Binder extends UiBinder<Widget, ConfigurationView> {
	}
    ListDataProvider<ConfigTuple> dataProvider = new ListDataProvider<ConfigTuple>();
    ColumnSortEvent.ListHandler<ConfigTuple> configTupleListHandler;
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
            public void update(int index, ConfigTuple object, String value) {
                object.setValue(value);
            }
        });

        captionColumn.setSortable(true);
        valueColumn.setSortable(true);
		
		configTable.addResizableColumn(captionColumn, "Наименование свойства");
		configTable.addResizableColumn(valueColumn, "Значение свойства");

		configTable.setSelectionModel(new NoSelectionModel<ConfigTuple>());
        configTupleListHandler = new ColumnSortEvent.ListHandler<ConfigTuple>(dataProvider.getList());
        configTupleListHandler.setComparator(captionColumn, new Comparator<ConfigTuple>() {
            @Override
            public int compare(ConfigTuple o1, ConfigTuple o2) {
                return o1.getParam().getCaption().compareTo(o2.getParam().getCaption());
            }
        });
        configTupleListHandler.setComparator(valueColumn, new ComparatorWithNull<ConfigTuple, String>() {
            @Override
            public int compare(final ConfigTuple o1, final ConfigTuple o2) {
                return compareWithNull(o1.getValue(), o2.getValue());
            }
        });
        configTable.addColumnSortHandler(configTupleListHandler);
        configTable.getColumnSortList().push(captionColumn);
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
        configTupleListHandler.setList(dataProvider.getList());
	}

	@Override
	public List<ConfigTuple> getConfigData() {
		return configData;
	}

}
