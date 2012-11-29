package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AccessFlags;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetAvailableMovesAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetAvailableMovesResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetNamesForIdAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetNamesForIdResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.SingleSelectionModel;
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
		FlowPanel getButtonPanel();
		DataGrid<DataRow> getFormDataTable();
		void loadFormData(FormData formData, AccessFlags flags);
		void reloadFormData(FormData formData, AccessFlags flags);
		void reloadRows();
		AccessFlags getFlags();
		FormData getFormData();
		void setLogMessages(List<LogEntry> logEntries);
		void reset();
		Boolean isReadOnly();
		void activateEditMode();
		void activateReadOnlyMode();
		void loadForm(FormData formData, AccessFlags flags);
		void activateReadOnlyMode(FormData data);
		void setAdditionalFormInfo(String formType, String taxType, String formKind,
									String departmentId, String reportPeriod, String state);
	}

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

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
		long formDataId = Long.parseLong(request.getParameter(FORM_DATA_ID, "none"));
		GetFormData action = new GetFormData();
		action.setFormDataId(formDataId);
		dispatcher.execute(action, new AbstractCallback<GetFormDataResult>() {
			@Override
			public void onSuccess(GetFormDataResult result) {
				getView().loadFormData(result.getFormData(), result.getAccessFlags());
				setAdditionalFormInfo();
				super.onSuccess(result);
			}
		});
	}
	
//	@Override
//	protected void onBind() {
//		super.onBind();
//		
//	}
	
	private void setAdditionalFormInfo() {
		
		GetNamesForIdAction action = new GetNamesForIdAction();
		action.setDepartmentId(getView().getFormData().getDepartmentId());
		action.setReportPeriodId(getView().getFormData().getReportPeriodId());
		dispatcher.execute(action, new AbstractCallback<GetNamesForIdResult>() {
			@Override
			public void onSuccess(GetNamesForIdResult result) {
				getView().setAdditionalFormInfo(
						getView().getFormData().getFormType().getName(),
						getView().getFormData().getFormType().getTaxType().getName(),
						getView().getFormData().getKind().getName(),
						result.getDepartmenName(),
						result.getReportPeriod(),
						getView().getFormData().getState().getName()
					);
				super.onSuccess(result);
			}
		});
		
	}

	@Override
	protected void onReset() {
		super.onReset();
		getView().reset();
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
			public void onSuccess(SaveFormDataResult result) {
//				view.activateReadOnlyMode(result.getFormData());
				view.reloadFormData(result.getFormData(), view.getFlags());
				view.setLogMessages(result.getLogEntries());	
				super.onSuccess(result);
			}

			@Override
			public void onFailure(Throwable throwable) {
				logger.log(Level.SEVERE, "Failed to save formData object", throwable);
				super.onFailure(throwable);
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
		FormData formData = getView().getFormData();
		// TODO need rework
		formData.getDataRows().remove(((SingleSelectionModel<DataRow>)getView().getFormDataTable().getSelectionModel()).getSelectedObject());
		getView().reloadRows();
	}
	
	//TODO need rework
	@Override
	public void onManualInputClicked() {
		final FlowPanel buttonPanel = getView().getButtonPanel();
		final MyView view = getView();
		GetAvailableMovesAction action = new GetAvailableMovesAction();
		action.setFormDataId(view.getFormData().getId());
		dispatcher.execute(action, new AbstractCallback<GetAvailableMovesResult>(){
			@Override
			public void onSuccess(GetAvailableMovesResult result) {
				for (final WorkflowMove move : result.getAvailableMoves()) {
					Button newButton = new Button();
					newButton.setText(move.getName());
					newButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							GoMoveAction action = new GoMoveAction();
							action.setFormDataId(view.getFormData().getId());
							action.setMove(move);
							dispatcher.execute(action, new AbstractCallback<GoMoveResult>(){
								@Override
								public void onSuccess(GoMoveResult result) {
									view.setLogMessages(result.getLogEntries());
									super.onSuccess(result);
								}
							});
						}
					});
					buttonPanel.add(newButton);
					
					super.onSuccess(result);
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				logger.log(Level.SEVERE, "Failed to get AvailableMoves object", throwable);
				super.onFailure(throwable);
			}							
		});
		
		view.activateEditMode();
	}
	
	@Override
	public void onOriginalVersionClicked() {
		getView().activateReadOnlyMode();
	}
	
	@Override
	public void onRecalculateClicked() {
		RecalculateFormDataAction action = new RecalculateFormDataAction();
		action.setFormData(getView().getFormData());
		dispatcher.execute(action, new AbstractCallback<RecalculateFormDataResult>(){
			@Override
			public void onSuccess(RecalculateFormDataResult result) {
				getView().reloadFormData(result.getFormData(), getView().getFlags());
				getView().setLogMessages(result.getLogEntries());
				super.onSuccess(result);
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
				public void onSuccess(DeleteFormDataResult result) {
					placeManager.revealPlace(new PlaceRequest(FormDataListNameTokens.FORM_DATA_LIST));
					super.onSuccess(result);
				}

				@Override
				public void onFailure(Throwable throwable) {
					logger.log(Level.SEVERE, "Failed to delete formData object", throwable);
					super.onFailure(throwable);
				}							
			});
		}
	}
}
