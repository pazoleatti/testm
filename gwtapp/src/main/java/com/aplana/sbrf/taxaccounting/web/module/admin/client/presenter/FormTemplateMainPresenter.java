package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateMainUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormResult;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;


public class FormTemplateMainPresenter extends TabContainerPresenter<FormTemplateMainPresenter.MyView, FormTemplateMainPresenter.MyProxy> implements FormTemplateMainUiHandlers {

	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateMainPage)
	public interface MyProxy extends ProxyPlace<FormTemplateMainPresenter>, Place {
	}

	public interface MyView extends TabView, HasUiHandlers<FormTemplateMainUiHandlers> {
		void setTitle(String title);
		void setFormId(int formId);
	}

	@RequestTabs
	public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<RequestTabsHandler>();

	@ChangeTab
	public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<ChangeTabHandler>();

	@ContentSlot
	public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<RevealContentHandler<?>>();

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private FormTemplate formTemplate;

	@Inject
	public FormTemplateMainPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	/**
	 * Подготовка формы. Здесь мы получаем с сервера шаблон формы и биндим его на форму.
	 *
	 * @param request запрос, из него мы получаем идентификатор шаблона формы.
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		int formId = Integer.valueOf(request.getParameter(AdminNameTokens.formTemplateId, "0"));

		placeManager.revealPlace(
				new PlaceRequest(AdminNameTokens.formTemplateScriptPage).with(
						AdminNameTokens.formTemplateId, String.valueOf(formId)
				)
		);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}

	@Override
	protected void onReveal() {
		resetFormTemplate();
	}

	@Override
	public void reset() {
		resetFormTemplate();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
		UpdateFormAction action = new UpdateFormAction();
		action.setForm(formTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateFormResult>() {
			@Override
			public void onReqSuccess(UpdateFormResult result) {
				MessageEvent.fire(this, "Форма Сохранена");
				resetFormTemplate();
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(this, "Request Failure", throwable);
				resetFormTemplate();
			}
		});
	}

	/**
	 * Закрыть форму редактирования и вернуться на форму администрирования со списком шаблонов форм.
	 */
	@Override
	public void close() {
		resetFormTemplate();
		placeManager.revealPlace(new PlaceRequest(AdminNameTokens.adminPage));
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	private void resetFormTemplate() {
		int formId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminNameTokens.formTemplateId, "0"));
		if (formId != 0) {
			getView().setFormId(formId);
			GetFormAction action = new GetFormAction();
			action.setId(formId);
			dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
				@Override
				public void onReqSuccess(GetFormResult result) {
					formTemplate = result.getForm();
					getView().setTitle(formTemplate.getType().getName());
					FormTemplateSetEvent.fire(FormTemplateMainPresenter.this);
				}
			});
		}
	}

}
