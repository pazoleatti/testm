package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.LogBusinessClient;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.List;

public class HistoryView extends PopupViewWithUiHandlers<AplanaUiHandlers> implements
        HistoryPresenter.MyView {

    public static final String MODAL_WINDOW_TITLE = "Информация по налоговой форме/декларации";
    public static final String MODAL_WINDOW_TITLE_D = "Информация по форме/уведомлению";
    public static final String MODAL_WINDOW_TITLE_E = "Информация по форме";
    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    private static final String DECLARATION_SAVE_EVENT = "Обновление";
    @UiField
    ModalWindow modalWindowTitle;
    @UiField
    GenericDataGrid<LogBusinessClient> logsTable;
    @UiField
    Button hideButton;
    private AsyncDataProviderWithSortableTable dataProvider;
    private HistoryBusinessSearchOrdering sortByColumn;
    private final PopupPanel widget;

    @Inject
    public HistoryView(EventBus eventBus, Binder uiBinder) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        setTableColumns();

        dataProvider = new AsyncDataProviderWithSortableTable<LogBusinessClient, AplanaUiHandlers, HistoryView>(logsTable, this) {
            @Override
            public AplanaUiHandlers getViewUiHandlers() {
                return getUiHandlers();
            }
        };
        dataProvider.setAscSorting(false);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setHistory(List<LogBusinessClient> logs) {
        logsTable.setRowData(logs);
        logsTable.redraw();
    }

    @Override
    public void updateTitle(TaxType taxType) {
        if (taxType.equals(TaxType.DEAL)) {
            modalWindowTitle.setText(MODAL_WINDOW_TITLE_D);
        } else if (taxType.equals(TaxType.ETR) || taxType.equals(TaxType.MARKET)) {
            modalWindowTitle.setText(MODAL_WINDOW_TITLE_E);
        } else {
            modalWindowTitle.setText(MODAL_WINDOW_TITLE);
        }
    }

    @Override
    public HistoryBusinessSearchOrdering getSearchOrdering() {
        if (sortByColumn == null) {
            sortByColumn = HistoryBusinessSearchOrdering.DATE;
        }
        return sortByColumn;
    }

    @Override
    public boolean isAscSorting() {
        return dataProvider.isAscSorting();
    }

    @Override
    public void setSortByColumn(String sortByColumn) {
        this.sortByColumn = HistoryBusinessSearchOrdering.valueOf(sortByColumn);
    }

    @UiHandler("hideButton")
    public void onHideButton(ClickEvent event) {
        hide();
    }

    private void setTableColumns() {
        TextColumn<LogBusinessClient> eventColumn = new TextColumn<LogBusinessClient>() {
            @Override
            public String getValue(LogBusinessClient object) {
                FormDataEvent event = FormDataEvent.getByCode(object.getEventId());
                if (object.getDeclarationId() != null && FormDataEvent.SAVE == event) {
                    return DECLARATION_SAVE_EVENT;
                }
                return event.getTitle();
            }
        };

        TextColumn<LogBusinessClient> dateColumn = new TextColumn<LogBusinessClient>() {
            @Override
            public String getValue(LogBusinessClient object) {
                return format.format(object.getLogDate());
            }
        };

        TextColumn<LogBusinessClient> nameColumn = new TextColumn<LogBusinessClient>() {
            @Override
            public String getValue(LogBusinessClient object) {
                return object.getUserName();
            }
        };

        TextColumn<LogBusinessClient> rolesColumn = new TextColumn<LogBusinessClient>() {
            @Override
            public String getValue(LogBusinessClient object) {
                return object.getRoles();
            }
        };

        TextColumn<LogBusinessClient> departmentColumn = new TextColumn<LogBusinessClient>() {
            @Override
            public String getValue(LogBusinessClient object) {
                return object.getDepartmentName();
            }
        };

        TextColumn<LogBusinessClient> noteColumn = new TextColumn<LogBusinessClient>() {
            @Override
            public String getValue(LogBusinessClient object) {
                return object.getNote();
            }
        };

        logsTable.addResizableColumn(eventColumn, "Событие");
        logsTable.addResizableColumn(dateColumn, "Дата-время");
        logsTable.addResizableColumn(nameColumn, "Пользователь");
        logsTable.addResizableColumn(rolesColumn, "Роли пользователя");
        logsTable.addResizableColumn(departmentColumn, "Подразделение пользователя");
        logsTable.addResizableColumn(noteColumn, "Текст события");
        logsTable.addCellPreviewHandler(new CellPreviewEvent.Handler<LogBusinessClient>() {
            @Override
            public void onCellPreview(CellPreviewEvent<LogBusinessClient> event) {
                if ("mouseover".equals(event.getNativeEvent().getType())) {
                    long index = event.getIndex();
                    TableCellElement cellElement = logsTable.getRowElement((int) index).getCells().getItem(event.getColumn());
                    if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
                        cellElement.removeAttribute("title");
                    } else {
                        cellElement.setTitle(cellElement.getInnerText());
                    }
                }
            }
        });

        eventColumn.setDataStoreName(HistoryBusinessSearchOrdering.EVENT.name());
        dateColumn.setDataStoreName(HistoryBusinessSearchOrdering.DATE.name());
        nameColumn.setDataStoreName(HistoryBusinessSearchOrdering.USER.name());
        rolesColumn.setDataStoreName(HistoryBusinessSearchOrdering.USER_ROLE.name());
        departmentColumn.setDataStoreName(HistoryBusinessSearchOrdering.DEPARTMENT.name());
        noteColumn.setDataStoreName(HistoryBusinessSearchOrdering.NOTE.name());
    }

    interface Binder extends UiBinder<PopupPanel, HistoryView> {
    }
}
