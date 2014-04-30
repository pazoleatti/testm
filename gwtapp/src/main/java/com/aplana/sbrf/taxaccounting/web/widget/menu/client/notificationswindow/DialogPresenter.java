package com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow;

import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationsAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationsResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.NotificationTableRow;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;

	public interface MyView extends PopupView, HasUiHandlers<DialogUiHandlers> {
		void setRows(PagingResult<NotificationTableRow> rows, int startIndex);
		void updateData(int pageNumber);
		void updateData();

        boolean isAsc();

        NotificationsFilterData.SortColumn getSortColumn();
    }

	@Inject
	public DialogPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
		super(eventBus, view);
		this.placeManager = placeManager;
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		getView().updateData(0);
	}

	@Override
	public void onRangeChange(final int start, int length) {


        NotificationsFilterData filterData = new NotificationsFilterData();
        filterData.setStartIndex(start);
        filterData.setCountOfRecords(length);
        filterData.setAsc(getView().isAsc());
        filterData.setSortColumn(getView().getSortColumn());

		dispatchAsync.execute(new GetNotificationsAction(filterData), CallbackUtils
				.defaultCallback(new AbstractCallback<GetNotificationsResult>() {
					@Override
					public void onSuccess(GetNotificationsResult result) {
						getView().setRows(result.getRows(), start);
					}
				}, DialogPresenter.this));
	}
}
