package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.ArrayList;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateDataRowsAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RollbackDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RollbackDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UploadDataRowsAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FormDataPresenter extends
        FormDataPresenterBase<FormDataPresenter.MyProxy> implements
        FormDataUiHandlers{

	public static final int PAGE_SIZE = 15;

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
			SignersPresenter signersPresenter, DialogPresenter dialogPresenter, HistoryPresenter historyPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, signersPresenter, dialogPresenter, historyPresenter);
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		GetFormData action = new GetFormData();
		action.setFormDataId(Long.parseLong(request.getParameter(FORM_DATA_ID, null)));
		action.setReadOnly(Boolean.parseBoolean(request.getParameter(READ_ONLY, "true")));
        executeAction(action);
	}

	@Override
	public void onRangeChange(final int start, int length) {
		if (formData != null) {
			GetRowsDataAction action = new GetRowsDataAction();
			action.setFormDataId(formData.getId());
			action.setRange(new DataRowRange(start+1, length));
			action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
			action.setReadOnly(readOnlyMode);
			action.setFormDataTemplateId(formData.getFormTemplateId());
			dispatcher.execute(action, CallbackUtils
					.wrongStateCallback(new AbstractCallback<GetRowsDataResult>() {
						@Override
						public void onSuccess(GetRowsDataResult result) {
							if(result==null || result.getDataRows().getTotalRecordCount() == 0)
								getView().setRowsData(start, 0, new ArrayList<DataRow<Cell>>());
							else {
								getView().setRowsData(start, (int) result.getDataRows().getTotalRecordCount(), result.getDataRows());
								if (result.getDataRows().size() > PAGE_SIZE) {
									getView().assignDataProvider(result.getDataRows().size());
								}
							}
							modifiedRows.clear();
						}
					}, FormDataPresenter.this));
		}
	}

	@Override
	public void onCellModified(DataRow<Cell> dataRow) {
		modifiedRows.add(dataRow);

	}

    @Override
    public void onUploadDataRow(String uuid) {
        UploadDataRowsAction action = new UploadDataRowsAction();
        action.setUuid(uuid);
        action.setFormData(formData);
        dispatcher.execute(action, createDataRowResultCallback(true));
    }

    @Override
	public void onSelectRow() {
		manageDeleteRowButtonEnabled();
	}

	@Override
	public void onShowCheckedColumns() {
		getView().setColumnsData(formData.getFormColumns(), readOnlyMode, forceEditMode);
	}

    private void manageDeleteRowButtonEnabled() {
		if (!readOnlyMode) {
			MyView view = getView();
			DataRow<Cell> dataRow = view.getSelectedRow();
			view.enableRemoveRowButton(dataRow != null);
		}
	}

	@Override
	public void onManualInputClicked(boolean readOnlyMode) {
		revealFormData(readOnlyMode);
	}

	@Override
	public void onInfoClicked() {
		historyPresenter.prepareFormHistory(formData.getId());
		addToPopupSlot(historyPresenter);
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
		revealFormDataList();
	}

	@Override
	public void onCancelClicked() {
			RollbackDataAction action = new RollbackDataAction();
			action.setFormDataId(formData.getId());
			dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<RollbackDataResult>() {
						@Override
						public void onSuccess(RollbackDataResult result) {
							revealFormData(true);
						}
						
						public void onFailure(Throwable caught) {
							modifiedRows.clear();
							getView().updateData();
						}
						
				}, this));
		}


	
	private AsyncCallback<DataRowResult> createDataRowResultCallback(final boolean showMsg){ 
			LogCleanEvent.fire(this);
			AbstractCallback<DataRowResult> callback = new AbstractCallback<DataRowResult>() {
				@Override
				public void onSuccess(DataRowResult result) {
					modifiedRows.clear();
					getView().updateData();
					LogAddEvent.fire(FormDataPresenter.this, result.getLogEntries());
					getView().setSelectedRow(result.getCurrentRow(), true);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					modifiedRows.clear();
					getView().updateData();
				}

			};
			return showMsg ? CallbackUtils.defaultCallback(callback, this) : 
				CallbackUtils.defaultCallbackNoModalError(callback, this);
	}
	
	@Override
	public void onCheckClicked() {
		LogCleanEvent.fire(this);
		CheckFormDataAction checkAction = new CheckFormDataAction();
		checkAction.setFormData(formData);
		checkAction.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		dispatcher.execute(checkAction, createDataRowResultCallback(false));
	}
	
	
	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onSaveClicked()
	 */
	@Override
	public void onSaveClicked() {
		SaveFormDataAction action = new SaveFormDataAction();
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		dispatcher.execute(action, createDataRowResultCallback(true));
	}
	

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onAddRowClicked()
	 */
	@Override
	public void onAddRowClicked() {
		DataRow<Cell> dataRow = getView().getSelectedRow();
		AddRowAction action = new AddRowAction();
		action.setCurrentDataRow(dataRow);
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		dispatcher.execute(action, createDataRowResultCallback(true));
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onRemoveRowClicked()
	 */
	@Override
	public void onRemoveRowClicked() {
		DataRow<Cell> dataRow = getView().getSelectedRow();
		DeleteRowAction action = new DeleteRowAction();
		action.setCurrentDataRow(dataRow);
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		if (dataRow != null) {
			dispatcher.execute(action, createDataRowResultCallback(true));
		}
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onRecalculateClicked()
	 */
	@Override
	public void onRecalculateClicked() {
		RecalculateDataRowsAction action = new RecalculateDataRowsAction();
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		dispatcher.execute(action, createDataRowResultCallback(true));
	}



	@Override
	public void onDeleteFormClicked() {
		boolean isOK = Window.confirm("Вы уверены, что хотите удалить налоговую форму?");
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
											revealFormDataList();
										}

									}, this));
		}
	}

	@Override
	public void onWorkflowMove(WorkflowMove wfMove) {
		if (wfMove.isReasonToMoveShouldBeSpecified()){
			dialogPresenter.setFormData(formData);
			dialogPresenter.setWorkFlow(wfMove);
			addToPopupSlot(dialogPresenter);
		} else {
			goMove(wfMove);
		}
	}



	private void goMove(final WorkflowMove wfMove){
		GoMoveAction action = new GoMoveAction();
		action.setFormDataId(formData.getId());
		action.setMove(wfMove);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GoMoveResult>() {
					@Override
					public void onSuccess(GoMoveResult result) {
						revealFormData(true);
					}
				}, this));
	}

    private void executeAction(GetFormData action){
        dispatcher.execute(
                action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetFormDataResult>() {
                            @Override
                            public void onSuccess(GetFormDataResult result) {

                                LogAddEvent.fire(FormDataPresenter.this,
                                        result.getLogEntries());
                                
                    			// Очищаем возможные изменения на форме перед открытием.
                    			modifiedRows.clear();
                    			
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
                                        result.getTemplateFormName(),
                                        result.getFormData().getFormType()
                                                .getTaxType(),
                                        result.getFormData().getKind()
                                                .getName(),
                                        result.getDepartmenName(),
                                        buildPeriodName(result),
                                        result.getFormData().getState()
                                                .getName(),
		                                result.getTaxPeriodStartDate(), result.getTaxPeriodEndDate());
                                // Если период для ввода остатков, то делаем все ячейки редактируемыми
                                
                                if (!readOnlyMode && result.isBalancePeriod()) {
                                    forceEditMode = true;
                                }
                                
                                getView().setBackButton("#" + FormDataListNameTokens.FORM_DATA_LIST + ";nType="
                                        + String.valueOf(result.getFormData().getFormType().getTaxType()));
                                getView().setColumnsData(
                                        formData.getFormColumns(),
                                        readOnlyMode,
                                        forceEditMode);
                                getView().addCustomHeader(
                                        formData.getHeaders());
                                getView().addCustomTableStyles(
                                        result.getAllStyles());

                                TitleUpdateEvent
                                        .fire(FormDataPresenter.this,
                                                readOnlyMode ? "Налоговая форма"
                                                        : "Редактирование налоговой формы",
                                                formData.getFormType()
                                                        .getName());
	                            getView().updateData(0);

                            }

                        }, this).addCallback(
                        		TaManualRevealCallback.create(this, placeManager)));
    }

    private String buildPeriodName(GetFormDataResult retFormDataResult) {
        StringBuilder builder = new StringBuilder();
        builder.append(retFormDataResult.getReportPeriodYear()).append(", ");
        builder.append(retFormDataResult.getReportPeriod().getName());
        Integer periodOrder = retFormDataResult.getFormData().getPeriodOrder();
        if (periodOrder != null) {
            builder.append(", ").append(Formats.getRussianMonthNameWithTier(retFormDataResult.getFormData().getPeriodOrder()));
        }
        return builder.toString();
    }

}
