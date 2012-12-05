package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ErrorEvent;
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
			action.setFormDataId(Long.parseLong(request.getParameter(
					FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))));
			action.setDepartmentId(Long.parseLong(request.getParameter(
					DEPARTMENT_ID, String.valueOf(Long.MAX_VALUE))));
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
								getView().setWorkflowButtons(accessParams.getAvailableWorkflowMoves());

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
							getProxy().manualReveal(FormDataPresenter.this);

							super.onReqSuccess(result);
						}

					});
		} catch (Exception e) {
			ErrorEvent.fire(this, "Неудалось открыть/создать налоговую форму", e);
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
		Window.alert("В разработке");
	}

	@Override
	public void onCancelClicked() {
		placeManager.revealPlace(new PlaceRequest(
				FormDataListNameTokens.FORM_DATA_LIST).with("nType",
				String.valueOf(TaxType.TRANSPORT)));
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
							placeManager.revealPlace(new PlaceRequest(
									FormDataListNameTokens.FORM_DATA_LIST));
						}
					});
		}
	}

	@Override
	public void onWorkflowMove(WorkflowMove wfMove) {
		GoMoveAction action = new GoMoveAction();
		action.setFormDataId(formData.getId());
		action.setMove(wfMove);
		dispatcher.execute(action, new AbstractCallback<GoMoveResult>() {
			@Override
			public void onReqSuccess(GoMoveResult result) {
				getView().setLogMessages(result.getLogEntries());
				revealForm(true);
				super.onReqSuccess(result);
			}
		});
	}

}
