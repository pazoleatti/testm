package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
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
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.HashSet;
import java.util.Set;


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
		Set<String> checkSet = new HashSet<String>();
		for (Column column : formTemplate.getColumns()) {
			if (!checkSet.add(column.getAlias())) {
				MessageEvent.fire(FormTemplateMainPresenter.this, "Форма не может быть сохранена," +
																  " найден повторяющийся алиас: " + column.getAlias() +
																  " для столбца: " + column.getName());
				return;
			}
		}

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
		placeManager.revealPlace(new PlaceRequest(AdminNameTokens.adminPage));
	}

	private void setFormTemplate() {
		int formId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminNameTokens.formTemplateId, "0"));
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
			}
		});
	}

}
