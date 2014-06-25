package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Представление списка справочников. Для конфигуратора
 *
 * @author Fail Mukhametdinov
 */
public class AdminRefBookListView extends AbstractRefBookListView implements AdminRefBookListPresenter.MyView {

    @UiField
    Label formHeader;

    @Inject
    @UiConstructor
    public AdminRefBookListView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        setSelectionModel();
        initColumns(true);
        initTable();
        loadButton.removeFromParent();
        formHeader.setText("Настройки");
    }

    private void initTable() {
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
