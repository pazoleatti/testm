package com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

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

    @Override
    public void deleteNotifications(Set<NotificationTableRow> selectedSet) {
        final List<Long> notificationIds = new ArrayList<Long>();
        for (NotificationTableRow notification : selectedSet) {
            notificationIds.add(notification.getId());
        }
        final DeleteNotificationAction action = new DeleteNotificationAction();
        action.setDeleteWithoutCheck(false);
        action.setNotificationIds(notificationIds);

        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<DeleteNotificationResult>() {
                    @Override
                    public void onSuccess(final DeleteNotificationResult result) {
                        if (result.getAllowedNotifications().size() == notificationIds.size()) {
                            Dialog.confirmMessage("Подтверждение удаления оповещений", "Вы действительно хотите удалить выбранные оповещения?", new DialogHandler() {
                                @Override
                                public void yes() {
                                    deleteNotificationWithoutCheck(action);
                                }
                            });
                        } else {
                            Dialog.confirmMessage("Подтверждение удаления оповещений", "Вы действительно хотите удалить выбранные оповещения? В случае подтверждения будет удалена только часть оповещений, так как не все оповещения доступны для удаления", new DialogHandler() {
                                @Override
                                public void yes() {
                                    action.setNotificationIds(result.getAllowedNotifications());
                                    deleteNotificationWithoutCheck(action);
                                }
                            });
                        }
                    }
                }, DialogPresenter.this));
    }

    private void deleteNotificationWithoutCheck(DeleteNotificationAction action) {
        action.setDeleteWithoutCheck(true);
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<DeleteNotificationResult>() {
                    @Override
                    public void onSuccess(DeleteNotificationResult result) {
                        getView().updateData();
                    }
                }, DialogPresenter.this));

    }
}
