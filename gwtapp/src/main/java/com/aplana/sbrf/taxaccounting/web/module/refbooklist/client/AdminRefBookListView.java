package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Представление списка справочников. Для конфигуратора
 *
 * @author Fail Mukhametdinov
 */
public class AdminRefBookListView extends AbstractRefBookListView implements AdminRefBookListPresenter.MyView {

    @Inject
    @UiConstructor
    public AdminRefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initTable();
        loadButton.removeFromParent();
    }

    private void initTable() {
        Column<TableModel, TableModel> nameColumn = new Column<TableModel, TableModel>(
                new AbstractCell<TableModel>() {
                    @Override
                    public void render(Context context, TableModel model, SafeHtmlBuilder sb) {
                        if (model == null) {
                            return;
                        }
                        sb.appendHtmlConstant(model.getName());
                    }
                }) {
            @Override
            public TableModel getValue(TableModel object) {
                return object;
            }
        };

        TextColumn<TableModel> typeColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.isReadOnly() ? "Не редактируемый" : "Редактируемый";
            }
        };

        formDataTable.addResizableColumn(nameColumn, AbstractRefBookListView.COLUMN_NAMES[0]);
        formDataTable.addResizableColumn(typeColumn, AbstractRefBookListView.COLUMN_NAMES[1]);
        formDataTable.setColumnWidth(typeColumn, 400, Style.Unit.PX);
    }

    @UiTemplate("RefBookListView.ui.xml")
    interface Binder extends UiBinder<Widget, AdminRefBookListView> {
    }
}
