package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Представление списка справочников. Для конфигуратора
 *
 * @author Fail Mukhametdinov
 */
public class AdminRefBookListView extends AbstractRefBookListView implements AdminRefBookListPresenter.MyView {

    public static final String[] COLUMN_NAMES = {"Наименование справочника", "Видимость справочника", "Тип справочника", "Региональность справочника", "Вид справочника"};

    @UiField
    Label formHeader;

    @Inject
    @UiConstructor
    public AdminRefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initTable();
        loadButton.removeFromParent();
        formHeader.setText("Настройки");
    }

    private void initTable() {
        Column<TableModel, TableModel> nameColumn = new Column<TableModel, TableModel>(
                new AbstractCell<TableModel>() {
                    @Override
                    public void render(Context context, TableModel model, SafeHtmlBuilder sb) {
                        if (model == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#" +
                                (RefBookType.LINEAR == model.getType() ? RefBookDataTokens.REFBOOK_SCRIPT : RefBookDataTokens.refBookHierData) + ";"
                                + RefBookDataTokens.REFBOOK_DATA_ID + "=" + model.getId() + "\">"
                                + model.getName() + "</a>");
                    }
                }) {
            @Override
            public TableModel getValue(TableModel object) {
                return object;
            }
        };

        TextColumn<TableModel> visibleColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.isVisible() ? "Скрытый" : "Видимый";
            }
        };

        TextColumn<TableModel> editableColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.isReadOnly() ? "Только чтение" : "Редактируемый";
            }
        };

        TextColumn<TableModel> regionColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.getRegionAttributeId() == null ? "Общий" : "Региональный";
            }
        };

        TextColumn<TableModel> typeColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.getRefBookType().getId() == 0 ? "Линейный" : "Иерархический";
            }
        };

        formDataTable.addResizableColumn(nameColumn, COLUMN_NAMES[0]);
        formDataTable.addResizableColumn(visibleColumn, COLUMN_NAMES[1]);
        formDataTable.addResizableColumn(editableColumn, COLUMN_NAMES[2]);
        formDataTable.addResizableColumn(regionColumn, COLUMN_NAMES[3]);
        formDataTable.addResizableColumn(typeColumn, COLUMN_NAMES[4]);
        formDataTable.setColumnWidth(visibleColumn, 200, Style.Unit.PX);
        formDataTable.setColumnWidth(editableColumn, 200, Style.Unit.PX);
        formDataTable.setColumnWidth(regionColumn, 200, Style.Unit.PX);
        formDataTable.setColumnWidth(typeColumn, 200, Style.Unit.PX);
    }

    @UiTemplate("RefBookListView.ui.xml")
    interface Binder extends UiBinder<Widget, AdminRefBookListView> {
    }
}
