package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateCloseEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateResetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSaveEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateColumnUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUtil.saveFormTemplate;

public class FormTemplateColumnPresenter extends Presenter<FormTemplateColumnPresenter.MyView, FormTemplateColumnPresenter.MyProxy>
		implements FormTemplateColumnUiHandlers, FormTemplateResetEvent.MyHandler, FormTemplateSaveEvent.MyHandler,
		FormTemplateCloseEvent.MyHandler {

	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateColumnPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = "Описание столбцов",
			priority = 2)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateColumnPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateColumnUiHandlers> {
		void setColumnList(List<Column> columnList);
	}

	private DispatchAsync dispatcher;
	private FormTemplate formTemplate;
	private PlaceManager placeManager;
	private int formId;
	private boolean isSelected = false;

	@Inject
	public FormTemplateColumnPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, final DispatchAsync dispatcher, final PlaceManager placeManager) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onReset(FormTemplateResetEvent event) {
		reset();
	}

	@ProxyEvent
	@Override
	public void onSave(FormTemplateSaveEvent event) {
		if (isSelected) {
			saveFormTemplate(this, formTemplate, dispatcher);
		}
	}

	@ProxyEvent
	@Override
	public void onClose(FormTemplateCloseEvent event) {
		reset();
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		formId = Integer.valueOf(request.getParameter(AdminNameTokens.formTemplateId, "0"));
		setColumnList();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
	}

	@Override
	protected void onReveal() {
		isSelected = true;
	}

	@Override
	protected void onHide() {
		isSelected = false;
	}

	@Override
	public void addColumn() {
		Column newColumn = new StringColumn();
		newColumn.setName("колонка заглушка");
		formTemplate.getColumns().add(newColumn);
		getView().setColumnList(formTemplate.getColumns());
	}

	@Override
	public void removeColumn(int index) {
		formTemplate.getColumns().remove(index);
		getView().setColumnList(formTemplate.getColumns());
	}

	private void setColumnList() {
		int newFormId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminNameTokens.formTemplateId, "0"));
		if (newFormId != 0) {
			formId = newFormId;
			GetFormAction action = new GetFormAction();
			action.setId(formId);
			dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
				@Override
				public void onReqSuccess(GetFormResult result) {
					formTemplate = result.getForm();
					getView().setColumnList(formTemplate.getColumns());
				}
			});
		}
	}

	private void reset() {
		formId = 0;
		formTemplate = null;
		setColumnList();
	}
}
