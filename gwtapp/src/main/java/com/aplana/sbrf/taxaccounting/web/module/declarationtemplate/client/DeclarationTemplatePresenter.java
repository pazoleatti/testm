package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

public class DeclarationTemplatePresenter extends Presenter<DeclarationTemplatePresenter.MyView, DeclarationTemplatePresenter.MyProxy>
		implements DeclarationTemplateUiHandlers {

	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplate)
	public interface MyProxy extends ProxyPlace<DeclarationTemplatePresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationTemplateUiHandlers> {
		void setDeclarationTemplate(DeclarationTemplate declaration);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private DeclarationTemplate declarationTemplate;
	private HandlerRegistration closeDeclarationTemplateHandlerRegistration;

	@Inject
	public DeclarationTemplatePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка декларации администрирования.
	 *
	 * @param request запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		setDeclarationTemplate();
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void reset() {
		setDeclarationTemplate();
	}

	@Override
	public void onHide() {
		super.onHide();
		unlockForm(declarationTemplate.getId());
		closeDeclarationTemplateHandlerRegistration.removeHandler();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
		UpdateDeclarationAction action = new UpdateDeclarationAction();
		action.setDeclarationTemplate(declarationTemplate);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<UpdateDeclarationResult>() {
					@Override
					public void onSuccess(UpdateDeclarationResult result) {
						MessageEvent.fire(DeclarationTemplatePresenter.this, "Декларация сохранена");
						setDeclarationTemplate();
					}
				}, this));
	}

	/**
	 * Закрыть декларацию редактирования и вернуться на форму администрирования со списком шаблонов деклараций.
	 */
	@Override
	public void close() {
		placeManager.revealPlace(new PlaceRequest(DeclarationTemplateTokens.declarationTemplateList));
	}

	@Override
	public void downloadJrxml() {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadJrxml/" + declarationTemplate.getId(), null, null);
	}
	
	@Override
	public void downloadDect() {
		Window.open(GWT.getHostPageBaseURL() + "download/declarationTemplate/downloadDect/" + declarationTemplate.getId(), null, null);		
	}

	@Override
	public void uploadJrxmlFail(String error) {
		MessageEvent.fire(this, "Не удалось загрузить файл. Ошибка: " + error);
	}

	private void setDeclarationTemplate() {
		final int declarationId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationTemplateId, "0"));
		if (declarationId != 0) {
			closeDeclarationTemplateHandlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
				@Override
				public void onWindowClosing(Window.ClosingEvent event) {
					unlockForm(declarationId);
					closeDeclarationTemplateHandlerRegistration.removeHandler();
				}
			});


			GetDeclarationAction action = new GetDeclarationAction();
			action.setId(declarationId);
			dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<GetDeclarationResult>() {
						@Override
						public void onSuccess(GetDeclarationResult result) {
							declarationTemplate = result.getDeclarationTemplate();
							getView().setDeclarationTemplate(declarationTemplate);
							TitleUpdateEvent.fire(DeclarationTemplatePresenter.this, "Шаблон декларации", declarationTemplate.getDeclarationType().getName());
						}
					}, this).addCallback(new ManualRevealCallback<GetDeclarationResult>(DeclarationTemplatePresenter.this)));
		}
	}

	private void unlockForm(int declarationId){
		UnlockDeclarationAction action = new UnlockDeclarationAction();
		action.setDeclarationId(declarationId);
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}

	@Override
	public void uploadDectSuccess() {
		MessageEvent.fire(DeclarationTemplatePresenter.this, "Декларация импортирована");
		setDeclarationTemplate();
	}

	@Override
	public void uploadDectFail(String msg) {
		MessageEvent.fire(this, "Не удалось импортировать шаблон. Ошибка: " + msg);
	}

}
