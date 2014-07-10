package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetDeclarationLogsBusinessAction;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetFormLogsBusinessAction;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetLogsBusinessResult;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.LogBusinessClient;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

public class HistoryPresenter extends
		PresenterWidget<HistoryView> {

	public static interface MyView extends PopupView{
		void setHistory(List<LogBusinessClient> logs);
        void updateTitle(TaxType taxType1);
	}

	private final DispatchAsync dispatcher;

	@Inject
	public HistoryPresenter(final EventBus eventBus, final HistoryView view, DispatchAsync dispatcher) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
	}

	public void prepareDeclarationHistory(long declarationId, TaxType taxType) {
        getView().updateTitle(taxType);
		GetDeclarationLogsBusinessAction action = new GetDeclarationLogsBusinessAction();
		action.setId(declarationId);
		prepareHistory(action);
	}

	public void prepareFormHistory(long formId, TaxType taxType) {
        getView().updateTitle(taxType);
		GetFormLogsBusinessAction action = new GetFormLogsBusinessAction();
		action.setId(formId);
		prepareHistory(action);
	}

	private void prepareHistory(UnsecuredActionImpl<GetLogsBusinessResult> action) {
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetLogsBusinessResult>() {
					@Override
					public void onSuccess(GetLogsBusinessResult result) {
						getView().setHistory(result.getLogs());
					}
				}, this));
	}

}
