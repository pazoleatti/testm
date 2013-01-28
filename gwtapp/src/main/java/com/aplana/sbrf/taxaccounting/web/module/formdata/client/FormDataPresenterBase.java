package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormDataResult;
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
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.List;
import java.util.logging.Logger;

public class FormDataPresenterBase<Proxy_ extends ProxyPlace<?>> extends
		Presenter<FormDataPresenterBase.MyView, Proxy_>{
	protected Logger logger = Logger.getLogger(getClass().getName());


	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenterBase}
	 * 's view.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataUiHandlers> {

		void setColumnsData(List<Column> columnsData, boolean readOnly);

		void setRowsData(List<DataRow> rowsData);

		void addCustomHeader(boolean addNumberedHeader);

		void addCustomTableStyles(List<FormStyle> allStyles);

		void setLogMessages(List<LogEntry> logEntries);

		void setAdditionalFormInfo(String formType, String taxType,
				String formKind, String departmentId, String reportPeriod,
				String state);

		void setWorkflowButtons(List<WorkflowMove> moves);

		void showOriginalVersionButton(boolean show);

		void showSaveButton(boolean show);

		void showRecalculateButton(boolean show);

		void showCheckButton(boolean show);

		void showAddRowButton(boolean show);

		void showRemoveRowButton(boolean show);

		void showPrintButton(boolean show);

		void showManualInputButton(boolean show);

		void showDeleteFormButton(boolean show);

		void setLockInformation(boolean isVisible, String lockDate, String lockedBy);

		void showWorkflowButton(boolean show);

		DataRow getSelectedRow();

		void enableRemoveRowButton(boolean enable);
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
	protected final PlaceManager placeManager;

	protected FormData formData;
	protected FormDataAccessParams accessParams;
	protected boolean readOnlyMode;
	protected boolean isFormDataLocked;
	protected boolean isLockedByCurrentUser;

	public FormDataPresenterBase(EventBus eventBus, MyView view, Proxy_ proxy,
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		closeFormDataHandlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(Window.ClosingEvent event) {
				setFormDataLockedMode(false, null, null);
				if (isFormDataLocked && isLockedByCurrentUser) {
					unlockForm(formData.getId());
				}
				closeFormDataHandlerRegistration.removeHandler();
			}
		});
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
				this);
	}

	@Override
	protected void onHide() {
		super.onHide();
		closeFormDataHandlerRegistration.removeHandler();
		if(isFormDataLocked && isLockedByCurrentUser){
			unlockForm(formData.getId());
		}
		setFormDataLockedMode(false, null, null);
	}

	protected void showReadOnlyModeButtons(boolean isLockedModeEnabled) {
		MyView view = getView();

		view.showSaveButton(false);
		view.showRemoveRowButton(false);
		view.showRecalculateButton(false);
		view.showAddRowButton(false);
		view.showOriginalVersionButton(false);

		view.showPrintButton(true);

		view.showManualInputButton(accessParams.isCanEdit() && !isLockedModeEnabled);
		view.showDeleteFormButton(accessParams.isCanDelete() && !isLockedModeEnabled);
	}

	protected void showEditModeButtons() {
		MyView view = getView();
		// сводная форма уровня Банка.
		if ((formData.getDepartmentId() == 1)
				&& (formData.getKind() == FormDataKind.SUMMARY)) {
			view.showOriginalVersionButton(true);
		} else {
			view.showOriginalVersionButton(false);
		}

		view.showSaveButton(true);
		view.showRecalculateButton(true);
		view.showAddRowButton(!formData.getFormType().isFixedRows());
		view.showRemoveRowButton(!formData.getFormType().isFixedRows());

		view.showPrintButton(false);
		view.showManualInputButton(false);
		view.showDeleteFormButton(false);
	}

	protected void setFormDataLockedMode(boolean isLocked, String lockedBy, String lockDate){
		MyView view = getView();
		if(isLocked){
			view.showManualInputButton(false);
			view.showDeleteFormButton(false);
			view.showWorkflowButton(false);
			view.setLockInformation(true, lockDate, lockedBy);
		} else {
			view.showManualInputButton(true);
			view.showDeleteFormButton(true);
			view.showWorkflowButton(true);
			view.setLockInformation(false, null, null);
		}
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

	protected void unlockForm(long formId){
		UnlockFormData action = new UnlockFormData();
		action.setFormId(formId);
		dispatcher.execute(action,
				new AbstractCallback<UnlockFormDataResult>() {
					@Override
					public void onReqSuccess(UnlockFormDataResult result) {
						if(result.isUnlockedSuccessfully()){
							isFormDataLocked = false;
							isLockedByCurrentUser = false;
							getView().setLockInformation(false, null, null);
						}
					}
				});
	}
}
