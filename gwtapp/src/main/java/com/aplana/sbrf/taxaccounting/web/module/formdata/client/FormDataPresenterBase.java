package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

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
				String state);

		void setWorkflowButtons(List<WorkflowMove> moves);

		void setBackButton(String link);

		void showOriginalVersionButton(boolean show);

		void showSaveCancelBar(boolean show);

		void showRecalculateButton(boolean show);

		void showCheckButton(boolean show);

		void showAddRowButton(boolean show);

		void showRemoveRowButton(boolean show);

		void showPrintAnchor(boolean show);

		void showManualInputAnchor(boolean show);

		void showDeleteFormButton(boolean show);

		void setLockInformation(boolean isVisible, String lockDate, String lockedBy);

		DataRow getSelectedRow();

		void setSelectedRow(DataRow<Cell> item, boolean selected);

		void enableRemoveRowButton(boolean enable);
		
		boolean getCheckedColumnsClicked();

		void assignDataProvider(int pageSize);

		void updateData();
	}

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";
	public static final String READ_ONLY = "readOnly";

	public static final String DEPARTMENT_ID = "departmentId";

	public static final String FORM_DATA_KIND_ID = "formDataKindId";

	public static final String FORM_DATA_TYPE_ID = "formDataTypeId";

	public static final String FORM_DATA_RPERIOD_ID = "formDataRPeriodId";

	public static final String WORK_FLOW_ID = "goWorkFlowId";

	private HandlerRegistration closeFormDataHandlerRegistration;

	protected final DispatchAsync dispatcher;
	protected final TaPlaceManager placeManager;
	protected final SignersPresenter signersPresenter;
	protected final DialogPresenter dialogPresenter;
	protected final HistoryPresenter historyPresenter;

	protected FormData formData;
	
	protected FormDataAccessParams formDataAccessParams;
	

	protected boolean readOnlyMode;

	protected boolean forceEditMode = false;
	
	protected boolean fixedRows;


	public FormDataPresenterBase(EventBus eventBus, MyView view, Proxy_ proxy,
								 PlaceManager placeManager, DispatchAsync dispatcher, SignersPresenter signersPresenter, DialogPresenter dialogPresenter, HistoryPresenter historyPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager)placeManager;
		this.dispatcher = dispatcher;
		this.signersPresenter = signersPresenter;
		this.dialogPresenter = dialogPresenter;
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		closeFormDataHandlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(Window.ClosingEvent event) {
				unlockForm(formData.getId());
				closeFormDataHandlerRegistration.removeHandler();
			}
		});
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	protected void onHide() {
		super.onHide();
		closeFormDataHandlerRegistration.removeHandler();
		unlockForm(formData.getId());
	}
	
	protected void setReadLockedMode(String lockedBy, String lockDate){
		readOnlyMode = true;
		
		MyView view = getView();
		view.showSaveCancelBar(false);
		view.showRemoveRowButton(false);
		view.showRecalculateButton(false);
		view.showAddRowButton(false);
		view.showOriginalVersionButton(false);
		view.showPrintAnchor(true);
		view.showManualInputAnchor(false);
		view.showDeleteFormButton(false);
		view.setLockInformation(true, lockDate, lockedBy);
		
		view.setWorkflowButtons(null);
		view.showCheckButton(formDataAccessParams.isCanRead());

	}

	protected void setReadUnlockedMode() {
		readOnlyMode = true;
		
		MyView view = getView();
		view.showSaveCancelBar(false);
		view.showRemoveRowButton(false);
		view.showRecalculateButton(false);
		view.showAddRowButton(false);
		view.showOriginalVersionButton(false);
		view.showPrintAnchor(true);
		view.showManualInputAnchor(formDataAccessParams.isCanEdit());
		view.showDeleteFormButton(formDataAccessParams.isCanDelete());
		view.setLockInformation(false, null, null);
		
		view.setWorkflowButtons(formDataAccessParams.getAvailableWorkflowMoves());
		view.showCheckButton(formDataAccessParams.isCanRead());
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

		view.showSaveCancelBar(true);
		view.showRecalculateButton(true);
		view.showAddRowButton(!fixedRows);
		view.showRemoveRowButton(!fixedRows);

		view.showPrintAnchor(false);
		view.showManualInputAnchor(false);
		view.showDeleteFormButton(false);
		view.setLockInformation(false, null, null);
		
		view.setWorkflowButtons(null);
		view.showCheckButton(formDataAccessParams.isCanRead());
		view.setSelectedRow(null, true);
	}


	
	protected void revealForm(Boolean readOnly) {
		placeManager.revealPlace(new PlaceRequest(FormDataPresenterBase.NAME_TOKEN)
				.with(FormDataPresenterBase.READ_ONLY, readOnly.toString()).with(
						FormDataPresenterBase.FORM_DATA_ID,
						formData.getId().toString()));
	}

	protected void revealForm(Boolean readOnly, Integer wfMove) {
		placeManager.revealPlace(new PlaceRequest(FormDataPresenterBase.NAME_TOKEN)
				.with(FormDataPresenterBase.WORK_FLOW_ID, wfMove.toString())
				.with(FormDataPresenterBase.READ_ONLY, readOnly.toString()).with(
						FormDataPresenterBase.FORM_DATA_ID,
						formData.getId().toString()));
	}

	@SuppressWarnings("unchecked")
	protected void unlockForm(Long formId){
		if (!readOnlyMode && formData.getId()!=null){
			UnlockFormData action = new UnlockFormData();
			action.setFormId(formId);
			dispatcher.execute(action, CallbackUtils.emptyCallback());
		}
	}
}
