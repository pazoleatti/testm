package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FormDataPresenter extends
		FormDataPresenterBase<FormDataPresenter.MyProxy> implements
		FormDataUiHandlers {

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenterBase}
	 * 's proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NAME_TOKEN)
	public interface MyProxy extends ProxyPlace<FormDataPresenter>, Place {
	}

	@Inject
	public FormDataPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher,
			SignersPresenter signersPresenter, DialogPresenter dialogPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, signersPresenter, dialogPresenter);
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			super.prepareFromRequest(request);

			final GetFormData action = new GetFormData();
			// WTF? Почему Long.MAX_VALUE?
			action.setFormDataId(Long.parseLong(request.getParameter(
					FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))));
			action.setDepartmentId(ParamUtils
					.getInteger(request, DEPARTMENT_ID));
			action.setFormDataKind(ParamUtils.getLong(request,
					FORM_DATA_KIND_ID));
			action.setFormDataTypeId(ParamUtils.getLong(request,
					FORM_DATA_TYPE_ID));
			action.setReadOnly(Boolean.parseBoolean(request.getParameter(
					READ_ONLY, "true")));

			dispatcher.execute(
					action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<GetFormDataResult>() {
								@Override
								public void onSuccess(GetFormDataResult result) {

									LogAddEvent.fire(FormDataPresenter.this,
											result.getLogEntries());

									formData = result.getFormData();
									formDataAccessParams = result
											.getFormDataAccessParams();
									fixedRows = result.isFixedRows();

									switch (result.getFormMode()) {
									case READ_UNLOCKED:
										setReadUnlockedMode();
										break;
									case READ_LOCKED:
										setReadLockedMode(
												result.getLockedByUser(),
												result.getLockDate());
										break;
									case EDIT:
										setEditMode();
										break;
									}

									manageDeleteRowButtonEnabled();

									getView().setAdditionalFormInfo(
											result.getFormData().getFormType()
													.getName(),
											result.getFormData().getFormType()
													.getTaxType(),
											result.getFormData().getKind()
													.getName(),
											result.getDepartmenName(),
											result.getReportPeriod(),
											result.getFormData().getState()
													.getName());

									getView().setBackButton("#" + FormDataListNameTokens.FORM_DATA_LIST + ";nType="
											+ String.valueOf(result.getFormData().getFormType().getTaxType()));

									getView().setColumnsData(
											formData.getFormColumns(),
											readOnlyMode);
									getView().setRowsData(
											formData.getDataRows());
									getView().addCustomHeader(
											result.isNumberedHeader());
									getView().addCustomTableStyles(
											result.getAllStyles());

									TitleUpdateEvent
											.fire(FormDataPresenter.this,
													readOnlyMode ? "Налоговая форма"
															: "Редактирование налоговой формы",
													formData.getFormType()
															.getName());

								}

							}).addCallback(
							new ManualRevealCallback<GetFormDataResult>(this)));

		} catch (Exception e) {
			getProxy().manualRevealFailed();
			MessageEvent.fire(this,
					"Не удалось открыть/создать налоговую форму", e);
		}
	}

	@Override
	public void onSelectRow() {
		manageDeleteRowButtonEnabled();
	}

	@Override
	public void onShowCheckedColumns() {
		getView().setColumnsData(formData.getFormColumns(), readOnlyMode);
	}

	private void manageDeleteRowButtonEnabled() {
		if (!readOnlyMode) {
			MyView view = getView();
			DataRow dataRow = view.getSelectedRow();
			view.enableRemoveRowButton(dataRow != null);
		}
	}

	@Override
	public void onManualInputClicked(boolean readOnlyMode) {
		revealForm(readOnlyMode);
	}

	@Override
	public void onOriginalVersionClicked() {
		Window.alert("В разработке");
	}

	@Override
	public void onPrintClicked() {
		Window.open(
				GWT.getHostPageBaseURL() + "download/downloadController/"
						+ formData.getId() + "/"
						+ getView().getCheckedColumnsClicked(), "", "");
	}

	@Override
	public void onSignersClicked() {
		signersPresenter.setFormData(formData);
		signersPresenter.setReadOnlyMode(readOnlyMode);
		addToPopupSlot(signersPresenter);
	}

	@Override
	public void onReturnClicked() {
		if (readOnlyMode ||
				Window.confirm("Вы уверены, что хотите прекратить редактирование данных" +
						" и перейти к списку налоговых форм?")) {
			unlockForm(formData.getId());
			goToFormDataList();
		}
	}

	@Override
	public void onCancelClicked() {
		if (Window.confirm("Вы уверены, что хотите прекратить редактирование данных налоговой формы?")){
			if(formData.getId() == null){
				goToFormDataList();
			} else {
				unlockForm(formData.getId());
				revealForm(true);
			}
		}
	}

	@Override
	public void onSaveClicked() {
		LogCleanEvent.fire(this);
		SaveFormDataAction action = new SaveFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<FormDataResult>() {
					@Override
					public void onSuccess(FormDataResult result) {
						processFormDataResult(result);
					}

				}));
	}

	private void processFormDataResult(FormDataResult result) {
		formData = result.getFormData();
		LogAddEvent.fire(this, result.getLogEntries());
		getView().setRowsData(formData.getDataRows());
	}

	@Override
	public void onAddRowClicked() {
		LogCleanEvent.fire(this);
		DataRow dataRow = getView().getSelectedRow();
		AddRowAction action = new AddRowAction();
		action.setCurrentDataRow(dataRow);
		action.setFormData(formData);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<FormDataResult>() {
					@Override
					public void onSuccess(FormDataResult result) {
						processFormDataResult(result);
					}

				}));
	}

	@Override
	public void onRemoveRowClicked() {
		LogCleanEvent.fire(this);
		DataRow dataRow = getView().getSelectedRow();
		DeleteRowAction action = new DeleteRowAction();
		action.setCurrentDataRow(dataRow);
		action.setFormData(formData);
		if (dataRow != null) {
			dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<FormDataResult>() {
						@Override
						public void onSuccess(FormDataResult result) {
							processFormDataResult(result);
						}

					}));
			getView().setRowsData(formData.getDataRows());
		}
	}

	@Override
	public void onRecalculateClicked() {
		LogCleanEvent.fire(this);
		RecalculateFormDataAction action = new RecalculateFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<FormDataResult>() {
					@Override
					public void onSuccess(FormDataResult result) {
						processFormDataResult(result);
					}

				}));
	}

	@Override
	public void onCheckClicked() {
		LogCleanEvent.fire(this);
		CheckFormDataAction checkAction = new CheckFormDataAction();
		checkAction.setFormData(formData);
		dispatcher.execute(checkAction, CallbackUtils
				.defaultCallback(new AbstractCallback<FormDataResult>() {
					@Override
					public void onSuccess(FormDataResult result) {
						LogAddEvent.fire(FormDataPresenter.this,
								result.getLogEntries());
					}
				}));
	}

	@Override
	public void onDeleteFormClicked() {
		boolean isOK = Window.confirm("Удалить?");
		if (isOK) {
			DeleteFormDataAction action = new DeleteFormDataAction();
			action.setFormDataId(formData.getId());
			dispatcher
					.execute(
							action,
							CallbackUtils
									.defaultCallback(new AbstractCallback<DeleteFormDataResult>() {
										@Override
										public void onSuccess(
												DeleteFormDataResult result) {
											goToFormDataList();
										}

									}));
		}
	}

	@Override
	public void onWorkflowMove(WorkflowMove wfMove) {
		dialogPresenter.setFormData(formData);
		dialogPresenter.setWorkFlow(wfMove);
		addToPopupSlot(dialogPresenter);
		//revealForm(true, wfMove.getId());
	}

	private void goToFormDataList() {
		placeManager.revealPlace(new PlaceRequest(
				FormDataListNameTokens.FORM_DATA_LIST).with("nType",
				String.valueOf(formData.getFormType().getTaxType())));
	}

}
