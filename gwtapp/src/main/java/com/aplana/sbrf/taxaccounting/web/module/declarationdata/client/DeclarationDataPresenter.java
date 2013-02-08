package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

public class DeclarationDataPresenter extends Presenter<DeclarationDataPresenter.MyView, DeclarationDataPresenter.MyProxy>
		implements DeclarationDataUiHandlers {

	@ProxyCodeSplit
	@NameToken(DeclarationDataTokens.declarationData)
	public interface MyProxy extends ProxyPlace<DeclarationDataPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationDataUiHandlers> {
		void setDeclarationData(Declaration declaration);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private Declaration declaration;

	@Inject
	public DeclarationDataPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка декларации.
	 *
	 * @param request запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		setDeclaration();
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}

	@Override
	public void accept() {

	}

	@Override
	public void cancel() {

	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	/*
	@Override
	public void save() {
		UpdateDeclarationAction action = new UpdateDeclarationAction();
		action.setDeclarationTemplate(declarationTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateDeclarationResult>() {
			@Override
			public void onReqSuccess(UpdateDeclarationResult result) {
				MessageEvent.fire(this, "Декларация сохранена");
				setDeclaration();
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(this, "Request Failure", throwable);
				setDeclaration();
			}
		});
	}
    */

	@Override
	public void downloadExcel() {
		//Window.open(GWT.getHostPageBaseURL() + "download/downloadJrxml/" + declarationTemplate.getId(), null, null);
	}

	@Override
	public void downloadAsLegislator() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private void setDeclaration() {
		final long declarationId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationDataTokens.declarationDataId, "0"));
		if (declarationId != 0) {

			Declaration dec = new Declaration();
			dec.setId(declarationId);
			dec.setAccepted(true);
			dec.setDeclarationTemplateId(1);
			dec.setDepartmentId(1);
			dec.setReportPeriodId(2);
			getView().setDeclarationData(dec);
			getProxy().manualReveal(DeclarationDataPresenter.this);
			/*
			GetDeclarationAction action = new GetDeclarationAction();
			action.setId(declarationId);
			dispatcher.execute(action, new AbstractCallback<GetDeclarationResult>() {
				@Override
				public void onReqSuccess(GetDeclarationResult result) {
					declaration = result.getDeclarationData();
					getView().setDeclarationData(declaration);
					getProxy().manualReveal(DeclarationDataPresenter.this);
				}
			});
			*/
		}
	}
}
