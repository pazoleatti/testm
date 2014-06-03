package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * Абстрактный класс представления списка справочников
 *
 * @author Stanislav Yasinskiy
 * @author Fail Mukhametdinov
 */
public abstract class AbstractRefBookListView extends ViewWithUiHandlers<RefBookListUiHandlers>
        implements AbstractRefBookListPresenter.MyView {

    private SingleSelectionModel<TableModel> selectionModel;

    @UiField
    GenericDataGrid<TableModel> formDataTable;
    @UiField
    TextBox filterText;
    @UiField
    LinkAnchor loadButton;

    @Override
    public void setTableData(List<TableModel> tableData, Long selectedItemId) {
        formDataTable.setRowData(tableData);
        selectionModel.clear();
        if (selectedItemId != null) {
            for(TableModel item: tableData) {
                if (item.getId().equals(selectedItemId)) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
    }

    protected void setSelectionModel() {
        selectionModel = new SingleSelectionModel<TableModel>();
        formDataTable.setSelectionModel(selectionModel);
    }

    @Override
    public Long getSelectedId() {
        TableModel item = selectionModel.getSelectedObject();
        if (item != null) {
            return item.getId();
        }
        return null;
    }

    @Override
    public String getFilter() {
        return filterText.getText();
    }

    @Override
    public void setFilter(String filterText) {
        this.filterText.setText(filterText);
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("findButton")
    void onFindClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFindClicked();
        }
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("filterText")
    void onFilterPressClicked(KeyPressEvent event) {
        if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            if (getUiHandlers() != null) {
                getUiHandlers().onFindClicked();
            }
        }
    }
}
