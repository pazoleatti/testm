package com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow;

import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.NotificationTableRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

public class DialogView extends PopupViewWithUiHandlers<DialogUiHandlers> implements DialogPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, DialogView> {
	}

	@UiField
	Button cancelButton;

    @UiField
    GenericCellTable<NotificationTableRow> notificationTable;

	@UiField
	FlexiblePager pager;

	private DateTimeFormat formatter = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    private ColumnSortEvent.AsyncHandler sortHandler;
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

	    notificationTable.addColumn(dateColumn, "Дата оповещения");
	    notificationTable.setColumnWidth(dateColumn, 115, Style.Unit.PX);
	    notificationTable.addColumn(contentColumn, "Содержание");

        notificationTable.getColumnSortList().setLimit(1);

        sortHandler = new ColumnSortEvent.AsyncHandler(notificationTable);
        notificationTable.addColumnSortHandler(sortHandler);

        dataProvider.addDataDisplay(notificationTable);

	    notificationTable.setPageSize(pager.getPageSize());
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
}
