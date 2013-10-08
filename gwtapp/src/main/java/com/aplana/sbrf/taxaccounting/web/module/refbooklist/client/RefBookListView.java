package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * View для формы списка справочников
 *
 * @author Stanislav Yasinskiy
 */
public class RefBookListView extends ViewWithUiHandlers<RefBookListUiHandlers>
        implements RefBookListPresenter.MyView {

    @UiField
    GenericDataGrid<TableModel> formDataTable;
    @UiField
    TextBox filterText;
    @UiField
    RadioButton rbExternal,
                rbInternal,
                rbAll;

    private static final String[] COLUMN_NAMES = {"Наименование справочника", "Тип справочника"};

    interface Binder extends UiBinder<Widget, RefBookListView> {
    }

    @Inject
    @UiConstructor
    public RefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initTable();
    }

    private void initTable() {
        Column<TableModel, TableModel> nameColumn = new Column<TableModel, TableModel>(
                new AbstractCell<TableModel>() {
                    @Override
                    public void render(Context context, TableModel model, SafeHtmlBuilder sb) {
                        if (model == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#" + RefBookDataTokens.refBookData + ";"
                                + RefBookDataTokens.REFBOOK_DATA_ID + "=" + model.getId() + "\">"
                                + model.getName() + "</a>");
                    }
                }) {
            @Override
            public TableModel getValue(TableModel object) {
                return object;
            }
        };

        TextColumn<TableModel> typeColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                return object.getType().toString();
            }
        };

        formDataTable.addColumn(nameColumn, COLUMN_NAMES[0]);
        formDataTable.addColumn(typeColumn, COLUMN_NAMES[1]);
        formDataTable.setColumnWidth(typeColumn, 400, Style.Unit.PX);
    }

    @Override
    public void setTableData(List<TableModel> tableData) {
        formDataTable.setRowData(tableData);
    }

    @Override
    public boolean isExternalFilter(){
        return rbExternal.getValue();
    }

    @Override
    public boolean isInternalFilter(){
        return rbInternal.getValue();
    }

    @Override
    public String getFilter(){
        return filterText.getText();
    }

    @UiHandler("findButton")
    void onFindClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFindClicked();
        }
    }

    @UiHandler("loadButton")
    void onLoadClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onLoadClicked();
        }
    }
}