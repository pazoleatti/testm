package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListNameTokens;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FormDataPresenter extends
		FormDataPresenterBase<FormDataPresenter.MyProxy> implements
		FormDataUiHandlers {

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
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy, placeManager, dispatcher);
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			super.prepareFromRequest(request);
			readOnlyMode = Boolean.parseBoolean(request.getParameter(READ_ONLY,
					"true"));

			GetFormData action = new GetFormData();
			Integer wfId = Integer.parseInt(request.getParameter(WORK_FLOW_ID, "-1"));
			if (wfId != -1) {
				action.setWorkFlowMove(WorkflowMove.fromId(wfId));
			} else {
				action.setWorkFlowMove(null);
			}
			action.setFormDataId(Long.parseLong(request.getParameter(
					FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))));
			action.setDepartmentId(Integer.parseInt(request.getParameter(
					DEPARTMENT_ID, String.valueOf(Integer.MAX_VALUE))));
			action.setFormDataKind(Long.parseLong((request.getParameter(
					FORM_DATA_KIND_ID, String.valueOf(Long.MAX_VALUE)))));
			action.setFormDataTypeId((Long.parseLong((request.getParameter(
					FORM_DATA_TYPE_ID, String.valueOf(Long.MAX_VALUE))))));
			action.setReportPeriodId(Long.parseLong(request.getParameter(
					FORM_DATA_RPERIOD_ID, String.valueOf(Long.MAX_VALUE))));

			dispatcher.execute(action,
					new AbstractCallback<GetFormDataResult>() {
						@Override
						public void onReqSuccess(GetFormDataResult result) {
							formData = result.getFormData();
							accessParams = result.getFormDataAccessParams();
							if (!readOnlyMode && accessParams.isCanEdit()) {
								showEditModeButtons();
								getView().setWorkflowButtons(null);
							} else {
								showReadOnlyModeButtons();
								getView().setWorkflowButtons(
										accessParams
												.getAvailableWorkflowMoves());

							}
							getView().setLogMessages(result.getLogEntries());
							getView().setAdditionalFormInfo(
									result.getFormData().getFormType()
											.getName(),
									result.getFormData().getFormType()
											.getTaxType().getName(),
									result.getFormData().getKind().getName(),
									result.getDepartmenName(),
									result.getReportPeriod(),
									result.getFormData().getState().getName());

							getView().setColumnsData(formData.getFormColumns(),
									readOnlyMode);
							getView().setRowsData(formData.getDataRows());

							TitleUpdateEvent.fire(this,
									readOnlyMode ? "Налоговая форма"
											: "Редактирование налоговой формы",
									formData.getFormType().getName());


							List<LogEntry> le = result.getLogEntries();
							boolean hasError = false;
							for (LogEntry logEntry : le) {
								if (LogLevel.ERROR.equals(logEntry.getLevel())){
									hasError = true;
								}
							}

							if (hasError){
								getProxy().manualRevealFailed();
								MessageEvent.fire(FormDataPresenter.this, "Неудалось открыть/создать налоговую форму", result.getLogEntries());
							} else {
								getProxy().manualReveal(FormDataPresenter.this);
							}

							super.onReqSuccess(result);
						}
					});
		} catch (Exception e) {
			getProxy().manualRevealFailed();
			MessageEvent.fire(this, "Неудалось открыть/создать налоговую форму",
					e);
		}
	}

	@Override
	public void onManualInputClicked() {
		revealForm(false);
	}

	@Override
	public void onOriginalVersionClicked() {
		Window.alert("В разработке");
	}

	@Override
	public void onPrintClicked() {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadController/"
				+ formData.getId(), "", "");
		// Window.alert("В разработке");
	}

	@Override
	public void onCancelClicked() {
		if (readOnlyMode || (formData.getId() == null)) {
			goToFormDataList();
		} else {
			boolean isOK = Window
					.confirm("Вы уверены, что хотите прекратить редактирование данных налоговой формы?");
			if (isOK) {
				revealForm(true);
			}
		}
	}

	@Override
	public void onSaveClicked() {
		SaveFormDataAction action = new SaveFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, new AbstractCallback<SaveFormDataResult>() {
			@Override
			public void onReqSuccess(SaveFormDataResult result) {
				FormDataPresenter.this.formData = result.getFormData();
				getView().setLogMessages(result.getLogEntries());
				getView().setRowsData(
						FormDataPresenter.this.formData.getDataRows());
				super.onReqSuccess(result);
			}
		});

	}

	@Override
	public void onAddRowClicked() {
		formData.appendDataRow(null);
		getView().setRowsData(formData.getDataRows());
	}

	@Override
	public void onRemoveRowClicked(DataRow dataRow) {
		formData.getDataRows().remove(dataRow);
		getView().setRowsData(formData.getDataRows());
	}

	@Override
	public void onRecalculateClicked() {
		RecalculateFormDataAction action = new RecalculateFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action,
				new AbstractCallback<RecalculateFormDataResult>() {
					@Override
					public void onReqSuccess(RecalculateFormDataResult result) {
						formData = result.getFormData();
						getView().setRowsData(formData.getDataRows());
						getView().setLogMessages(result.getLogEntries());
						super.onReqSuccess(result);
					}
				});
	}

	@Override
	public void onDeleteFormClicked() {
		boolean isOK = Window.confirm("Удалить?");
		if (isOK) {
			DeleteFormDataAction action = new DeleteFormDataAction();
			action.setFormDataId(formData.getId());
			dispatcher.execute(action,
					new AbstractCallback<DeleteFormDataResult>() {
						@Override
						public void onReqSuccess(DeleteFormDataResult result) {
							goToFormDataList();
						}
					});
		}
	}

	@Override
	public void onWorkflowMove(WorkflowMove wfMove) {
		revealForm(true, wfMove.getId());
	}

	private void goToFormDataList() {
		placeManager.revealPlace(new PlaceRequest(
				FormDataListNameTokens.FORM_DATA_LIST).with("nType",
				String.valueOf(formData.getFormType().getTaxType())));
	}

}
