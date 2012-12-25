package com.aplana.sbrf.taxaccounting.web.module.admin.client;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateResetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormResult;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;

public class FormTemplateUtil {
	public static void saveFormTemplate(final Presenter presenter, FormTemplate formTemplate, DispatchAsync dispatcher) {
		UpdateFormAction action = new UpdateFormAction();
		action.setForm(formTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateFormResult>() {
			@Override
			public void onReqSuccess(UpdateFormResult result) {
				MessageEvent.fire(presenter, "Форма Сохранена");
				FormTemplateResetEvent.fire(presenter);
				super.onReqSuccess(result);
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(presenter, "Request Failure", throwable);
			}
		});
	}
}
