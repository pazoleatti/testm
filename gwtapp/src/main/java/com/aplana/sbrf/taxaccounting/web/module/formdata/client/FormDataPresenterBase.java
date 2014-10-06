package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.search.FormSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

		void setRowsData(int start, int totalCount, List<DataRow<Cell>> rowsData);

		void addCustomHeader(List<DataRow<HeaderCell>> headers);

		void addCustomTableStyles(List<FormStyle> allStyles);

		void setAdditionalFormInfo(String formType, TaxType taxType, String formKind, String departmentId,
                                   String reportPeriod, String state, Date startDate, Date endDate, Long formDataId,
                                   boolean correctionPeriod, boolean correctionDiff);

		void setWorkflowButtons(List<WorkflowMove> moves);

		void setBackButton(String link);

		void showOriginalVersionButton(boolean show);

		void showSaveCancelPanel(boolean show);

        void showAddRemoveRowsBlock(boolean show);

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

		void setLockInformation(boolean isVisible, String lockDate, String lockedBy);

		DataRow<Cell> getSelectedRow();

		void setSelectedRow(DataRow<Cell> item, boolean selected);

		void enableRemoveRowButton(boolean enable);
		
		boolean getCheckedColumnsClicked();

		void assignDataProvider(int pageSize);

        int getPageSize();

		void updateData();
		
        void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler);

        void isCanEditPage(boolean visible);

        void updatePageSize(TaxType taxType);

        TaxType getTaxType();

        void setFocus(Long rowIndex);

        void setupSelectionModel(boolean fixedRows);

        /** Текст кнопки-ссылки для переключения видов «Абсолютные значения»/«Корректировка» */
        void setCorrectionText(String text);
    }

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";
	public static final String READ_ONLY = "readOnly";
    public static final String MANUAL = "manual";
    public static final String CORRECTION = "correction";
    public static final String UUID = "uuid";

	protected HandlerRegistration closeFormDataHandlerRegistration;

	protected final DispatchAsync dispatcher;
	protected final TaPlaceManager placeManager;
	protected final SignersPresenter signersPresenter;
	protected final DialogPresenter dialogPresenter;
	protected final HistoryPresenter historyPresenter;
	protected final FormSearchPresenter formSearchPresenter;
    protected final SourcesPresenter sourcesPresenter;

    protected FormData formData;
	
	protected FormDataAccessParams formDataAccessParams;

    /** Признак сводной формы банка */
    protected boolean isBankSummaryForm;

    /** Признак существования версии ручного ввода */
    protected boolean existManual;

    protected boolean canCreatedManual;

	protected boolean readOnlyMode;

	protected boolean forceEditMode = false;
	
	protected boolean fixedRows;
    // Признак отображения вида для форм в корректирующих периодах, true - обычный режим, false - режим отображения изенений
    protected boolean absoluteView = true;

    /** Идентификатор сообщений в лог. Используется в случаях, когда не нужна перезагрузка страницы */
    protected String innerLogUuid;

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
                                 SourcesPresenter sourcesPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager)placeManager;
		this.dispatcher = dispatcher;
		this.signersPresenter = signersPresenter;
		this.dialogPresenter = dialogPresenter;
        this.formSearchPresenter = formDataPresenter;
        this.sourcesPresenter = sourcesPresenter;
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

    @Override
    public void onReset(){
        // при каждом открытии страницы скрываем модальные окна, на случай если они были открыты а адрес страницы поменяли
        this.signersPresenter.getView().hide();
        this.dialogPresenter.getView().hide();
        this.formSearchPresenter.getView().hide();
        this.sourcesPresenter.getView().hide();
    }

	@Override
	protected void onHide() {
		super.onHide();
		if (closeFormDataHandlerRegistration !=null ){
			closeFormDataHandlerRegistration.removeHandler();
		}
		unlockForm(formData.getId());
	}
	
	protected void setReadLockedMode(String lockedBy, String lockDate){
		readOnlyMode = true;
		
		MyView view = getView();
		view.showSaveCancelPanel(false);
        view.showAddRemoveRowsBlock(false);
		view.showRecalculateButton(false);
		view.showOriginalVersionButton(false);
		view.showPrintAnchor(true);
		view.showDeleteFormButton(false);
		view.setLockInformation(true, lockDate, lockedBy);
		
		view.setWorkflowButtons(null);
		view.showCheckButton(false);
        view.showEditModeLabel(false);

        getView().showEditAnchor(false);
        getView().showModeAnchor(existManual, formData.isManual());
        getView().showManualAnchor(false);
        getView().showDeleteManualAnchor(false);
	}

	protected void setReadUnlockedMode() {
        readOnlyMode = true;
		
		MyView view = getView();
		view.showSaveCancelPanel(false);
        view.showEditModeLabel(false);
        view.showAddRemoveRowsBlock(false);
		view.showRecalculateButton(false);
		view.showOriginalVersionButton(false);
		view.showPrintAnchor(true);
		view.showDeleteFormButton(formDataAccessParams.isCanDelete());
		view.setLockInformation(false, null, null);
		
		view.setWorkflowButtons(formDataAccessParams.getAvailableWorkflowMoves());
		view.showCheckButton(formDataAccessParams.isCanRead());

        getView().showEditAnchor(formDataAccessParams.isCanEdit());
        getView().showModeAnchor(existManual, formData.isManual());
        getView().showManualAnchor(canCreatedManual && !existManual);
        getView().showDeleteManualAnchor(false);
        placeManager.setOnLeaveConfirmation(null);
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

		view.showSaveCancelPanel(true);
        view.showEditModeLabel(true);
		view.showRecalculateButton(!formData.isManual());
        view.showAddRemoveRowsBlock(!fixedRows);

		view.showPrintAnchor(false);
		view.showDeleteFormButton(false);
		view.setLockInformation(false, null, null);
		
		view.setWorkflowButtons(null);
		view.showCheckButton(formDataAccessParams.isCanRead());
		view.setSelectedRow(null, true);

        getView().showEditAnchor(false);
        getView().showModeAnchor(false, false);
        getView().showManualAnchor(false);
        getView().showDeleteManualAnchor(formData.isManual());

		placeManager.setOnLeaveConfirmation("Вы уверены, что хотите прекратить редактирование данных налоговой формы?");
		closeFormDataHandlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
			@Override
			public void onClose(CloseEvent<Window> event) {
				closeFormDataHandlerRegistration.removeHandler();
				unlockForm(formData.getId());
			}
		});
	}

	protected void revealFormDataList() {
		placeManager.revealPlace(new PlaceRequest.Builder().nameToken(
				FormDataListNameTokens.FORM_DATA_LIST).with("nType",
				String.valueOf(formData.getFormType().getTaxType())).build());
	}
	
	protected void revealFormData(boolean readOnly, boolean isManual, boolean correctionDiff, String uuid) {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(FormDataPresenterBase.NAME_TOKEN)
                .with(FormDataPresenterBase.READ_ONLY, String.valueOf(readOnly))
                .with(FormDataPresenterBase.FORM_DATA_ID, String.valueOf(formData.getId()));
        if (isManual) {
            builder.with(FormDataPresenterBase.MANUAL, String.valueOf(isManual));
        }
        if (correctionDiff) {
            builder.with(FormDataPresenterBase.CORRECTION, String.valueOf(correctionDiff));
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
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}
}
