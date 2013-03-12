package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;
import com.google.inject.*;
import com.google.web.bindery.event.shared.*;
import com.gwtplatform.dispatch.shared.*;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

public class DeclarationDataPresenter extends Presenter<DeclarationDataPresenter.MyView, DeclarationDataPresenter.MyProxy>
		implements DeclarationDataUiHandlers {

	@ProxyCodeSplit
	@NameToken(DeclarationDataTokens.declarationData)
	public interface MyProxy extends ProxyPlace<DeclarationDataPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationDataUiHandlers> {
		void setDeclarationData(DeclarationData declaration);
		void setCannotAccept();
		void setCannotReject();
		void setCannotDownloadXml();
		void setCannotDelete();
		void setType(String type);
		void setTitle(String title);
		void setDepartment(String department);
		void setReportPeriod(String reportPeriod);
		void setBackButton(String link);
		void setPdfFile(String fileUrl);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private DeclarationData declaration;
	private String taxName;

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
		final long declarationId = Integer.valueOf(request.getParameter(DeclarationDataTokens.declarationId, "0"));
		if (declarationId != 0) {
			GetDeclarationDataAction action = new GetDeclarationDataAction();
			action.setId(declarationId);
			dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<GetDeclarationDataResult>() {
						@Override
						public void onSuccess(GetDeclarationDataResult result) {
							if (result.isCanRead()) {
								declaration = result.getDeclarationData();
								taxName = result.getTaxType().name();
								getView().setDeclarationData(declaration);
								getView().setType("Декларация");
								getView().setReportPeriod(result.getReportPeriod());
								getView().setDepartment(result.getDepartment());
								getView().setBackButton("#" + DeclarationListNameTokens.DECLARATION_LIST + ";nType="
										+ String.valueOf(result.getTaxType()));
								getView().setTitle(result.getTaxType().getName() + " / " + result.getDeclarationType());
								updateTitle(result.getDeclarationType());
								loadPdfFile();

								if (!result.isCanAccept()) {
									getView().setCannotAccept();
								}
								if (!result.isCanReject()) {
									getView().setCannotReject();
								}
								if (!result.isCanDownload()) {
									getView().setCannotDownloadXml();
								}
								if (!result.isCanDelete()) {
									getView().setCannotDelete();
								}

								getProxy().manualReveal(DeclarationDataPresenter.this);
							}
							else {
								MessageEvent.fire(DeclarationDataPresenter.this, "Недостаточно прав на просмотр данных декларации");
							}
						}
					}));
		}
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
	public void refreshDeclaration(){
		LogCleanEvent.fire(this);
		RefreshDeclarationDataAction action = new RefreshDeclarationDataAction();
		action.setDeclarationId(declaration.getId());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<RefreshDeclarationDataResult>() {
					@Override
					public void onSuccess(RefreshDeclarationDataResult result) {
						MessageEvent.fire(DeclarationDataPresenter.this, "Декларация обновлена");
						revealPlaceRequest();
					}
				}));
	}

	@Override
	public void accept(boolean accepted) {
		LogCleanEvent.fire(this);
		AcceptDeclarationDataAction action = new AcceptDeclarationDataAction();
		action.setAccepted(accepted);
		action.setDeclarationId(declaration.getId());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<AcceptDeclarationDataResult>() {
					@Override
					public void onSuccess(AcceptDeclarationDataResult result) {
						MessageEvent.fire(DeclarationDataPresenter.this, "Декларация сохранена");
						revealPlaceRequest();
					}
				}));
	}

	@Override
	public void delete() {
		LogCleanEvent.fire(this);
		DeleteDeclarationDataAction action = new DeleteDeclarationDataAction();
		action.setDeclarationId(declaration.getId());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<DeleteDeclarationDataResult>() {
					@Override
					public void onSuccess(DeleteDeclarationDataResult result) {
						MessageEvent.fire(DeclarationDataPresenter.this, "Декларация удалена");
						placeManager
								.revealPlace(new PlaceRequest(DeclarationListNameTokens.DECLARATION_LIST).with("nType", taxName));
					}
				}));
	}

	@Override
	public void downloadExcel() {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadExcel/" + declaration.getId(), null, null);
	}

	@Override
	public void downloadXml() {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadXml/" + declaration.getId(), null, null);
	}

	@Override
	public void loadPdfFile() {
		getView().setPdfFile(GWT.getHostPageBaseURL() + "download/downloadPDF/" + declaration.getId());
	}

	private void revealPlaceRequest() {
		placeManager.revealPlace(new PlaceRequest(DeclarationDataTokens.declarationData)
				.with(DeclarationDataTokens.declarationId, String.valueOf(declaration.getId())));
	}

	private void updateTitle(String declarationType){
		TitleUpdateEvent.fire(this, "Декларация", declarationType);
	}
}
