package com.aplana.sbrf.taxaccounting.web.module.formdataimport.client;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdataimport.shared.FormDataImportAction;
import com.aplana.sbrf.taxaccounting.web.module.formdataimport.shared.FormDataImportResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FormDataImportPresenter
		extends
		Presenter<FormDataImportPresenter.MyView, FormDataImportPresenter.MyProxy> {

	public static final String FDIMPORT = "!formDataImport";

	public static final String DEPARTMENT_ID = "departmentId";
	public static final String FORM_DATA_KIND_ID = "formDataKindId";
	public static final String FORM_DATA_TEMPLATE_ID = "formDataTemplateId";
	public static final String FORM_DATA_RPERIOD_ID = "reportPeriodId";

	@ProxyStandard
	@NameToken(FDIMPORT)
	public interface MyProxy extends ProxyPlace<FormDataImportPresenter> {
	}

	public interface MyView extends View {

	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	@Inject
	public FormDataImportPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, PlaceManager placeManager,
			DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			FormDataImportAction action = new FormDataImportAction();
			action.setFormTemplateId(ParamUtils.getInteger(request,
					FORM_DATA_TEMPLATE_ID));
			action.setDepartmentId(ParamUtils
					.getInteger(request, DEPARTMENT_ID));
			action.setKind(FormDataKind.fromId(ParamUtils.getInteger(request,
					FORM_DATA_KIND_ID)));
			action.setReportPeriodId(ParamUtils.getInteger(request,
					FORM_DATA_RPERIOD_ID));

			dispatcher.execute(action, CallbackUtils.defaultCallback(
					new AbstractCallback<FormDataImportResult>() {
						@Override
						public void onSuccess(FormDataImportResult result) {
							LogAddEvent.fire(FormDataImportPresenter.this,
									result.getLogEntries());

							MessageEvent.fire(FormDataImportPresenter.this,
									"Форма успешно импортирована");
							getProxy().manualRevealFailed();
						}

					}, FormDataImportPresenter.this));

			super.prepareFromRequest(request);
		} catch (Exception e) {
			((TaPlaceManager) placeManager).navigateBackQuietly();
			getProxy().manualRevealFailed();
			MessageEvent.fire(this, "Не импортировать форму", e);
		}
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

}
