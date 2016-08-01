package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.comments.FilesCommentsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.event.SetFocus;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.search.FormSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateManualFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateManualFormDataResult;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.CheckHandler;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.client.LogAreaPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.ManualMenuPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.*;

public class FormDataPresenter extends FormDataPresenterBase<FormDataPresenter.MyProxy> implements
        FormDataUiHandlers, SetFocus.SetFocusHandler {

    private static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");
    private static final String NOT_EXIST_EVENT_MSG = "Данное действие на форме недоступно";

    private final ManualMenuPresenter manualMenuPresenter;
    private final LogAreaPresenter logAreaPresenter;
    private ReportType timerType;
    private Map<String, TimerReportResult.StatusReport> reportTimerStatus;
    private List<FormDataReportType> reportTypes = new ArrayList<FormDataReportType>();

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
			SignersPresenter signersPresenter, DialogPresenter dialogPresenter, HistoryPresenter historyPresenter, FormSearchPresenter searchPresenter,
            SourcesPresenter sourcesPresenter, FilesCommentsPresenter filesCommentsPresenter, ManualMenuPresenter manualMenuPresenter, LogAreaPresenter logAreaPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, signersPresenter, dialogPresenter, historyPresenter, searchPresenter, sourcesPresenter, filesCommentsPresenter);
		this.manualMenuPresenter = manualMenuPresenter;
        this.logAreaPresenter = logAreaPresenter;
        getView().setUiHandlers(this);
		getView().assignDataProvider(getView().getPageSize());
        reportTimerStatus = new HashMap<String, TimerReportResult.StatusReport>();
        timer = new Timer() {
            @Override
            public void run() {
                updateFormMode(true);
                updateReportStatus(true);
            }
        };
        stopTimer();
        formMode = null;
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
        modifiedRows.clear();
        super.prepareFromRequest(request);
        LogCleanEvent.fire(FormDataPresenter.this);
		GetFormDataAction action = new GetFormDataAction();
        Long fdId = Long.parseLong(request.getParameter(FORM_DATA_ID, null));
		if ( formData!=null && !readOnlyMode ){
            action.setOldFormDataId(formData.getId());
		}
		action.setFormDataId(fdId);
		action.setReadOnly(Boolean.parseBoolean(request.getParameter(READ_ONLY, "true")));
        action.setManual(Boolean.parseBoolean(request.getParameter(MANUAL, "false")));
        action.setUuid(request.getParameter(UUID, null));
        action.setCorrectionDiff(Boolean.parseBoolean(request.getParameter(CORRECTION, "false")));
        free = Boolean.parseBoolean(request.getParameter(FREE, "false"));
        formMode = null;
        getFormData(action);
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
            action.setCorrectionDiff(!absoluteView);
            action.setFree(free);
            action.setInnerLogUuid(logAreaPresenter.getUuid());
            dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<GetRowsDataResult>() {
						@Override
						public void onSuccess(GetRowsDataResult result) {
                            if (result != null) {
                                LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                            }
                            if (result == null || result.getDataRows().getTotalCount() == 0) {
                                getView().setRowsData(start, 0, new ArrayList<DataRow<Cell>>());
                            } else {
                                if (formData.isManual() && !readOnlyMode) {
                                    /** Устанавливаем возможность редактирования и стили для ручного ввода */
                                    Set<String> aliases = result.getDataRows().get(0).keySet();
                                    for (DataRow<Cell> row : result.getDataRows()) {
                                        for (String alias : aliases) {
                                            Cell cell = row.getCell(alias);
                                            if (!(ColumnType.REFERENCE.equals(cell.getColumn().getColumnType()))) {
                                                cell.setEditable(true);
                                                if (cell.getStyle() == null) {
                                                    cell.setClientStyle("manual_editable_cell", Color.BLACK, Color.LIGHT_BLUE);
                                                }
                                            }  else {
                                                if (cell.getStyle() == null) {
                                                    cell.setClientStyle("manual_non_editable_cell", Color.BLACK, Color.WHITE);
                                                }
                                            }
                                        }
                                    }
                                }
                                getView().setRowsData(start, result.getDataRows().getTotalCount(), result.getDataRows());
                                if (result.getDataRows().size() > getView().getPageSize()) {
                                    getView().assignDataProvider(result.getDataRows().size());
                                } else {
                                    getView().assignDataProvider(getView().getPageSize());
                                }
                                getView().isCanEditPage(!fixedRows);
                            }
                            if (!modifiedRows.isEmpty()) {
                                edited = true;
                                modifiedRows.clear();
                            }
                        }
					}, FormDataPresenter.this));
		}
	}

	@Override
	public void onCellModified(DataRow<Cell> dataRow) {
        modifiedRows.add(dataRow);
        setOnLeaveConfirmation();
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
                                    formData.setManual(true);
                                    revealFormData(false, true, !absoluteView, result.getUuid());
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
        updateReportStatus(false);
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
			getView().showRecalculateButton(false);
			getView().showOriginalVersionButton(false);

            getView().showEditAnchor(false);
            getView().showModeAnchor(false, false);
            getView().showManualAnchor(false);
		}
	}

    @Override
    public void onModeChangeClicked() {
        revealFormData(readOnlyMode, !formData.isManual(), !absoluteView, null);
    }

	@Override
	public void onEditClicked(final boolean readOnlyMode, final boolean force) {
		FormDataEditAction action = new FormDataEditAction();
		action.setFormData(formData);
        action.setForce(force);
		dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<FormDataEditResult>() {
                @Override
			public void onSuccess(FormDataEditResult result) {
                    if (result.isLock()) {
                        LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                        Dialog.confirmMessage(result.getLockMsg(), new DialogHandler() {
                            @Override
                            public void yes() {
                                onEditClicked(readOnlyMode, true);
                            }
                        });
                    } else {
                        modifiedRows.clear();
                        revealFormData(readOnlyMode, formData.isManual(), readOnlyMode && !absoluteView, null);
                    }
                }
            }, this));
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

    private void updateReportStatus(final boolean isTimer) {
        TimerReportAction action = new TimerReportAction();
        action.setFormDataId(formData.getId());
        action.setShowChecked(getView().getCheckedColumnsClicked());
        action.setManual(formData.isManual());
        action.setSaved(absoluteView);
        action.setReportTypes(reportTypes);
        dispatcher.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<TimerReportResult>() {
                    @Override
                    public void onSuccess(TimerReportResult result) {
                        for(Map.Entry<String, TimerReportResult.StatusReport> entry: result.getMapExistReport().entrySet()) {
                            if (isTimer && entry.getValue() != null && entry.getValue().equals(reportTimerStatus.get(entry.getKey()))) {
                                continue;
                            }
                            if (entry.getValue().equals(TimerReportResult.StatusReport.EXIST)) {
                                getView().updatePrintReportButtonName(entry.getKey(), true);
                                manualMenuPresenter.updateNotificationCount();
                            } else if (entry.getValue().equals(TimerReportResult.StatusReport.NOT_EXIST)) { // если файл не существует и блокировки нет(т.е. задачу отменили или ошибка при формировании)
                                getView().updatePrintReportButtonName(entry.getKey(), false);
                            } else {
                                getView().updatePrintReportButtonName(entry.getKey(), false);
                            }
                            reportTimerStatus.put(entry.getKey(), entry.getValue());
                        }
                    }
                }));
    }

    @Override
    public void onPrintClicked(final FormDataReportType fdReportType, boolean force) {
        CreateReportAction action = new CreateReportAction();
        action.setFormDataId(formData.getId());
        action.setType(fdReportType);
        action.setShowChecked(getView().getCheckedColumnsClicked());
        action.setManual(formData.isManual());
        action.setSaved(absoluteView);
        action.setForce(force);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateReportResult>() {
                    @Override
                    public void onSuccess(CreateReportResult result) {
                        LogCleanEvent.fire(FormDataPresenter.this);
                        LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                        if (result.isExistReport()) {
                            getView().updatePrintReportButtonName(fdReportType.getReportName(), true);
                            DownloadUtils.openInIframe(
                                    GWT.getHostPageBaseURL() + "download/downloadBlobController/"
                                            + fdReportType.getReportName() + "/"
                                            + formData.getId() + "/"
                                            + getView().getCheckedColumnsClicked() + "/"
                                            + formData.isManual() + "/"
                                            + absoluteView);
                        } else if (result.isLock()) {
                            getView().updatePrintReportButtonName(fdReportType.getReportName(), false);
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    onPrintClicked(fdReportType, true);
                                }
                            });
                        } else {
                            getView().updatePrintReportButtonName(fdReportType.getReportName(), false);
                        }
                    }
                }, this));
    }

	@Override
	public void onSignersClicked() {
		signersPresenter.setFormData(formData);
		addToPopupSlot(signersPresenter);
	}

	@Override
	public void onReturnClicked() {
		revealFormDataList();
	}

    @Override
    public void onCancelClicked() {
        String msg;
        boolean isEdited = edited || !modifiedRows.isEmpty();

        if (formMode.equals(TimerTaskResult.FormMode.LOCKED_EDIT) && !readOnlyMode) {
            if (isEdited) {
                msg = "Сохранить изменения без отмены операции \"" + taskName + "\", выйти из режима редактирования? \n\"Да\" - выйти с сохранением без отмены операции. \"Нет\" - выйти без сохранения с отменой операции.";
            } else {
                msg = "Выйти из режима редактирования без отмены операции \"" + taskName + "\"? \n\"Да\" - выйти без отмены операции. \"Нет\" - выйти с отменой операции.";
            }
        } else if (isEdited) {
            msg = "Сохранить изменения, выйти из режима редактирования? \n\"Да\" - выйти с сохранением. \"Нет\" - выйти без сохранения.";
        } else {
            msg = "Выйти из режима редактирования (несохраненные изменения отсутствуют)?";
            Dialog.confirmMessageYesClose("Подтверждение выхода из режима редактирования", msg, new DialogHandler() {
                @Override
                public void yes() {
                    ExitAndSaveFormDataAction action = new ExitAndSaveFormDataAction();
                    action.setFormData(formData);
                    action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
                    dispatcher.execute(action, CallbackUtils.defaultCallback(new AsyncCallback<DataRowResult>() {
                        @Override
                        public void onSuccess(DataRowResult result) {
                            setOnLeaveConfirmation(null);
                            modifiedRows.clear();
                            revealFormData(true, formData.isManual(), !absoluteView, null);
                        }

                        @Override
                        public void onFailure(Throwable caught) {

                        }
                    }, FormDataPresenter.this));
                }

                @Override
                public void close() {
                    super.close();
                }
            });
            return;
        }

        Dialog.confirmMessage("Подтверждение выхода из режима редактирования", msg, new DialogHandler() {
            @Override
            public void yes() {
                ExitAndSaveFormDataAction action = new ExitAndSaveFormDataAction();
                action.setFormData(formData);
                action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
                dispatcher.execute(action, CallbackUtils.defaultCallback(new AsyncCallback<DataRowResult>() {
                    @Override
                    public void onSuccess(DataRowResult result) {
                        setOnLeaveConfirmation(null);
                        modifiedRows.clear();
                        revealFormData(true, formData.isManual(), !absoluteView, null);
                    }

                    @Override
                    public void onFailure(Throwable caught) {

                    }
                }, FormDataPresenter.this));
            }

            @Override
            public void no() {
                setOnLeaveConfirmation(null);
                modifiedRows.clear();
                revealFormData(true, formData.isManual(), !absoluteView, null);
            }
        });
    }
	
	private AsyncCallback<DataRowResult> createDataRowResultCallback(final boolean showMsg, final boolean isSave){
			LogCleanEvent.fire(this);
			AbstractCallback<DataRowResult> callback = new AbstractCallback<DataRowResult>() {
				@Override
				public void onSuccess(DataRowResult result) {
					modifiedRows.clear();
                    edited = !isSave;
                    setOnLeaveConfirmation();
                    LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
					getView().updateData();
					getView().setSelectedRow(result.getCurrentRow(), true);
				}
				
				@Override
				public void onFailure(Throwable caught) {
                    if (caught instanceof TaActionException) {
                        LogAddEvent.fire(FormDataPresenter.this, ((TaActionException) caught).getUuid());
                    }
                    modifiedRows.clear();
                    getView().updateData();
				}

			};
			return showMsg ? CallbackUtils.defaultCallback(callback, this) :
				CallbackUtils.defaultCallbackNoModalError(callback, this);
	}

	@Override
	public void onCheckClicked(final boolean force) {
        LogCleanEvent.fire(this);
        CheckFormDataAction checkAction = new CheckFormDataAction();
        checkAction.setFormData(formData);
        checkAction.setEditMode(!readOnlyMode);
        checkAction.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
        checkAction.setForce(force);
        dispatcher.execute(checkAction, createDataRowResultCallback(force, false, ReportType.CHECK_FD));
	}  	
	
	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onSaveClicked()
	 */
	@Override
	public void onSaveClicked() {
        SaveFormDataAction action = new SaveFormDataAction();
        action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		dispatcher.execute(action, createDataRowResultCallback(true, true));
	}

    @Override
    public void onExitAndSaveClicked() {

    }

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onAddRowClicked()
	 */
	@Override
	public void onAddRowClicked() {
        if (!eventScriptStatus.get(FormDataEvent.ADD_ROW)) {
            Dialog.infoMessage(NOT_EXIST_EVENT_MSG);
            return;
        }
		DataRow<Cell> dataRow = getView().getSelectedRow();
		AddRowAction action = new AddRowAction();
		action.setCurrentDataRow(dataRow);
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		dispatcher.execute(action, createDataRowResultCallback(true, false));
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onRemoveRowClicked()
	 */
	@Override
	public void onRemoveRowClicked() {
        if (!eventScriptStatus.get(FormDataEvent.DELETE_ROW)) {
            Dialog.infoMessage(NOT_EXIST_EVENT_MSG);
            return;
        }
		DataRow<Cell> dataRow = getView().getSelectedRow();
		DeleteRowAction action = new DeleteRowAction();
		action.setCurrentDataRow(dataRow);
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
		if (dataRow != null) {
			dispatcher.execute(action, createDataRowResultCallback(true, false));
		}
	}

    @Override
    public void onFillPreviousButtonClicked() {
        FillPreviousAction action = new FillPreviousAction();
        action.setFormData(formData);
        dispatcher.execute(action, createDataRowResultCallback(true, false));
    }

    @Override
    public void onRefreshClicked(final boolean force, final boolean cancelTask) {
        if (!eventScriptStatus.get(FormDataEvent.REFRESH)) {
            Dialog.infoMessage(NOT_EXIST_EVENT_MSG);
            return;
        }
        RefreshDataRowsAction action = new RefreshDataRowsAction();
        action.setFormData(formData);
        action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
        action.setForce(force);
        action.setCancelTask(cancelTask);
        dispatcher.execute(action, createDataRowResultCallback(force, cancelTask, ReportType.REFRESH_FD));
    }

    /* (non-Javadoc)
     * @see com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataUiHandlers#onRecalculateClicked()
     */
	@Override
	public void onRecalculateClicked(final boolean force, final boolean cancelTask) {
        if (!eventScriptStatus.get(FormDataEvent.CALCULATE)) {
            Dialog.infoMessage(NOT_EXIST_EVENT_MSG);
            return;
        }
        RecalculateDataRowsAction action = new RecalculateDataRowsAction();
		action.setFormData(formData);
		action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
        action.setForce(force);
        action.setCancelTask(cancelTask);
		dispatcher.execute(action, createDataRowResultCallback(force, cancelTask, ReportType.CALCULATE_FD));
	}

    private AsyncCallback<TaskFormDataResult> createDataRowResultCallback(final boolean force, final boolean cancelTask, final ReportType reportType){
        AbstractCallback<TaskFormDataResult> callback = new AbstractCallback<TaskFormDataResult>() {
            @Override
            public void onSuccess(TaskFormDataResult result) {
                LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                if (result.isLock()) {
                    Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                        @Override
                        public void yes() {
                            if (ReportType.CALCULATE_FD.equals(reportType)) {
                                onRecalculateClicked(true, cancelTask);
                            } else if (ReportType.REFRESH_FD.equals(reportType)) {
                                onRefreshClicked(true, cancelTask);
                            } else if (ReportType.CHECK_FD.equals(reportType)) {
                                onCheckClicked(true);
                            }
                        }
                    });
                } else if (result.isLockTask()) {
                    if (!modifiedRows.isEmpty()) {
                        edited = true;
                        modifiedRows.clear();
                    }
                    Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                        @Override
                        public void yes() {
                            if (ReportType.CALCULATE_FD.equals(reportType)) {
                                onRecalculateClicked(force, true);
                            } else if (ReportType.REFRESH_FD.equals(reportType)) {
                                onRefreshClicked(force, true);
                            } else if (ReportType.CHECK_FD.equals(reportType)) {
                                onCheckClicked(force);
                            }
                        }
                    });
                } else {
                    if (!modifiedRows.isEmpty()) {
                        edited = true;
                        modifiedRows.clear();
                    }
                    timerType = reportType;
                    timer.run();
                    getView().setSelectedRow(null, true);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof TaActionException) {
                    LogAddEvent.fire(FormDataPresenter.this, ((TaActionException) caught).getUuid());
                }
                modifiedRows.clear();
                getView().updateData();
            }

        };
        return CallbackUtils.defaultCallback(callback, this);
    }

	@Override
	public void onDeleteFormClicked() {
        Dialog.confirmMessage("Подтверждение", "Вы уверены, что хотите удалить налоговую форму?",new DialogHandler() {
            @Override
            public void yes() {
                DeleteFormDataAction action = new DeleteFormDataAction();
                action.setFormData(formData);
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
                                                if (result.getUuid() != null){
                                                    LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                                                    Dialog.errorMessage("Ошибка", "Форма не может быть удалена!");
                                                }  else{
                                                    revealFormDataList();
                                                }
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
        Dialog.confirmMessage("Удалить версию ручного ввода и перейти к автоматически сформированной версии?",
                new DialogHandler() {
            @Override
            public void yes() {
                LogCleanEvent.fire(FormDataPresenter.this);
                LogShowEvent.fire(FormDataPresenter.this, false);
                DeleteFormDataAction action = new DeleteFormDataAction();
                action.setFormDataId(formData.getId());
                action.setFormData(formData);
                action.setManual(true);
                dispatcher
                        .execute(
                                action,
                                CallbackUtils
                                        .defaultCallback(new AbstractCallback<DeleteFormDataResult>() {
                                            @Override
                                            public void onSuccess(
                                                    DeleteFormDataResult result) {
                                                setReadUnlockedMode();
                                                revealFormData(true, false, !absoluteView, null);
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
    public void onOpenSourcesDialog() {
        sourcesPresenter.setFormData(formData);
        addToPopupSlot(sourcesPresenter);
    }

    @Override
    public void onFilesCommentsDialog() {
        filesCommentsPresenter.setFormData(formData);
        addToPopupSlot(filesCommentsPresenter);
    }

    @Override
    public void onCorrectionSwitch() {
        revealFormData(readOnlyMode || absoluteView, false, absoluteView, null);
    }

    @Override
    public void onConsolidate(final boolean force, final boolean cancelTask) {
        if (!eventScriptStatus.get(FormDataEvent.COMPOSE)) {
            Dialog.infoMessage(NOT_EXIST_EVENT_MSG);
            return;
        }
        ConsolidateAction action = new ConsolidateAction();
        action.setManual(formData.isManual());
        action.setFormDataId(formData.getId());
        action.setForce(force);
        action.setCancelTask(cancelTask);
        action.setTaxType(formData.getFormType().getTaxType());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<ConsolidateResult>() {
            @Override
            public void onSuccess(ConsolidateResult result) {
                LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                if (result.isLock()) {
                    Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                        @Override
                        public void yes() {
                            onConsolidate(true, cancelTask);
                        }
                    });
                } else if (result.isLockTask()) {
                    modifiedRows.clear();
                    LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                    Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                        @Override
                        public void yes() {
                            onConsolidate(force, true);

                        }
                    });
                } else {
                    timerType = ReportType.CONSOLIDATE_FD;
                    timer.run();
                }
            }
        }, this));
    }

    @Override
	public void onWorkflowMove(final WorkflowMove wfMove) {
        if (!formData.isManual() && wfMove.getFromState().equals(WorkflowState.ACCEPTED)) {
            if (existManual) {
                deleteManualAndGoForm(wfMove);
            } else {
                commonMoveLogic(wfMove);
            }
        } else if (formData.isManual() && wfMove.getFromState().equals(WorkflowState.ACCEPTED)) {
            deleteManualAndGoForm(wfMove);
        } else {
            commonMoveLogic(wfMove);
        }
	}

    private void deleteManualAndGoForm(final WorkflowMove wfMove) {
        Dialog.confirmMessage("Подтверждение", "Удалить версию ручного ввода и выполнить переход в статус \""+wfMove.getToState().getTitle()+"\"?", new DialogHandler() {
            @Override
            public void yes() {
                commonMoveLogic(wfMove);
/*
                LogCleanEvent.fire(FormDataPresenter.this);
                LogShowEvent.fire(FormDataPresenter.this, false);
                DeleteFormDataAction action = new DeleteFormDataAction();
                action.setFormDataId(formData.getId());
                action.setFormData(formData);
                action.setManual(true);
                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<DeleteFormDataResult>() {
                            @Override
                            public void onSuccess(
                                    DeleteFormDataResult result) {
                                formData.setManual(false);
                                commonMoveLogic(wfMove);
                            }
                        }, FormDataPresenter.this));*/
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
            goMove(wfMove, false, false);
        }
    }

	private void goMove(final WorkflowMove wfMove, final boolean force, final boolean cancelTask){
		LogCleanEvent.fire(this);
		GoMoveAction action = new GoMoveAction();
		action.setFormDataId(formData.getId());
		action.setMove(wfMove);
        action.setTaxType(formData.getFormType().getTaxType());
        action.setForce(force);
        action.setCancelTask(cancelTask);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GoMoveResult>() {
					@Override
					public void onSuccess(GoMoveResult result) {
                        LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                        if (result.isLock()) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    goMove(wfMove, true, cancelTask);
                                }
                            });
                        } else if (result.isLockTask()) {
                            Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                                @Override
                                public void yes() {
                                    goMove(wfMove, force, true);
                                }
                            });
                        } else {
                            timerType = ReportType.MOVE_FD;
                            timer.run();
                        }
                    }
                }, this));
	}

    private void getFormData(final GetFormDataAction action){
        stopTimer();
        dispatcher.execute(
                action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetFormDataResult>() {
                            @Override
                            public void onSuccess(GetFormDataResult result) {
                                LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                    			// Очищаем возможные изменения на форме перед открытием.
                    			modifiedRows.clear();
                                edited = false;
                                //setOnLeaveConfirmation();

                    			formData = result.getFormData();
                                existManual = result.existManual();
                                updating = result.isUpdating();
                                canCreatedManual = result.canCreatedManual();
                                isBankSummaryForm = result.isBankSummaryForm();
                                formSearchPresenter.setFormDataId(formData.getId());
                                formSearchPresenter.setFormTemplateId(formData.getFormTemplateId());
                                eventScriptStatus = result.getEventScriptStatus();

                                reportTypes = result.getReportTypes();
                                getView().setReportTypes(result.getReportTypes());
                                if (formData.getComparativePeriodId() != null) {
                                    getView().updateTableTopPosition(FormDataView.DEFAULT_TABLE_TOP_POSITION + 20);
                                    getView().updateRightButtonsHeight(FormDataView.DEFAULT_RIGHT_BUTTONS_HEIGHT + 20);
                                } else {
                                    getView().updateTableTopPosition(FormDataView.DEFAULT_TABLE_TOP_POSITION);
                                    getView().updateRightButtonsHeight(FormDataView.DEFAULT_RIGHT_BUTTONS_HEIGHT);
                                }

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
                    			if (closeFormDataHandlerRegistration != null){
                    				closeFormDataHandlerRegistration.removeHandler();
                    			}

                                formDataAccessParams = result
                                        .getFormDataAccessParams();
                                fixedRows = result.isFixedRows();
                                getView().setupSelectionModel(fixedRows);

                                switch (result.getFormMode()) {
                                    case READ_UNLOCKED:
                                        readOnlyMode = true;
                                        break;
                                    case READ_LOCKED:
                                        readOnlyMode = true;
                                        break;
                                    case EDIT:
                                        readOnlyMode = false;
                                        break;
                                }

	                            manageButtonsForFormInClosedPeriod(!result.getDepartmentReportPeriod().isActive());

                                manageDeleteRowButtonEnabled();

                                absoluteView = !result.isCorrectionDiff();
                                correctionPeriod = result.getDepartmentReportPeriod().getCorrectionDate() != null;
                                DepartmentReportPeriod drp = result.getDepartmentReportPeriod();
                                DepartmentReportPeriod cdrp = result.getComparativePeriod();
                                getView().setAdditionalFormInfo(
                                        result.getTemplateFormName(),
                                        result.getFormData().getFormType().getTaxType(),
                                        result.getFormData().getKind().getTitle(),
                                        result.getDepartmentFullName(),
                                        buildPeriodName(formData.isAccruing() ? drp.getReportPeriod().getAccName() : drp.getReportPeriod().getName(),
                                                drp.getReportPeriod().getTaxPeriod().getYear(), formData.getPeriodOrder(), drp.getCorrectionDate()),
                                        cdrp != null ? buildPeriodName(formData.isAccruing() ? cdrp.getReportPeriod().getAccName() : cdrp.getReportPeriod().getName(),
                                                cdrp.getReportPeriod().getTaxPeriod().getYear(), formData.getPeriodOrder(), cdrp.getCorrectionDate()) : null,
                                        result.getFormData().getState().getTitle(),
		                                result.getDepartmentReportPeriod().getReportPeriod().getCalendarStartDate(),
                                        result.getDepartmentReportPeriod().getReportPeriod().getEndDate(),
                                        formData.getId(), correctionPeriod,
                                        result.isCorrectionDiff(), result.isReadOnly());

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

                                getView().updatePageSize(result.getFormData().getFormType().getTaxType());
                                getView().showConsolidation(
                                        WorkflowState.ACCEPTED != formData.getState()
                                                &&
                                                (FormDataKind.CONSOLIDATED == formData.getKind() || FormDataKind.SUMMARY == formData.getKind() || FormDataKind.CALCULATED == formData.getKind())
                                                &&
                                                readOnlyMode);

                                updateReportStatus(false);
                                updateFormMode(false);
                                startTimer();
                            }
                        }, this).addCallback(
                        TaManualRevealCallback.create(this, placeManager)));
    }

    private String buildPeriodName(String reportPeriodName, int year, Integer periodOrder, Date correctionDate) {
        StringBuilder builder = new StringBuilder();
        builder.append(year).append(", ");
        builder.append(reportPeriodName);

        if (periodOrder != null) {
            builder.append(", ").append(Formats.getRussianMonthNameWithTier(periodOrder));
        }
        if (correctionDate != null) {
            builder.append(", корр. (").append(DATE_TIME_FORMAT.format(correctionDate)).append(")");
        }
        return builder.toString();
    }

    @Override
    protected void onBind() {
        super.onBind();
        ValueChangeHandler<String> valueChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onUploadTF(false, false, event.getValue());
            }
        };

        CheckHandler checkHandler = new CheckHandler() {
            @Override
            public boolean onCheck() {
                if (!eventScriptStatus.get(FormDataEvent.IMPORT)) {
                    Dialog.infoMessage(NOT_EXIST_EVENT_MSG);
                    return false;
                }
                return true;
            }
        };
        getView().addFileUploadValueChangeHandler(valueChangeHandler, checkHandler);
        addRegisteredHandler(SetFocus.getType(), this);
    }

    private void onUploadTF(final boolean force, final boolean cancelTask, final String uuid) {
        final ReportType reportType = ReportType.IMPORT_FD;
        final UploadDataRowsAction action = new UploadDataRowsAction();
        action.setFormData(formData);
        action.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
        action.setForce(force);
        action.setUuid(uuid);
        action.setCancelTask(cancelTask);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<UploadFormDataResult>() {
            @Override
            public void onSuccess(UploadFormDataResult result) {
                LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                if (result.isLock()) {
                    Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                        @Override
                        public void yes() {
                            onUploadTF(true, cancelTask, uuid);
                        }
                    });
                } else if (result.isLockTask()) {
                    if (!modifiedRows.isEmpty()) {
                        edited = true;
                        modifiedRows.clear();
                    }
                    LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                    Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                        @Override
                        public void yes() {
                            onUploadTF(force, true, uuid);

                        }
                    });
                } else {
                    if (!modifiedRows.isEmpty()) {
                        edited = true;
                        modifiedRows.clear();
                    }
                    timerType = reportType;
                    timer.run();
                    getView().setSelectedRow(null, true);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof TaActionException) {
                    LogAddEvent.fire(FormDataPresenter.this, ((TaActionException) caught).getUuid());
                }
                modifiedRows.clear();
                getView().updateData();
            }
        }, this));
    }

    @Override
    public void onOpenSearchDialog() {
        LogCleanEvent.fire(FormDataPresenter.this);
        if (!modifiedRows.isEmpty()) {
            PreSearchAction preSearchAction = new PreSearchAction();
            preSearchAction.setFormData(formData);
            preSearchAction.setModifiedRows(new ArrayList<DataRow<Cell>>(modifiedRows));
            dispatcher.execute(preSearchAction, CallbackUtils.defaultCallback(new AbstractCallback<DataRowResult>() {
                @Override
                public void onSuccess(DataRowResult result) {
                    edited = true;
                    modifiedRows.clear();
                    LogAddEvent.fire(FormDataPresenter.this, result.getUuid());
                    getView().updateData();
                    getView().setSelectedRow(result.getCurrentRow(), true);
                    formSearchPresenter.open(readOnlyMode, formData.isManual());
                    addToPopupSlot(formSearchPresenter);
                }

                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof TaActionException) {
                        LogAddEvent.fire(FormDataPresenter.this, ((TaActionException) caught).getUuid());
                    }
                    modifiedRows.clear();
                    getView().updateData();
                }

            }, FormDataPresenter.this));
        } else {
            formSearchPresenter.open(readOnlyMode, formData.isManual());
            addToPopupSlot(formSearchPresenter);
        }
    }

    @Override
    public void onSetFocus(SetFocus event) {
        getView().setFocus(event.getRowIndex());
    }

    @Override
    protected void onHide() {
        super.onHide();
        removeFromPopupSlot(formSearchPresenter);
        formSearchPresenter.close();
        stopTimer();
    }

    private void updateFormMode(final boolean isTimer) {
        final ReportType oldType = timerType;
        TimerTaskAction action = new TimerTaskAction();
        action.setFormDataId(formData.getId());
        dispatcher.execute(
                action,
                CallbackUtils.simpleCallback(
                        new AbstractCallback<TimerTaskResult>() {
                            @Override
                            public void onSuccess(TimerTaskResult result) {
                                if (isTimer && !timerEnabled)
                                    return;
                                timerType = result.getTaskType();
                                boolean isUpdate = false;
                                if (readOnlyMode) {
                                    if (timerType == null
                                            && oldType != null) {
                                        isUpdate = true;
                                        // задача завершена, обновляем таблицу с данными
                                        switch (oldType) {
                                            case MOVE_FD:
                                                revealFormData(true, formData.isManual(), !absoluteView, logAreaPresenter.getUuid());
                                                break;
                                            default:
                                                getView().updateData();
                                        }
                                        manualMenuPresenter.updateNotificationCount();
                                    } else if (oldType != null && !oldType.equals(timerType)) {
                                        // изменился тип задачи, возможно нужно обновить форму???
                                        isUpdate = true;
                                        switch (oldType) {
                                            case MOVE_FD:
                                                revealFormData(true, formData.isManual(), !absoluteView, logAreaPresenter.getUuid());
                                                break;
                                            default:
                                                getView().updateData();
                                        }
                                        manualMenuPresenter.updateNotificationCount();
                                    }
                                } else {
                                    if ((timerType == null || ReportType.EDIT_FD.equals(timerType))
                                            && (oldType != null && !ReportType.EDIT_FD.equals(timerType))) {
                                        // задача завершена, обновляем таблицу с данными
                                        isUpdate = true;
                                        getView().updateData();
                                        manualMenuPresenter.updateNotificationCount();
                                    } else if (oldType != null && !oldType.equals(timerType)) {
                                        // изменился тип задачи, возможно нужно обновить форму???
                                        isUpdate = true;
                                        getView().updateData();
                                        manualMenuPresenter.updateNotificationCount();
                                    }
                                }
                                if (!isTimer || isUpdate || !result.getFormMode().equals(formMode) || result.getLockInfo().isEditMode() != lockEditMode || (taskName != null && !taskName.equals(result.getTaskName()) || taskName == null && result.getTaskName() != null)) {
                                    edited = result.isEdited();
                                    taskName = result.getTaskName();
                                    lockEditMode = result.getLockInfo().isEditMode();
                                    formMode = result.getFormMode();
                                    switch (result.getFormMode()) {
                                        case EDIT:
                                            if (readOnlyMode) {
                                                if (result.getLockInfo().isEditMode()) {
                                                    setLowReadLockedMode(result.getLockInfo());
                                                    updateReportStatus(false);
                                                } else {
                                                    setReadUnlockedMode();
                                                }
                                            } else {
                                                setEditMode();
                                            }
                                            break;
                                        case LOCKED_EDIT:
                                            if (readOnlyMode) {
                                                setLowReadLockedMode(result.getLockInfo());
                                                updateReportStatus(false);
                                            } else {
                                                setLowEditLockedMode(result.getLockInfo(), result.getTaskName());
                                            }
                                            break;
                                        case LOCKED:
                                            setReadLockedMode(true, result.getLockInfo());
                                            updateReportStatus(false);
                                            break;
                                        case LOCKED_READ:
                                            setLowReadLockedMode(result.getLockInfo());
                                            updateReportStatus(false);
                                            break;
                                    }
                                } else {
                                    edited = result.isEdited();
                                    taskName = result.getTaskName();
                                    lockEditMode = result.getLockInfo().isEditMode();
                                    formMode = result.getFormMode();
                                }
                                getView().redrawTable();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                super.onFailure(caught);
                            }
                        })
        );
    }
}
