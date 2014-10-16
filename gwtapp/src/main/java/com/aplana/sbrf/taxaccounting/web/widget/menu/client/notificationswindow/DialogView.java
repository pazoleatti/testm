package com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow;

import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.NotificationTableRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

public class DialogView extends PopupViewWithUiHandlers<DialogUiHandlers> implements DialogPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, DialogView> {
	}

	@UiField
	Button cancelButton;

    @UiField
    LinkButton deleteButton;

    @UiField
    GenericCellTable<NotificationTableRow> notificationTable;

    private MultiSelectionModel<NotificationTableRow> notificationTableSM = new MultiSelectionModel<NotificationTableRow>();

	@UiField
	FlexiblePager pager;

	private DateTimeFormat formatter = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    private boolean isAsc = false;
    private NotificationsFilterData.SortColumn sortColumn = NotificationsFilterData.SortColumn.DATE;

	private final PopupPanel widget;

	private AsyncDataProvider<NotificationTableRow> dataProvider = new  AsyncDataProvider<NotificationTableRow>() {
		@Override
		protected void onRangeChanged(HasData<NotificationTableRow> display) {
			if (getUiHandlers() != null){
                if (notificationTable.getColumnSortList().size() > 0) {

                    ColumnSortList.ColumnSortInfo column = notificationTable.getColumnSortList().get(0);
                    isAsc = column.isAscending();
                    if(NotificationsFilterData.SortColumn.DATE.name().equals(column.getColumn().getDataStoreName())){
                        sortColumn = NotificationsFilterData.SortColumn.DATE;
                    } else {
                        sortColumn = NotificationsFilterData.SortColumn.TEXT;
                    }
                }
				Range range = display.getVisibleRange();
				getUiHandlers().onRangeChange(range.getStart(), range.getLength());
			}
		}
	};

    @Inject
    public DialogView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);


        Column<NotificationTableRow, Boolean> checkBoxColumn = new Column<NotificationTableRow, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(NotificationTableRow object) {
                return notificationTableSM.isSelected(object);
            }
        };
        checkBoxColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        final CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
        checkBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    for (NotificationTableRow item : notificationTable.getVisibleItems()) {
                        notificationTableSM.setSelected(item, true);
                    }
                } else {
                    notificationTableSM.clear();
                }
            }
        });
        notificationTableSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                int selectedCount = notificationTableSM.getSelectedSet().size();
                checkBoxHeader.setValue(selectedCount == notificationTable.getRowCount());
            }
        });

	    TextColumn<NotificationTableRow> dateColumn = new TextColumn<NotificationTableRow>() {
		    @Override
		    public String getValue(NotificationTableRow object) {
			    return formatter.format(object.getDate());
		    }
	    };

	    TextColumn<NotificationTableRow> contentColumn = new TextColumn<NotificationTableRow>() {
		    @Override
		    public String getValue(NotificationTableRow object) {
			    return object.getMsg();
		    }
	    };

        contentColumn.setSortable(true);
        contentColumn.setDataStoreName(NotificationsFilterData.SortColumn.TEXT.name());
        dateColumn.setDefaultSortAscending(false);
        dateColumn.setSortable(true);
        dateColumn.setDataStoreName(NotificationsFilterData.SortColumn.DATE.name());

        notificationTable.addColumn(checkBoxColumn, checkBoxHeader, 40, Style.Unit.PX);
	    notificationTable.addColumn(dateColumn, "Дата оповещения");
	    notificationTable.setColumnWidth(dateColumn, 115, Style.Unit.PX);
	    notificationTable.addColumn(contentColumn, "Содержание");

        notificationTable.getColumnSortList().setLimit(1);

        ColumnSortEvent.AsyncHandler sortHandler = new ColumnSortEvent.AsyncHandler(notificationTable);
        notificationTable.addColumnSortHandler(sortHandler);

        dataProvider.addDataDisplay(notificationTable);

	    notificationTable.setPageSize(pager.getPageSize());
        notificationTable.setSelectionModel(notificationTableSM, createCustomManager(
                new DefaultSelectionEventManager.CheckboxEventTranslator<NotificationTableRow>(0) {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<NotificationTableRow> event) {
                        return false;
                    }

                    @Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<NotificationTableRow> event) {
                        return DefaultSelectionEventManager.SelectAction.TOGGLE;
                    }
                }));
	    pager.setDisplay(notificationTable);
    }

    @Override
    public Widget asWidget() {
		return widget;
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}

    @UiHandler("deleteButton")
    public void cancel(ClickEvent event) {
        if (notificationTableSM.getSelectedSet().isEmpty()) {
            return;
        }
        getUiHandlers().deleteNotifications(notificationTableSM.getSelectedSet());
    }

	@Override
	public void setRows(PagingResult<NotificationTableRow> rows, int startIndex) {
		notificationTable.setRowCount(rows.getTotalCount());
		notificationTable.setRowData(startIndex, rows);
	}

	@Override
	public void updateData(int pageNumber) {
		if (pager.getPage() == pageNumber){
			updateData();
		} else {
			pager.setPage(pageNumber);
		}
	}

	@Override
	public void updateData() {
		notificationTable.setVisibleRangeAndClearData(notificationTable.getVisibleRange(), true);
	}

    @Override
    public boolean isAsc() {
        return isAsc;
    }
    @Override
    public NotificationsFilterData.SortColumn getSortColumn(){
        return sortColumn;
	}

    @Override
    public void clearSelected() {
        notificationTableSM.clear();
    }
}
