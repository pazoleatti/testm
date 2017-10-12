package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.List;

public class EventScriptView extends PopupViewWithUiHandlers<EventScriptUiHandlers> implements EventScriptPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, EventScriptView> {

    }

    private final PopupPanel widget;

    @UiField
    GenericDataGrid<FormDataEvent> eventTable;

    private ListDataProvider<FormDataEvent> dataProvider = new ListDataProvider<FormDataEvent>();

    @Inject
    public EventScriptView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        dataProvider.addDataDisplay(eventTable);
        initTable();
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    private void initTable() {
        eventTable.setPageSize(100);
        TextColumn<FormDataEvent> idColumn = new TextColumn<FormDataEvent>() {
            @Override
            public String getValue(FormDataEvent object) {
                return String.valueOf(object.getCode());
            }
        };
        TextColumn<FormDataEvent> nameColumn = new TextColumn<FormDataEvent>() {
            @Override
            public String getValue(FormDataEvent object) {
                return object.getTitle();
            }
        };
        eventTable.addColumn(idColumn, "Код");
        eventTable.setColumnWidth(idColumn, 10, Style.Unit.PX);
        eventTable.addColumn(nameColumn, "Название события");
        eventTable.setColumnWidth(nameColumn, 55, Style.Unit.PX);
    }

    @Override
    public void updateTableData(List<FormDataEvent> eventList) {
        eventTable.setRowData(0, eventList);
    }

    @UiHandler("create")
    public void onCreateClicked(ClickEvent event) {
        FormDataEvent selectedEvent = eventTable.getVisibleItem(eventTable.getKeyboardSelectedRow());
        getUiHandlers().onCreate(selectedEvent.getCode());
    }

    @UiHandler("close")
    public void onCloseClicked(ClickEvent event) {
        getUiHandlers().onClose();
    }
}