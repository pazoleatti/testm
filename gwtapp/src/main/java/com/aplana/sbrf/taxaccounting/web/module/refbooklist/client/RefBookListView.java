package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Представление списка справочников. Для конфигуратора
 *
 * @author Fail Mukhametdinov
 */
public class RefBookListView extends AbstractRefBookListView implements RefBookListPresenter.MyView {

    public static final String[] COLUMN_NAMES = {"Наименование справочника", "Тип справочника"};

    @UiField
    LinkAnchor loadButton;

    @Inject
    @UiConstructor
    public RefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        setSelectionModel();
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
                        sb.appendHtmlConstant("<a href=\"#" +
                                (RefBookType.LINEAR == model.getType() ? RefBookDataTokens.refBookData : RefBookDataTokens.refBookHierData) + ";"
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
            public String getValue(TableModel tableModel) {
                return tableModel.isReadOnly() ? "Только для чтения" : "Редактируемый";
            }
        };

        formDataTable.addResizableColumn(nameColumn, COLUMN_NAMES[0]);
        formDataTable.addResizableColumn(typeColumn, COLUMN_NAMES[1]);
        formDataTable.setColumnWidth(typeColumn, 400, Style.Unit.PX);
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("loadButton")
    void onLoadClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onLoadClicked();
        }
    }

    @Override
    public void hideLoadButton(boolean hide) {
        loadButton.setVisible(!hide);
    }

    @UiTemplate("RefBookListView.ui.xml")
    interface Binder extends UiBinder<Widget, RefBookListView> {
    }
}