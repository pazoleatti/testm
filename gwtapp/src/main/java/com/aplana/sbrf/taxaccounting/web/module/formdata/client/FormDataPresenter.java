package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AccessFlags;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
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
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class FormDataPresenter extends Presenter<FormDataPresenter.MyView, FormDataPresenter.MyProxy> 
									implements FormDataUiHandlers{
	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NAME_TOKEN)
	public interface MyProxy extends ProxyPlace<FormDataPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter}'s view.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataUiHandlers> {

		void setColumnsData(List<Column> columnsData);
		void setRowsData(List<DataRow> rowsData, boolean readOnly);
		void setLogMessages(List<LogEntry> logEntries);
		void setAdditionalFormInfo(String formType, String taxType, String formKind,
									String departmentId, String reportPeriod, String state);

		void setWorkflowButtons(List<WorkflowMove> moves);
		
		void showOriginalVersionButton(boolean show);
		void showSaveButton(boolean show);
		void showRecalculateButton(boolean show);
		void showAddRowButton(boolean show);
		void showRemoveRowButton(boolean show);
		void showPrintButton(boolean show);
		void showManualInputButton(boolean show);
		void showDeleteFormButton(boolean show);
	}

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";
	public static final String READ_ONLY = "readOnly";
	
	public static final String DEPARTMENT_ID = "departmentId";
	
	public static final String FORM_DATA_KIND_ID = "formDataKindId";
	
	public static final String FORM_DATA_TYPE_ID = "formDataTypeId";
	
	public static final String FORM_DATA_RPERIOD_ID = "formDataTypeId";

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	protected FormData formData;
	protected AccessFlags flags;
	protected boolean readOnlyMode;

	@Inject
	public FormDataPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		readOnlyMode = Boolean.parseBoolean(request.getParameter(READ_ONLY, "true"));
		
		GetFormData action = new GetFormData();
		action.setFormDataId(Long.parseLong(request.getParameter(FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))));
		action.setDepartmentId(Long.parseLong(request.getParameter(DEPARTMENT_ID, String.valueOf(Long.MAX_VALUE))));
		action.setFormDataKind(Long.parseLong((request.getParameter(FORM_DATA_KIND_ID, String.valueOf(Long.MAX_VALUE)))));
		action.setFormDataTypeId((Long.parseLong((request.getParameter(FORM_DATA_TYPE_ID, String.valueOf(Long.MAX_VALUE))))));
		action.setReportPeriodId(Long.parseLong(request.getParameter(FORM_DATA_RPERIOD_ID, String.valueOf(Long.MAX_VALUE))));
		
		dispatcher.execute(action, new AbstractCallback<GetFormDataResult>() {
			@Override
			public void onReqSuccess(GetFormDataResult result) {
				formData = result.getFormData();
				flags= result.getAccessFlags();
				if (!readOnlyMode && result.getAccessFlags().getCanEdit()) {
					showEditModeButtons();
					getView().setWorkflowButtons(null);
				} else {
					showReadOnlyModeButtons();
					getView().setWorkflowButtons(result.getAvailableMoves());
					
			}
				getView().setAdditionalFormInfo(
						result.getFormData().getFormType().getName(),
						result.getFormData().getFormType().getTaxType().getName(),
						result.getFormData().getKind().getName(),
						result.getDepartmenName(),
						result.getReportPeriod(),
						result.getFormData().getState().getName()
					);
				
				getView().setColumnsData(formData.getFormColumns());
				getView().setRowsData(formData.getDataRows(), readOnlyMode);
				getProxy().manualReveal(FormDataPresenter.this);
				
				super.onReqSuccess(result);
			}

			@Override
			protected void onReqFailure(Throwable throwable) {
				logger.log(Level.INFO, "Can't open form", throwable);
				placeManager.revealPlace(new PlaceRequest(FormDataListNameTokens.FORM_DATA_LIST).with("nType", String.valueOf(TaxType.TRANSPORT)));
				super.onReqFailure(throwable);
			}
			
		});
	}
		
	@Override
	public boolean useManualReveal() {
		return true;
	}

		
	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}
	
	@Override
	public void onCancelClicked() {
		placeManager.revealPlace(new PlaceRequest(FormDataListNameTokens.FORM_DATA_LIST).with("nType", String.valueOf(TaxType.TRANSPORT)));
	}
	
	@Override
	public void onSaveClicked() {
		SaveFormDataAction action = new SaveFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, new AbstractCallback<SaveFormDataResult>(){
			@Override
			public void onReqSuccess(SaveFormDataResult result) {
				FormDataPresenter.this.formData = result.getFormData();
				getView().setLogMessages(result.getLogEntries());
				getView().setRowsData(FormDataPresenter.this.formData.getDataRows(), readOnlyMode);
				super.onReqSuccess(result);
			}

			@Override
			public void onReqFailure(Throwable throwable) {
				logger.log(Level.SEVERE, "Failed to save formData object", throwable);
			}							
		});
		
	}
	
	@Override
	public void onAddRowClicked() {
		formData.appendDataRow(null);
		getView().setRowsData(formData.getDataRows(), readOnlyMode);
	}
	
	@Override
	public void onRemoveRowClicked(DataRow dataRow) {
		formData.getDataRows().remove(dataRow);
		getView().setRowsData(formData.getDataRows(), readOnlyMode);
	}
	
	@Override
	public void onManualInputClicked() {
		refreshForm(false);
			}							
		
	@Override
	public void onOriginalVersionClicked() {
		Window.alert("В разработке");
	}
	
	@Override
	public void onRecalculateClicked() {
		RecalculateFormDataAction action = new RecalculateFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, new AbstractCallback<RecalculateFormDataResult>(){
			@Override
			public void onReqSuccess(RecalculateFormDataResult result) {
				formData = result.getFormData();
				getView().setLogMessages(result.getLogEntries());
				getView().setRowsData(formData.getDataRows(), readOnlyMode);
				super.onReqSuccess(result);
			}
		});
	}
	
	@Override
	public void onPrintClicked() {
		Window.alert("В разработке");
	}
	
	@Override
	public void onDeleteFormClicked() {
		boolean isOK = Window.confirm("Удалить?");
		if (isOK) {
			DeleteFormDataAction action = new DeleteFormDataAction();
			action.setFormDataId(formData.getId());
			dispatcher.execute(action, new AbstractCallback<DeleteFormDataResult>(){
				@Override
				public void onReqSuccess(DeleteFormDataResult result) {
					placeManager.revealPlace(new PlaceRequest(FormDataListNameTokens.FORM_DATA_LIST));
				}

				@Override
				public void onReqFailure(Throwable throwable) {
					logger.log(Level.SEVERE, "Failed to delete formData object", throwable);
					super.onReqFailure(throwable);
				}							
			});
		}
	}
	
	private void showReadOnlyModeButtons() {
		MyView view = getView();
		
		view.showSaveButton(false);
		view.showRemoveRowButton(false);
		view.showRecalculateButton(false);
		view.showAddRowButton(false);
		view.showOriginalVersionButton(false);
		
		view.showPrintButton(true);

		view.showManualInputButton(flags.getCanEdit());
		view.showDeleteFormButton(flags.getCanDelete());
	}
	
	private void showEditModeButtons() {
		MyView view = getView();
		// сводная форма уровня Банка.
		if ((formData.getDepartmentId() == 1) && (formData.getKind() == FormDataKind.SUMMARY)) {
			view.showOriginalVersionButton(true);
		} else {
			view.showOriginalVersionButton(false);
		}
		
		view.showSaveButton(true);
		view.showRecalculateButton(true);
		view.showAddRowButton(true);
		view.showRemoveRowButton(true);
		
		view.showPrintButton(false);
		view.showManualInputButton(false);
		view.showDeleteFormButton(false);
	}
	
	public void refreshForm(Boolean readOnly) {
		placeManager.revealPlace(new PlaceRequest(
				FormDataPresenter.NAME_TOKEN).with(FormDataPresenter.READ_ONLY, readOnly.toString()).with(
				FormDataPresenter.FORM_DATA_ID, formData.getId().toString()));
	}

	@Override
	public void onWorkflowMove(WorkflowMove wfMove) {
		GoMoveAction action = new GoMoveAction();
		action.setFormDataId(formData.getId());
		action.setMove(wfMove);
		dispatcher.execute(action, new AbstractCallback<GoMoveResult>(){
			@Override
			public void onReqSuccess(GoMoveResult result) {
				getView().setLogMessages(result.getLogEntries());
				refreshForm(true);
				super.onReqSuccess(result);
			}
		});
	}
}
