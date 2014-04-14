package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.event.SetFocus;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.search.FormSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateManualFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateManualFormDataResult;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.ArrayList;
import java.util.List;

public class FormDataPresenter extends FormDataPresenterBase<FormDataPresenter.MyProxy> implements
        FormDataUiHandlers, SetFocus.SetFocusHandler {

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
			SignersPresenter signersPresenter, DialogPresenter dialogPresenter, HistoryPresenter historyPresenter, FormSearchPresenter searchPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, signersPresenter, dialogPresenter, historyPresenter, searchPresenter);
		getView().setUiHandlers(this);
		getView().assignDataProvider(getView().getPageSize());
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		GetFormData action = new GetFormData();
		if ( formData!=null ){
			action.setOldFormDataId(formData.getId());
		}
		action.setFormDataId(Long.parseLong(request.getParameter(FORM_DATA_ID, null)));
		action.setReadOnly(Boolean.parseBoolean(request.getParameter(READ_ONLY, "true")));
        action.setManual(request.getParameter(MANUAL, null) != null ? Boolean.parseBoolean(request.getParameter(MANUAL, null)) : null);
        action.setUuid(request.getParameter(UUID, null));
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
            action.setManual(formData.isManual());
			action.setFormDataTemplateId(formData.getFormTemplateId());
			dispatcher.execute(action, CallbackUtils
					.wrongStateCallback(new AbstractCallback<GetRowsDataResult>() {
						@Override
						public void onSuccess(GetRowsDataResult result) {
                            if (result == null || result.getDataRows().getTotalCount() == 0) {
                                getView().setRowsData(start, 0, new ArrayList<DataRow<Cell>>());
                            } else {
                                getView().setRowsData(start, result.getDataRows().getTotalCount(), result.getDataRows());
                                if (result.getDataRows().size() > getView().getPageSize()) {
                                    getView().assignDataProvider(result.getDataRows().size());
                                } else {
                                    getView().assignDataProvider(getView().getPageSize());
                                }
                                getView().isCanEditPage(!fixedRows);
                                //getView().setPagingVisible(result.getDataRows().getTotalCount() > getView().getPageSize());
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
    public void onStartLoad() {
        LockInteractionEvent.fire(this, true);
    }

    @Override
    public void onEndLoad() {
        LockInteractionEvent.fire(this, false);
    }

    @Override
    public void onCreateManualClicked() {
        //Сводная форма банка
        if (!existManual) {
            Dialog.confirmMessage("Подтверждение", "Создать для налоговой формы версию ручного ввода?", new DialogHandler() {
                @Override
                public void yes() {
                    LogCleanEvent.fire(FormDataPresenter.this);
                    LogShowEvent.fire(FormDataPresenter.this, false);
                    CreateManualFormData action = new CreateManualFormData();
                    action.setFormDataId(formData.getId());
                    dispatcher.execute(action, CallbackUtils
                            .defaultCallback(new AbstractCallback<CreateManualFormDataResult>() {
                                @Override
                                public void onSuccess(CreateManualFormDataResult result) {
                                    revealFormData(readOnlyMode, true, null);
                                }
                            }, FormDataPresenter.this)
                    );
                    Dialog.hideMessage();
                }

                @Override
                public void no() {
                    Dialog.hideMessage();
                }

                @Override
                public void close() {
                    Dialog.hideMessage();
                }
            });
        }
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

	private void manageButtonsForFormInClosedPeriod(boolean isPeriodClosed) {
		if (isPeriodClosed) {
			getView().showSignersAnchor(false);
			getView().showRecalculateButton(false);
			getView().showOriginalVersionButton(false);

            getView().showEditAnchor(false);
            getView().showModeAnchor(false, false);
            getView().showManualAnchor(false);
		}
	}

    @Override
    public void onModeChangeClicked() {
        revealFormData(readOnlyMode, !formData.isManual(), null);
    }

	@Override
	public void onEditClicked(final boolean readOnlyMode) {
        if (formData.isManual()) {
            CheckManualAction action = new CheckManualAction();
            action.setFormDataId(formData.getId());
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CheckManualResult>() {
                @Override
                public void onSuccess(CheckManualResult result) {
                    revealFormData(readOnlyMode, formData.isManual(), null);
                }
            }, this));
        } else {
            revealFormData(readOnlyMode, formData.isManual(), null);
        }
	}

	@Override
	public void onInfoClicked() {
		historyPresenter.prepareFormHistory(formData.getId(), getView().getTaxType());
		addToPopupSlot(historyPresenter);
	}

	@Override
	public void onOriginalVersionClicked() {
        Dialog.warningMessage("В разработке");
	}

	@Override
	public void onPrintClicked() {
		Window.open(
                GWT.getHostPageBaseURL() + "download/downloadController/"
                        + formData.getId() + "/"
                        + getView().getCheckedColumnsClicked() + "/"
                        + formData.isManual(), "", "");
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
		revealFormData(true, formData.isManual(), null);
	}
	
	private AsyncCallback<DataRowResult> createDataRowResultCallback(final boolean showMsg){ 
			LogCleanEvent.fire(this);
			AbstractCallback<DataRowResult> callback = new AbstractCallback<DataRowResult>() {
				@Override
				public void onSuccess(DataRowResult result) {
					modifiedRows.clear();
					getView().updateData();
					LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
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
        Dialog.confirmMessage("Подтверждение", "Вы уверены, что хотите удалить налоговую форму?",new DialogHandler() {
            @Override
            public void yes() {
                DeleteFormDataAction action = new DeleteFormDataAction();
                action.setFormDataId(formData.getId());
                action.setManual(false);
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

                                        }, FormDataPresenter.this));
                Dialog.hideMessage();
            }

            @Override
            public void no() {
                Dialog.hideMessage();
            }

            @Override
            public void close() {
                Dialog.hideMessage();
            }
        });
	}

    @Override
    public void onDeleteManualClicked() {
        Dialog.confirmMessage("Удалить версию ручного ввода и перейти к автоматически сформированной версии?",new DialogHandler() {
            @Override
            public void yes() {
                LogCleanEvent.fire(FormDataPresenter.this);
                LogShowEvent.fire(FormDataPresenter.this, false);
                DeleteFormDataAction action = new DeleteFormDataAction();
                action.setFormDataId(formData.getId());
                action.setManual(true);
                dispatcher
                        .execute(
                                action,
                                CallbackUtils
                                        .defaultCallback(new AbstractCallback<DeleteFormDataResult>() {
                                            @Override
                                            public void onSuccess(
                                                    DeleteFormDataResult result) {
                                                revealFormData(true, false, null);
                                            }

                                        }, FormDataPresenter.this));
                Dialog.hideMessage();
            }

            @Override
            public void no() {
                Dialog.hideMessage();
            }

            @Override
            public void close() {
                Dialog.hideMessage();
            }
        });
    }

	@Override
	public void onWorkflowMove(final WorkflowMove wfMove) {
        if (formData.isManual() && wfMove.getFromState().equals(WorkflowState.ACCEPTED)) {
            Dialog.confirmMessage("Подтверждение", "Удалить версию ручного ввода и выполнить переход в статус \""+wfMove.getToState().getName()+"\"?", new DialogHandler() {
                @Override
                public void yes() {
                    LogCleanEvent.fire(FormDataPresenter.this);
                    LogShowEvent.fire(FormDataPresenter.this, false);
                    DeleteFormDataAction action = new DeleteFormDataAction();
                    action.setFormDataId(formData.getId());
                    action.setManual(true);
                    dispatcher.execute(action, CallbackUtils
                            .defaultCallback(new AbstractCallback<DeleteFormDataResult>() {
                                @Override
                                public void onSuccess(
                                        DeleteFormDataResult result) {
                                    formData.setManual(false);
                                    commonMoveLogic(wfMove);
                                }
                            }, FormDataPresenter.this));
                    Dialog.hideMessage();
                }

                @Override
                public void no() {
                    Dialog.hideMessage();
                }

                @Override
                public void close() {
                    Dialog.hideMessage();
                }
            });
        } else {
            commonMoveLogic(wfMove);
        }
	}

    private void commonMoveLogic(final WorkflowMove wfMove) {
        if (wfMove.isReasonToMoveShouldBeSpecified()){
            DestinationCheckAction action = new DestinationCheckAction();
            action.setFormDataId(formData.getId());
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DestinationCheckResult>() {
                @Override
                public void onSuccess(DestinationCheckResult result) {
                    dialogPresenter.setFormData(formData);
                    dialogPresenter.setWorkFlow(wfMove);
                    addToPopupSlot(dialogPresenter);
                }
            }, this));
        } else {
            goMove(wfMove);
        }
    }

	private void goMove(final WorkflowMove wfMove){
		LogCleanEvent.fire(this);
		GoMoveAction action = new GoMoveAction();
		action.setFormDataId(formData.getId());
		action.setMove(wfMove);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GoMoveResult>() {
					@Override
					public void onSuccess(GoMoveResult result) {
                        LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                        revealFormData(true, formData.isManual(), result.getUuid());
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

                                LogAddEvent.fire(FormDataPresenter.this, result.getUuid());

                    			// Очищаем возможные изменения на форме перед открытием.
                    			modifiedRows.clear();

                    			formData = result.getFormData();
                                existManual = result.existManual();
                                canCreatedManual = result.canCreatedManual();
                                isBankSummaryForm = result.isBankSummaryForm();
                                formSearchPresenter.setFormDataId(formData.getId());

                                /**
                                 * Передаем призентору поиска по форме, данные о скрытых колонках
                                 */
                                List<Integer> hiddenColumns = new ArrayList<Integer>();
                                for(Column c :formData.getFormColumns()){
                                    if (c.getWidth() == 0){
                                        hiddenColumns.add(c.getOrder());
                                    }
                                }
                                formSearchPresenter.setHiddenColumns(hiddenColumns);
                    			
                    			// Регистрируем хендлер на закрытие
                    			if (closeFormDataHandlerRegistration !=null ){
                    				closeFormDataHandlerRegistration.removeHandler();
                    			}
                                
                                formDataAccessParams = result
                                        .getFormDataAccessParams();
                                fixedRows = result.isFixedRows();
                                getView().setupSelectionModel(fixedRows);

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

	                            manageButtonsForFormInClosedPeriod(result.isFormInClosedPeriod());

                                manageDeleteRowButtonEnabled();

                                getView().setAdditionalFormInfo(
                                        result.getTemplateFormName(),
                                        result.getFormData().getFormType()
                                                .getTaxType(),
                                        result.getFormData().getKind()
                                                .getName(),
                                        result.getDepartmenFullName(),
                                        buildPeriodName(result),
                                        result.getFormData().getState()
                                                .getName(),
		                                result.getReportPeriodStartDate(), result.getReportPeriodEndDate(), formData.getId());

                                getView().setBackButton("#" + FormDataListNameTokens.FORM_DATA_LIST + ";nType="
                                        + result.getFormData().getFormType().getTaxType());
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

                                getView().updatePageSize(result.getFormData().getFormType().getTaxType());
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

    @Override
    protected void onBind() {
        super.onBind();
        final ValueChangeHandler<String> fileUploadValueChangeHandler = new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String uuid = event.getValue();
                UploadDataRowsAction action = new UploadDataRowsAction();
                action.setUuid(uuid);
                action.setFormData(formData);
                dispatcher.execute(action, createDataRowResultCallback(true));
            }
        };
        getView().addFileUploadValueChangeHandler(fileUploadValueChangeHandler);
        addRegisteredHandler(SetFocus.getType(), this);
    }

    @Override
    public void onOpenSearchDialog() {
        formSearchPresenter.open();
        addToPopupSlot(formSearchPresenter);
    }

    @Override
    public void onSetFocus(SetFocus event) {
        getView().setFocus(event.getRowIndex());
    }

    @Override
    protected void onHide() {
        removeFromPopupSlot(formSearchPresenter);
        formSearchPresenter.close();
    }
}
