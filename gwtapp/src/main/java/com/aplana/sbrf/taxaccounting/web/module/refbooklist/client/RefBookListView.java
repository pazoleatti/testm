package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Представление списка справочников. Для конфигуратора
 *
 * @author Fail Mukhametdinov
 */
public class RefBookListView extends AbstractRefBookListView implements RefBookListPresenter.MyView {

    @Inject
    @UiConstructor
    public RefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        setSelectionModel();
        initColumns(false);
        initTable();
    }

    private void initTable() {
        formDataTable.addResizableColumn(nameColumn, COLUMN_NAMES[0]);
        formDataTable.addResizableColumn(editableColumn, COLUMN_NAMES[2]);
        formDataTable.setColumnWidth(editableColumn, 400, Style.Unit.PX);
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("loadButton")
    void onLoadClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onLoadClicked();
        }
    }

    @UiTemplate("RefBookListView.ui.xml")
    interface Binder extends UiBinder<Widget, RefBookListView> {
    }
}