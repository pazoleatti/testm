package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateMainUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormResult;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;


public class FormTemplateMainPresenter extends TabContainerPresenter<FormTemplateMainPresenter.MyView, FormTemplateMainPresenter.MyProxy> implements FormTemplateMainUiHandlers {

	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateMainPage)
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

	@Override
	protected void revealInParent() {
		setFormTemplate();
	}

	@Override
	public void reset() {
		setFormTemplate();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
		FormTemplateFlushEvent.fire(this);
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				saveAfterFlush();
			}
		});
	}

	/**
	 * Закрыть форму редактирования и вернуться на форму администрирования со списком шаблонов форм.
	 */
	@Override
	public void close() {
		placeManager.revealPlace(new PlaceRequest(AdminConstants.NameTokens.adminPage));
	}

	private void setFormTemplate() {
		int formId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminConstants.NameTokens.formTemplateId, "0"));
		if (formId != 0) {
			GetFormAction action = new GetFormAction();
			action.setId(formId);
			dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
				@Override
				public void onReqSuccess(GetFormResult result) {
					formTemplate = result.getForm();
					getView().setTitle(formTemplate.getType().getName());
					getView().setFormId(formTemplate.getId());
					RevealContentEvent.fire(FormTemplateMainPresenter.this, RevealContentTypeHolder.getMainContent(), FormTemplateMainPresenter.this);
					FormTemplateSetEvent.fire(FormTemplateMainPresenter.this, formTemplate);
				}
			});
		}
	}

	private void saveAfterFlush() {
		UpdateFormAction action = new UpdateFormAction();
		action.setForm(formTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateFormResult>() {
			@Override
			public void onReqSuccess(UpdateFormResult result) {
				MessageEvent.fire(this, "Форма Сохранена");
				setFormTemplate();
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(this, "Request Failure", throwable);
				setFormTemplate();
			}
		});
	}

}
