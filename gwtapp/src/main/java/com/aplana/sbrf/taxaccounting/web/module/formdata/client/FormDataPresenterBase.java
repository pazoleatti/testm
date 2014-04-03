package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
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

		void setAdditionalFormInfo(String formType, TaxType taxType,
				String formKind, String departmentId, String reportPeriod,
                String state, Date startDate, Date endDate, Long formDataId);

		void setWorkflowButtons(List<WorkflowMove> moves);

		void setBackButton(String link);

		void showOriginalVersionButton(boolean show);

		void showSaveCancelPanel(boolean show);

        void showAddRemoveRowsBlock(boolean show);

        void showRecalculateButton(boolean show);

		void showCheckButton(boolean show);

		void showPrintAnchor(boolean show);

        void showEditModeLabel(boolean show);

		void showDeleteFormButton(boolean show);

		void showSignersAnchor(boolean show);

		void setLockInformation(boolean isVisible, String lockDate, String lockedBy);

		DataRow<Cell> getSelectedRow();

		void setSelectedRow(DataRow<Cell> item, boolean selected);

		void enableRemoveRowButton(boolean enable);
		
		boolean getCheckedColumnsClicked();

		void assignDataProvider(int pageSize);

        int getPageSize();

		void updateData();
		
		void updateData(int pageNumber);

        void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler);

        void isCanEditPage(boolean visible);

        void updatePageSize(TaxType taxType);

        void setVisibilityMode(boolean bankSummaryForm, boolean manual, boolean existManual, boolean readOnlyMode, boolean canCreatedManual);

        TaxType getTaxType();
    }

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";
	public static final String READ_ONLY = "readOnly";
    public static final String MANUAL = "manual";
    public static final String UUID = "uuid";

	protected HandlerRegistration closeFormDataHandlerRegistration;

	protected final DispatchAsync dispatcher;
	protected final TaPlaceManager placeManager;
	protected final SignersPresenter signersPresenter;
	protected final DialogPresenter dialogPresenter;
	protected final HistoryPresenter historyPresenter;

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

	protected Set<DataRow<Cell>> modifiedRows = new HashSet<DataRow<Cell>>();


	public FormDataPresenterBase(EventBus eventBus,
	                             MyView view,
	                             Proxy_ proxy,
								 PlaceManager placeManager,
								 DispatchAsync dispatcher,
								 SignersPresenter signersPresenter,
								 DialogPresenter dialogPresenter,
								 HistoryPresenter historyPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager)placeManager;
		this.dispatcher = dispatcher;
		this.signersPresenter = signersPresenter;
		this.dialogPresenter = dialogPresenter;
	}

	@Override
	public boolean useManualReveal() {
		return true;
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
        view.setVisibilityMode(isBankSummaryForm, formData.isManual(), existManual, false, canCreatedManual);
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
        view.setVisibilityMode(isBankSummaryForm, formData.isManual(), existManual, formDataAccessParams.isCanEdit(), canCreatedManual);
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
		view.showRecalculateButton(true);
        view.showAddRemoveRowsBlock(!fixedRows);

		view.showPrintAnchor(false);
		view.showDeleteFormButton(false);
		view.setLockInformation(false, null, null);
		
		view.setWorkflowButtons(null);
		view.showCheckButton(formDataAccessParams.isCanRead());
		view.setSelectedRow(null, true);
        view.setVisibilityMode(isBankSummaryForm, formData.isManual(), existManual, readOnlyMode, canCreatedManual);

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
	
	protected void revealFormData(Boolean readOnly, boolean isManual, String uuid) {
		placeManager.revealPlace(new PlaceRequest.Builder().nameToken(FormDataPresenterBase.NAME_TOKEN)
                .with(FormDataPresenterBase.READ_ONLY, String.valueOf(readOnly))
                .with(FormDataPresenterBase.MANUAL, String.valueOf(isManual))
                .with(FormDataPresenterBase.FORM_DATA_ID, String.valueOf(formData.getId())).build()
                .with(UUID, uuid)
        );
	}

	@SuppressWarnings("unchecked")
	protected void unlockForm(Long formId){
		UnlockFormData action = new UnlockFormData();
		action.setFormId(formId);
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}
}
