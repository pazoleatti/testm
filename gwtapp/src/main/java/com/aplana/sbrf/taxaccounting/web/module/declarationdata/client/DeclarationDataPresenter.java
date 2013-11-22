package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
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
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Date;

public class DeclarationDataPresenter
		extends
		Presenter<DeclarationDataPresenter.MyView, DeclarationDataPresenter.MyProxy>
		implements DeclarationDataUiHandlers {

	@ProxyCodeSplit
	@NameToken(DeclarationDataTokens.declarationData)
	public interface MyProxy extends ProxyPlace<DeclarationDataPresenter>,
			Place {
	}

	public interface MyView extends View,
			HasUiHandlers<DeclarationDataUiHandlers> {
		void showAccept(boolean show);

		void showReject(boolean show);

		void showRefresh(boolean show);

		void showDownloadXml(boolean show);

		void showDelete(boolean show);

		void setType(String type);

		void setTitle(String title);

		void setDepartment(String department);

		void setReportPeriod(String reportPeriod);

		void setBackButton(String link);

		void setPdf(Pdf pdf);

		void setDocDate(Date date);
	}

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;
	private final DialogPresenter dialogPresenter;
	private final HistoryPresenter historyPresenter;
	private long declarationId;
	private String taxName;

	@Inject
	public DeclarationDataPresenter(final EventBus eventBus, final MyView view,
									final MyProxy proxy, DispatchAsync dispatcher,
									PlaceManager placeManager, DialogPresenter dialogPresenter,
									HistoryPresenter historyPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager) placeManager;
		this.dialogPresenter = dialogPresenter;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка декларации.
	 * 
	 * @param request
	 *            запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		final long id = ParamUtils.getLong(request,
				DeclarationDataTokens.declarationId);

		GetDeclarationDataAction action = new GetDeclarationDataAction();
		action.setId(id);
		dispatcher.execute(
				action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetDeclarationDataResult>() {
							@Override
							public void onSuccess(
									GetDeclarationDataResult result) {
								declarationId = id;
								taxName = result.getTaxType().name();
								getView().setType("Декларация");
								getView().setReportPeriod(
										result.getReportPeriodYear() + ", " + result.getReportPeriod());
								getView().setDocDate(result.getDocDate());
								getView().setDepartment(result.getDepartment());
								getView()
										.setBackButton(
												"#"
														+ DeclarationListNameTokens.DECLARATION_LIST
														+ ";nType="
														+ String.valueOf(result
																.getTaxType()));
								getView().setTitle(
										result.getTaxType().getName() + " / "
												+ result.getDeclarationType());
								updateTitle(result.getDeclarationType());

								getView().showAccept(result.isCanAccept());
								getView().showReject(result.isCanReject());
								getView().showDownloadXml(
										result.isCanDownload());
								getView().showDelete(result.isCanDelete());
								getView().showRefresh(result.isCanDelete());
								getView().setPdf(result.getPdf());
							}
						}, DeclarationDataPresenter.this).addCallback(
						TaManualRevealCallback.create(
								DeclarationDataPresenter.this, placeManager)));

	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void refreshDeclaration(Date docDate) {
		LogCleanEvent.fire(this);
		RefreshDeclarationDataAction action = new RefreshDeclarationDataAction();
		action.setDeclarationId(declarationId);
		action.setDocDate(docDate);
		dispatcher
				.execute(
						action,
						CallbackUtils
								.defaultCallback(new AbstractCallback<RefreshDeclarationDataResult>() {
									@Override
									public void onSuccess(
											RefreshDeclarationDataResult result) {
                                        LogAddEvent.fire(DeclarationDataPresenter.this, result.getLogEntries());
										MessageEvent.fire(
												DeclarationDataPresenter.this,
												"Декларация обновлена");
										revealPlaceRequest();
									}
								}, DeclarationDataPresenter.this));
	}

	@Override
	public void accept(boolean accepted) {
		if (accepted) {
			LogCleanEvent.fire(this);
			AcceptDeclarationDataAction action = new AcceptDeclarationDataAction();
			action.setAccepted(accepted);
			action.setDeclarationId(declarationId);
			dispatcher
					.execute(
							action,
							CallbackUtils
									.defaultCallback(new AbstractCallback<AcceptDeclarationDataResult>() {
										@Override
										public void onSuccess(
												AcceptDeclarationDataResult result) {
											revealPlaceRequest();
										}
									}, DeclarationDataPresenter.this));
		} else {
			dialogPresenter.setDeclarationId(declarationId);
			addToPopupSlot(dialogPresenter);
		}

	}

	@Override
	public void delete() {
		if (Window.confirm("Вы уверены, что хотите удалить декларацию?")) {
			LogCleanEvent.fire(this);
			DeleteDeclarationDataAction action = new DeleteDeclarationDataAction();
			action.setDeclarationId(declarationId);
			dispatcher
					.execute(
							action,
							CallbackUtils
									.defaultCallback(new AbstractCallback<DeleteDeclarationDataResult>() {
										@Override
										public void onSuccess(
												DeleteDeclarationDataResult result) {
											MessageEvent
													.fire(DeclarationDataPresenter.this,
															"Декларация удалена");
											placeManager
													.revealPlace(new PlaceRequest(
															DeclarationListNameTokens.DECLARATION_LIST)
															.with("nType",
																	taxName));
										}
									}, DeclarationDataPresenter.this));
		}
	}

	@Override
	public void check() {
		LogCleanEvent.fire(this);
		CheckDeclarationDataAction checkAction = new CheckDeclarationDataAction();
		checkAction.setDeclarationId(declarationId);
		dispatcher.execute(checkAction, CallbackUtils
				.defaultCallback(new AbstractCallback<CheckDeclarationDataResult>() {
					@Override
					public void onSuccess(CheckDeclarationDataResult result) {
                        LogAddEvent.fire(DeclarationDataPresenter.this, result.getLogEntries());
					}
				}, this));
	}

	@Override
	public void downloadExcel() {
		Window.open(GWT.getHostPageBaseURL() + "download/declarationData/xlsx/"
				+ declarationId, null, null);
	}

	@Override
	public void downloadXml() {
		Window.open(GWT.getHostPageBaseURL() + "download/declarationData/xml/"
				+ declarationId, null, null);
	}

	@Override
	public void onInfoClicked() {
		historyPresenter.prepareDeclarationHistory(declarationId);
		addToPopupSlot(historyPresenter);
	}

	private void revealPlaceRequest() {
		placeManager.revealPlace(new PlaceRequest(
				DeclarationDataTokens.declarationData).with(
				DeclarationDataTokens.declarationId,
				String.valueOf(declarationId)));
	}

	private void updateTitle(String declarationType) {
		TitleUpdateEvent.fire(this, "Декларация", declarationType);
	}
}
