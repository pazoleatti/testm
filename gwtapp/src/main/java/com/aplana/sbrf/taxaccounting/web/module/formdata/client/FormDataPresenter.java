package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

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
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		void reloadRows();
		FormData getFormData();
		void setLogMessages(List<LogEntry> logEntries);
		void cleanTable();
//		void activateReadOnlyModeWithoutUpdate(AccessFlags flags);
//		void activateEditModeWithoutUpdate();
		void setAdditionalFormInfo(String formType, String taxType, String formKind,
									String departmentId, String reportPeriod, String state);
		void loadFormData(FormData formData, boolean readOnly);
		void setWorkflowButtons(List<WorkflowMove> moves);
		
		void showOriginalVersionButton(boolean show);
		void showSaveButton(boolean show);
		void showRecalculateButton(boolean show);
		void showAddRowButton(boolean show);
		void showRemoveRowButton(boolean show);
		void showPrintButton(boolean show);
		void showManualInputButton(boolean show);
		void showDeleteFormButton(boolean show);
		
		void removeSelectedTableRow();
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

	private FormData formData;
	private AccessFlags flags;

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
		final boolean readOnly = Boolean.parseBoolean(request.getParameter(READ_ONLY, "true"));
		
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
				if (!readOnly && result.getAccessFlags().getCanEdit()) {
					showEditModeButtons();
					getView().setWorkflowButtons(null);
				} else {
//					getView().activateReadOnlyModeWithoutUpdate(result.getAccessFlags());
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
				
				getView().loadFormData(result.getFormData(), readOnly);
//				getProxy().manualReveal(FormDataPresenter.this);
				
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
		return false;
	}

	
	@Override
	protected void onReset() {
		super.onReset();
		getView().cleanTable();
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
		final MyView view = getView();
		action.setFormData(view.getFormData());
		dispatcher.execute(action, new AbstractCallback<SaveFormDataResult>(){
			@Override
			public void onReqSuccess(SaveFormDataResult result) {
				getView().loadFormData(result.getFormData(), false);
				view.setLogMessages(result.getLogEntries());	
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
		FormData formData = getView().getFormData();
		formData.appendDataRow(null);
		getView().reloadRows();
	}
	
	@Override
	public void onRemoveRowClicked() {
		getView().removeSelectedTableRow();
		getView().reloadRows();
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
		action.setFormData(getView().getFormData());
		dispatcher.execute(action, new AbstractCallback<RecalculateFormDataResult>(){
			@Override
			public void onReqSuccess(RecalculateFormDataResult result) {
				getView().loadFormData(result.getFormData(), false);
				getView().setLogMessages(result.getLogEntries());
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
			action.setFormDataId(getView().getFormData().getId());
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
				FormDataPresenter.FORM_DATA_ID, getView().getFormData().getId().toString()));
	}

	@Override
	public void onWorkflowMove(WorkflowMove wfMove) {
		GoMoveAction action = new GoMoveAction();
		action.setFormDataId(getView().getFormData().getId());
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
