package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateDeclarationResult;
import com.google.gwt.core.client.GWT;
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
		void setCannotAccept();
		void setCannotReject();
		void setTaxType(TaxType taxType);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private Declaration declaration;
	private TaxType taxType;

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
		if (!request.getParameter(DeclarationDataTokens.nType, "").isEmpty()) {
			taxType = TaxType.valueOf(request.getParameter(DeclarationDataTokens.nType, ""));
		}
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
	public void setAccepted(boolean accepted) {
		UpdateDeclarationAction action = new UpdateDeclarationAction();
		declaration.setAccepted(accepted);
		action.setDeclaration(declaration);
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

	@Override
	public void downloadExcel() {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadExcel/" + declaration.getId(), null, null);
	}

	@Override
	public void downloadAsLegislator() {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadXml/" + declaration.getId(), null, null);
	}

	private void setDeclaration() {
		final long declarationId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationDataTokens.declarationId, "0"));
		if (declarationId != 0) {
			GetDeclarationAction action = new GetDeclarationAction();
			action.setId(declarationId);
			dispatcher.execute(action, new AbstractCallback<GetDeclarationResult>() {
				@Override
				public void onReqSuccess(GetDeclarationResult result) {
					if (result.isCanRead()) {
						declaration = result.getDeclaration();
						getView().setDeclarationData(declaration);
						getView().setTaxType(taxType);
						if (!result.isCanAccept()) {
							getView().setCannotAccept();
						}
						if (!result.isCanReject()) {
							getView().setCannotReject();
						}
						getProxy().manualReveal(DeclarationDataPresenter.this);
					}
					else {
						MessageEvent.fire(this, "Недостаточно прав на просмотр данных декларации");
					}
				}
			});
		}
	}
}
