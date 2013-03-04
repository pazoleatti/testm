package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.*;
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
		void setTaxType(String taxType);
		void setTitle(String title);
		void setDepartment(String department);
		void setReportPeriod(String reportPeriod);
		void setBackButton(TaxType taxType);
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
	public void refreshDeclaration(){
		UpdateDeclarationDataAction action = new UpdateDeclarationDataAction();
		action.setRefresh(true);
		action.setDeclarationData(declaration);
		dispatcher.execute(action, new AbstractCallback<UpdateDeclarationDataResult>() {
			@Override
			public void onReqSuccess(UpdateDeclarationDataResult result) {
				MessageEvent.fire(this, "Декларация обновлена");
				setDeclaration();
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(this, "Запрос не выполнен", throwable);
				setDeclaration();
			}
		});
	}

	@Override
	public void setAccepted(boolean accepted) {
		UpdateDeclarationDataAction action = new UpdateDeclarationDataAction();
		declaration.setAccepted(accepted);
		action.setDeclarationData(declaration);
		dispatcher.execute(action, new AbstractCallback<UpdateDeclarationDataResult>() {
			@Override
			public void onReqSuccess(UpdateDeclarationDataResult result) {
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
	public void delete() {
		UpdateDeclarationDataAction action = new UpdateDeclarationDataAction();
		action.setDelete(true);
		action.setDeclarationData(declaration);
		dispatcher.execute(action, new AbstractCallback<UpdateDeclarationDataResult>() {
			@Override
			public void onReqSuccess(UpdateDeclarationDataResult result) {
				MessageEvent.fire(this, "Декларация удалена");
				placeManager
						.revealPlace(new PlaceRequest(DeclarationListNameTokens.DECLARATION_LIST).with("nType", taxName));
			}

			@Override
			protected boolean needErrorOnFailure() {
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				MessageEvent.fire(this, "Запрос не выполнен", throwable);
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

	@Override
	public void loadPdfFile() {
		getView().setPdfFile(GWT.getHostPageBaseURL() + "download/downloadPDF/" + declaration.getId());
	}

	private void setDeclaration() {
		final long declarationId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationDataTokens.declarationId, "0"));
		if (declarationId != 0) {
			GetDeclarationDataAction action = new GetDeclarationDataAction();
			action.setId(declarationId);
			dispatcher.execute(action, new AbstractCallback<GetDeclarationDataResult>() {
				@Override
				public void onReqSuccess(GetDeclarationDataResult result) {
					if (result.isCanRead()) {
						declaration = result.getDeclarationData();
						taxName = result.getTaxType().name();
						getView().setDeclarationData(declaration);
						getView().setTaxType(result.getTaxType().getName());
						getView().setReportPeriod(result.getReportPeriod());
						getView().setDepartment(result.getDepartment());
						getView().setBackButton(result.getTaxType());
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
						MessageEvent.fire(this, "Недостаточно прав на просмотр данных декларации");
					}
				}
			});
		}
	}

	private void updateTitle(String declarationType){
		TitleUpdateEvent.fire(this, "Декларация", declarationType);
	}
}
