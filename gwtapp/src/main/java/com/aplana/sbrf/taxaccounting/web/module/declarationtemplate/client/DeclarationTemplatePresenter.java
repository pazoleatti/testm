package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

public class DeclarationTemplatePresenter extends Presenter<DeclarationTemplatePresenter.MyView, DeclarationTemplatePresenter.MyProxy>
		implements DeclarationTemplateUiHandlers {

	@Title("Шаблоны деклараций")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplate)
	public interface MyProxy extends ProxyPlace<DeclarationTemplatePresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationTemplateUiHandlers> {
		void setTitle(String title);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private DeclarationTemplate declarationTemplate;

	@Inject
	public DeclarationTemplatePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view, proxy);
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
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}

	@Override
	public void reset() {
		setDeclarationTemplate();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
		UpdateDeclarationAction action = new UpdateDeclarationAction();
		action.setDeclarationTemplate(declarationTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateDeclarationResult>() {
			@Override
			public void onReqSuccess(UpdateDeclarationResult result) {
				MessageEvent.fire(this, "Декларация сохранена");
				setDeclarationTemplate();
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(this, "Request Failure", throwable);
				setDeclarationTemplate();
			}
		});
	}

	/**
	 * Закрыть декларацию редактирования и вернуться на форму администрирования со списком шаблонов деклараций.
	 */
	@Override
	public void close() {
		placeManager.revealPlace(new PlaceRequest(DeclarationTemplateTokens.declarationTemplateList));
	}

	private void setDeclarationTemplate() {
		getView().setTitle("Самая лучшая декларация");
		int declarationId = 0;
		//int declarationId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationTemplateId, "0"));
		if (declarationId != 0) {
			GetDeclarationAction action = new GetDeclarationAction();
			action.setId(declarationId);
			dispatcher.execute(action, new AbstractCallback<GetDeclarationResult>() {
				@Override
				public void onReqSuccess(GetDeclarationResult result) {
					declarationTemplate = result.getDeclarationTemplate();
					getView().setTitle(declarationTemplate.getTaxType().getName());
				}
			});
		}
	}
}
