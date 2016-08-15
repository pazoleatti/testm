package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.comments.FilesCommentsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.search.FormSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.LockInfo;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.CheckHandler;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.*;
import java.util.logging.Logger;

public class FormDataPresenterBase<Proxy_ extends ProxyPlace<?>> extends
		Presenter<FormDataPresenterBase.MyView, Proxy_>{
    protected Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenterBase}
	 * 's view.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataUiHandlers> {

		void setColumnsData(List<Column> columnsData, boolean readOnly, boolean forceEditMode);

        void setTableLockMode(boolean lockMode);

		void setRowsData(int start, int totalCount, List<DataRow<Cell>> rowsData);

		void addCustomHeader(List<DataRow<HeaderCell>> headers);

		void addCustomTableStyles(List<FormStyle> allStyles);

		void setAdditionalFormInfo(String formType, TaxType taxType, String formKind, String departmentId,
                                   String reportPeriod, String comparativePeriod, String state, Date startDate, Date endDate, Long formDataId,
                                   boolean correctionPeriod, boolean correctionDiff, boolean readOnly);

		void setWorkflowButtons(List<WorkflowMove> moves);

		void setBackButton(String link);

		void showOriginalVersionButton(boolean show);

		void showSaveCancelPanel(boolean show, boolean readOnlyMode);

        void showAddRemoveRowsBlock(boolean show);

        void showRefreshButton(boolean show);

        void showRecalculateButton(boolean show);

		void showCheckButton(boolean show);

		void showPrintAnchor(boolean show);

        void showEditModeLabel(boolean show);

        void showEditAnchor(boolean show);

		void showDeleteFormButton(boolean show);

		void showSignersAnchor(boolean show);

        void showModeAnchor(boolean show, boolean manual);

        void showManualAnchor(boolean show);

        void showDeleteManualAnchor(boolean show);

		void setLockInformation(boolean isVisible, boolean readOnlyMode, LockInfo lockInfo, TaxType taxType);

		DataRow<Cell> getSelectedRow();

		void setSelectedRow(DataRow<Cell> item, boolean selected);

		void enableRemoveRowButton(boolean enable);

        boolean getCheckedColumnsClicked();

		void assignDataProvider(int pageSize);

        int getPageSize();

		void updateData();

        void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler, CheckHandler checkHandler);

        void isCanEditPage(boolean visible);

        void updatePageSize(TaxType taxType);

        TaxType getTaxType();

        void setFocus(Long rowIndex);

        void setupSelectionModel(boolean fixedRows);

        void showCorrectionButton(boolean correctionPeriod);

        void updateTableTopPosition(int position);

        void updatePrintReportButtonName(String fdReportType, boolean isLoad);

        void showConsolidation(boolean isShown);

        void updateRightButtonsHeight(int height);

        void setReportTypes(List<FormDataReportType> reportTypes);

        int generateNewSessionId();
    }

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";
	public static final String READ_ONLY = "readOnly";
    public static final String MANUAL = "manual";
    public static final String CORRECTION = "correction";
    public static final String UUID = "uuid";
    public static final String FREE = "free";

	protected HandlerRegistration closeFormDataHandlerRegistration = null;

	protected final DispatchAsync dispatcher;
	protected final TaPlaceManager placeManager;
	protected final SignersPresenter signersPresenter;
	protected final DialogPresenter dialogPresenter;
	protected final HistoryPresenter historyPresenter;
	protected final FormSearchPresenter formSearchPresenter;
    protected final SourcesPresenter sourcesPresenter;
    protected final FilesCommentsPresenter filesCommentsPresenter;

    protected FormData formData;

	protected FormDataAccessParams formDataAccessParams;

    /** Признак сводной формы банка */
    protected boolean isBankSummaryForm;

    /** Признак существования версии ручного ввода */
    protected boolean existManual;

    protected boolean canCreatedManual;

	protected boolean readOnlyMode;

    protected boolean updating;

    protected boolean forceEditMode = false;
    protected boolean free;
	
	protected boolean fixedRows;
    // Признак отображения вида для форм в корректирующих периодах, true - обычный режим, false - режим отображения изенений
    protected boolean absoluteView = true;
    protected boolean correctionPeriod = false;
    protected boolean edited;
    protected String taskName;
    protected TimerTaskResult.FormMode formMode;
    protected boolean lockEditMode;
    protected Map<FormDataEvent, Boolean> eventScriptStatus;

    protected boolean timerEnabled;
    protected Timer timer;

	protected Set<DataRow<Cell>> modifiedRows = new HashSet<DataRow<Cell>>();

	public FormDataPresenterBase(EventBus eventBus,
	                             MyView view,
	                             Proxy_ proxy,
								 PlaceManager placeManager,
								 DispatchAsync dispatcher,
								 SignersPresenter signersPresenter,
								 DialogPresenter dialogPresenter,
								 HistoryPresenter historyPresenter,
                                 FormSearchPresenter formDataPresenter,
                                 SourcesPresenter sourcesPresenter,
                                 FilesCommentsPresenter filesCommentsPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager)placeManager;
		this.dispatcher = dispatcher;
		this.signersPresenter = signersPresenter;
		this.dialogPresenter = dialogPresenter;
        this.formSearchPresenter = formDataPresenter;
        this.sourcesPresenter = sourcesPresenter;
        this.filesCommentsPresenter = filesCommentsPresenter;
	}

    protected void startTimer() {
        timerEnabled = true;
        timer.scheduleRepeating(5000);
    }

    protected void stopTimer() {
        timerEnabled = false;
        timer.cancel();
    }


    @Override
	public boolean useManualReveal() {
		return true;
	}

    @Override
    public void onReset(){
        // при каждом открытии страницы скрываем модальные окна, на случай если они были открыты а адрес страницы поменяли
        this.filesCommentsPresenter.getView().hide();
        this.signersPresenter.getView().hide();
        this.dialogPresenter.getView().hide();
        this.formSearchPresenter.getView().hide();
        this.sourcesPresenter.getView().hide();
    }

	@Override
	protected void onHide() {
		super.onHide();
        setOnLeaveConfirmation(null);
        unlockForm(formData.getId());
	}

    /**
     * Форма находиться в режиме просмотра, при этом другим пользователем запущена операция изменения данных/форма находиться в режиме редактирования
     * @param readOnlyMode
     * @param lockInfo
     */
	protected void setReadLockedMode(boolean readOnlyMode, LockInfo lockInfo){
		this.readOnlyMode = readOnlyMode;

		MyView view = getView();
		view.showSaveCancelPanel(false, readOnlyMode);
        view.showAddRemoveRowsBlock(false);
        view.showConsolidation(!lockInfo.isEditMode() && WorkflowState.ACCEPTED != formData.getState()
                &&
                (FormDataKind.CONSOLIDATED == formData.getKind() || FormDataKind.SUMMARY == formData.getKind() || FormDataKind.CALCULATED == formData.getKind())
                &&
                readOnlyMode);
        view.showRefreshButton(false);
		view.showRecalculateButton(false);
		view.showOriginalVersionButton(false);
		view.showPrintAnchor(true);
        view.showDeleteFormButton(!lockInfo.isEditMode() && formDataAccessParams.isCanDelete());
		view.setLockInformation(true, readOnlyMode, lockInfo, formData.getFormType().getTaxType());

        if (lockInfo.isEditMode()) {
            view.setWorkflowButtons(null);
        } else {
            view.setWorkflowButtons(formDataAccessParams.getAvailableWorkflowMoves());
        }
        view.showCheckButton(!lockInfo.isEditMode() && absoluteView);
        view.showEditModeLabel(false);

        view.showEditAnchor(!lockInfo.isEditMode() && formDataAccessParams.isCanEdit());
        view.showModeAnchor(existManual, formData.isManual());
        view.showManualAnchor(false);
        view.showDeleteManualAnchor(false);

        view.showCorrectionButton(!lockInfo.isEditMode() && correctionPeriod);

        view.setTableLockMode(true);
        view.setColumnsData(formData.getFormColumns(), true, forceEditMode);

        setOnLeaveConfirmation(null);
    }

    /**
     * Форма находиться в режиме просмотра, при этом текущим пользователем запущена операция изменения данных/форма находиться в режиме редактирования
     * @param lockInfo
     */
    protected void setLowReadLockedMode(LockInfo lockInfo){
        readOnlyMode = true;

        MyView view = getView();
        view.showSaveCancelPanel(false, readOnlyMode);
        view.showEditModeLabel(false);
        view.showAddRemoveRowsBlock(false);
        view.showConsolidation(!lockInfo.isEditMode() && WorkflowState.ACCEPTED != formData.getState()
                &&
                (FormDataKind.CONSOLIDATED == formData.getKind() || FormDataKind.SUMMARY == formData.getKind() || FormDataKind.CALCULATED == formData.getKind())
                &&
                readOnlyMode);
        view.showRefreshButton(false);
        view.showRecalculateButton(false);
        view.showOriginalVersionButton(false);
        view.showPrintAnchor(true);
        view.showDeleteFormButton(!lockInfo.isEditMode() && formDataAccessParams.isCanDelete());
        view.setLockInformation(true, readOnlyMode, lockInfo, formData.getFormType().getTaxType());

        if (lockInfo.isEditMode()) {
            view.setWorkflowButtons(null);
        } else {
            view.setWorkflowButtons(formDataAccessParams.getAvailableWorkflowMoves());
        }
        view.showCheckButton(!lockInfo.isEditMode() && absoluteView);

        view.showEditAnchor(!lockInfo.isEditMode() && formDataAccessParams.isCanEdit());
        view.showModeAnchor(existManual, formData.isManual());
        view.showManualAnchor(canCreatedManual && !existManual);
        view.showDeleteManualAnchor(false);

        view.showCorrectionButton(!lockInfo.isEditMode() && correctionPeriod);

        view.setTableLockMode(true);
        view.setColumnsData(formData.getFormColumns(), true, forceEditMode);

        setOnLeaveConfirmation(null);
    }

    /**
     * Форма находится в режиме редактирования, при этом запущена операция изменения данных
     * @param lockInfo
     */
    protected void setLowEditLockedMode(LockInfo lockInfo, String taskName){
        readOnlyMode = false;

        MyView view = getView();
        // сводная форма уровня Банка.
        if ((formData.getDepartmentId() == 1)
                && (formData.getKind() == FormDataKind.SUMMARY)) {
            view.showOriginalVersionButton(true);
        } else {
            view.showOriginalVersionButton(false);
        }

        view.showSaveCancelPanel(true, readOnlyMode);
        view.showEditModeLabel(true);
        view.showConsolidation(false);
        view.showRefreshButton(updating);
        view.showRecalculateButton(!formData.isManual());
        view.showAddRemoveRowsBlock(false);

        view.showPrintAnchor(false);
        view.showDeleteFormButton(false);
        view.setLockInformation(true, false, lockInfo, formData.getFormType().getTaxType());

        view.setWorkflowButtons(null);
        view.showCheckButton(formDataAccessParams.isCanRead() && absoluteView);
        view.setSelectedRow(null, true);

        view.showEditAnchor(false);
        view.showModeAnchor(false, false);
        view.showManualAnchor(false);
        view.showDeleteManualAnchor(formData.isManual());

        view.showCorrectionButton(correctionPeriod);

        view.setTableLockMode(true);
        view.setColumnsData(formData.getFormColumns(), readOnlyMode, forceEditMode);

        setOnLeaveConfirmation();
    }

	protected void setReadUnlockedMode() {
        readOnlyMode = true;
		
		MyView view = getView();
		view.showSaveCancelPanel(false, readOnlyMode);
        view.showEditModeLabel(false);
        view.showAddRemoveRowsBlock(false);
        view.showConsolidation(WorkflowState.ACCEPTED != formData.getState()
                &&
                (FormDataKind.CONSOLIDATED == formData.getKind() || FormDataKind.SUMMARY == formData.getKind() || FormDataKind.CALCULATED == formData.getKind())
                &&
                readOnlyMode);
        view.showRefreshButton(false);
		view.showRecalculateButton(false);
		view.showOriginalVersionButton(false);
		view.showPrintAnchor(true);
		view.showDeleteFormButton(formDataAccessParams.isCanDelete());
		view.setLockInformation(false, readOnlyMode, null, formData.getFormType().getTaxType());
		
		view.setWorkflowButtons(formDataAccessParams.getAvailableWorkflowMoves());
		view.showCheckButton(formDataAccessParams.isCanRead() && absoluteView);

        view.showEditAnchor(formDataAccessParams.isCanEdit());
        view.showModeAnchor(existManual, formData.isManual());
        view.showManualAnchor(canCreatedManual && !existManual);
        view.showDeleteManualAnchor(false);

        view.showCorrectionButton(correctionPeriod);

        view.setTableLockMode(false);
        view.setColumnsData(formData.getFormColumns(), readOnlyMode, forceEditMode);

        setOnLeaveConfirmation(null);
	}

	protected void setEditMode() {
		readOnlyMode = false;
		
		MyView view = getView();
		// сводная форма уровня Банка.
		if ((formData.getDepartmentId() == 1)
				&& (formData.getKind() == FormDataKind.SUMMARY)) {
			view.showOriginalVersionButton(true);
		} else {
			view.showOriginalVersionButton(false);
		}

		view.showSaveCancelPanel(true, readOnlyMode);
        view.showEditModeLabel(true);
        view.showConsolidation(false);
        view.showRefreshButton(updating);
		view.showRecalculateButton(!formData.isManual());
        view.showAddRemoveRowsBlock(!fixedRows);

		view.showPrintAnchor(false);
		view.showDeleteFormButton(false);
		view.setLockInformation(false, readOnlyMode, null, formData.getFormType().getTaxType());
		
		view.setWorkflowButtons(null);
		view.showCheckButton(formDataAccessParams.isCanRead() && absoluteView);
		view.setSelectedRow(null, true);

        view.showEditAnchor(false);
        view.showModeAnchor(false, false);
        view.showManualAnchor(false);
        view.showDeleteManualAnchor(formData.isManual());

        view.showCorrectionButton(correctionPeriod);

        view.setTableLockMode(false);
        view.setColumnsData(formData.getFormColumns(), readOnlyMode, forceEditMode);

        setOnLeaveConfirmation();
    }

    protected void setOnLeaveConfirmation() {
        if (taskName == null || TimerTaskResult.FormMode.EDIT.equals(formMode)) {
            if (edited || !modifiedRows.isEmpty()) {
                setOnLeaveConfirmation("Вы уверены, что хотите прекратить редактирование данных " + speck() + "? В случае выхода все несохраненные изменения будут утеряны.");
            } else {
                setOnLeaveConfirmation("Вы уверены, что хотите прекратить редактирование данных " + speck() + " (несохраненные изменения отсутствуют)?");
            }
        } else {
            if (edited || !modifiedRows.isEmpty()) {
                setOnLeaveConfirmation("Вы уверены, что хотите прекратить редактирование данных " + speck() + "? В случае выхода все несохраненные изменения будут утеряны и будет отменена операция \"" + taskName + "\".");
            } else {
                setOnLeaveConfirmation("Вы уверены, что хотите прекратить редактирование данных " + speck() + "? В случае выхода будет отменена операция \"" + taskName + "\".");
            }
        }
    }

    protected void setOnLeaveConfirmation(String msg) {
        placeManager.setOnLeaveConfirmation(msg);
        if (closeFormDataHandlerRegistration != null && msg == null) {
            closeFormDataHandlerRegistration.removeHandler();
        } else if (closeFormDataHandlerRegistration == null && msg != null) {
            closeFormDataHandlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
                @Override
                public void onClose(CloseEvent<Window> event) {
                    closeFormDataHandlerRegistration.removeHandler();
                    unlockForm(formData.getId());
                }
            });
        }
    }

	protected void revealFormDataList() {
		placeManager.revealPlace(new PlaceRequest.Builder().nameToken(
				FormDataListNameTokens.FORM_DATA_LIST).with("nType",
				String.valueOf(formData.getFormType().getTaxType())).build());
	}
	
	protected void revealFormData(boolean readOnly, boolean isManual, boolean correctionDiff, String uuid) {
        stopTimer();
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(FormDataPresenterBase.NAME_TOKEN)
                .with(FormDataPresenterBase.READ_ONLY, String.valueOf(readOnly))
                .with(FormDataPresenterBase.FORM_DATA_ID, String.valueOf(formData.getId()));
        if (isManual) {
            builder.with(FormDataPresenterBase.MANUAL, String.valueOf(true));
        }
        if (correctionDiff) {
            builder.with(FormDataPresenterBase.CORRECTION, String.valueOf(true));
        }
        if (uuid != null) {
            builder.with(UUID, uuid);
        }
        placeManager.revealPlace(builder.build());
	}

	@SuppressWarnings("unchecked")
	protected void unlockForm(Long formId){
		UnlockFormData action = new UnlockFormData();
		action.setFormId(formId);
        action.setManual(formData.isManual());
        action.setReadOnlyMode(readOnlyMode);
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}

    private String speck(){
        return formData.getFormType().getTaxType().isTax() ? "налоговой формы" : "формы";
    }
}
