package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.Type;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

/**
 * View для формы списка справочников
 *
 * @author Stanislav Yasinskiy
 */
public class RefBookListView extends ViewWithUiHandlers<RefBookListUiHandlers>
        implements RefBookListPresenter.MyView {

    @UiField
    Button findButton;
    @UiField
    Button loadButton;
    @UiField
    GenericDataGrid<TableModel> formDataTable;
    @UiField
    FlexiblePager pager;
    @UiField
    TextBox filterText;
    @UiField
    RadioButton rbExternal;
    @UiField
    RadioButton rbInternal;
    @UiField
    RadioButton rbAll;

    interface Binder extends UiBinder<Widget, RefBookListView> {
    }

    @Inject
    @UiConstructor
    public RefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initTable();
    }

    @UiHandler("findButton")
    void onFindClicked(ClickEvent event) {
        Type type = null;
        if (rbExternal.getValue()) {
            type = Type.EXTERNAL;
        } else if (rbInternal.getValue()) {
            type = Type.INTERNAL;
        }
        getUiHandlers().init(type, filterText.getText());
    }

    @UiHandler("loadButton")
    void onLoadClicked(ClickEvent event) {
        // TODO загрузка справочников http://conf.aplana.com/pages/viewpage.action?pageId=9572224
    }

    private void initTable() {

        Column<TableModel, TableModel> nameColumn = new Column<TableModel, TableModel>(
                new AbstractCell<TableModel>() {

                    @Override
                    public void render(Context context, TableModel model, SafeHtmlBuilder sb) {
                        if (model == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#" + RefBookListTokens.refbookList + ";"
                                // TODO поменять на токен
                                + "id=" +
                                +model.getId() + "\">"
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
                return object.getType().getName();
            }
        };

        formDataTable.addColumn(nameColumn, "Наименование справочника");
        formDataTable.addColumn(typeColumn, "Тип справочника");
        formDataTable.setColumnWidth(typeColumn, 400, Style.Unit.PX);

        formDataTable.setRowCount(0);
        pager.setDisplay(formDataTable);
    }

    @Override
    public void init(List<TableModel> tableData) {
        formDataTable.setRowCount(0);
        if (tableData != null && tableData.size() > 0)
            formDataTable.setRowData(tableData);
    }
}