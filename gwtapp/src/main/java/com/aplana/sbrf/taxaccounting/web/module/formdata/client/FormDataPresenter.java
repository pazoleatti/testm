package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.ModelUtils;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.WrongInputDataServiceException;
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
			PlaceManager placeManager, DispatchAsync dispatcher, SignersPresenter signersPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, signersPresenter);
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			super.prepareFromRequest(request);

			final GetFormData action = new GetFormData();
			Integer wfId = ParamUtils.getInteger(request, WORK_FLOW_ID);
			action.setWorkFlowMove(wfId != null ? WorkflowMove.fromId(wfId)
					: null);
			// WTF? Почему Long.MAX_VALUE?
			action.setFormDataId(Long.parseLong(request.getParameter(
					FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))));
			action.setDepartmentId(ParamUtils
					.getInteger(request, DEPARTMENT_ID));
			action.setFormDataKind(ParamUtils.getLong(request,
					FORM_DATA_KIND_ID));
			action.setFormDataTypeId(ParamUtils.getLong(request,
					FORM_DATA_TYPE_ID));
			action.setReadOnly(Boolean.parseBoolean(request.getParameter(
					READ_ONLY, "true")));

			dispatcher.execute(action,
					new AbstractCallback<GetFormDataResult>() {
						@Override
						public void onReqSuccess(GetFormDataResult result) {
							
							if (ModelUtils.findByProperties(
									result.getLogEntries(),
									LogLevel.ERROR,
									new ModelUtils.GetPropertiesFunc<LogEntry, LogLevel>() {
										@Override
										public LogLevel getProperties(
												LogEntry object) {
											return object.getLevel();
										}
									}) != null) {
								if (!isVisible()) {
									MessageEvent
											.fire(FormDataPresenter.this,
													"Не удалось открыть/создать налоговую форму",
													result.getLogEntries());
								}
								getProxy().manualRevealFailed();
							} else {
								getProxy().manualReveal(FormDataPresenter.this);
							}
							
							formData = result.getFormData();
							formDataAccessParams = result.getFormDataAccessParams();

							switch (result.getFormMode()) {
							case READ_UNLOCKED:
								setReadUnlockedMode();
								break;
							case READ_LOCKED:
								setReadLockedMode(result.getLockedByUser(), result.getLockDate());
								break;
							case EDIT:
								setEditMode();
								break;
							}
							
							
							manageDeleteRowButtonEnabled();

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
							getView()
									.addCustomHeader(result.isNumberedHeader());
							getView().addCustomTableStyles(
									result.getAllStyles());
							
							TitleUpdateEvent.fire(this,
									readOnlyMode ? "Налоговая форма"
											: "Редактирование налоговой формы",
									formData.getFormType().getName());



							super.onReqSuccess(result);
						}

						@Override
						protected void onReqFailure(Throwable throwable) {
							getProxy().manualRevealFailed();
							if (throwable instanceof WrongInputDataServiceException) {
								MessageEvent.fire(this,
										"Не удалось создать налоговую форму: "
												+ throwable.getMessage());
							} else {
								MessageEvent
										.fire(this,
												"Не удалось открыть/создать налоговую форму",
												throwable);
							}
						}

						@Override
						protected boolean needErrorOnFailure() {
							return false;
						}
					});

		} catch (Exception e) {
			getProxy().manualRevealFailed();
			MessageEvent.fire(this,
					"Не удалось открыть/создать налоговую форму", e);
		}
	}

	@Override
	public void onSelectRow() {
		manageDeleteRowButtonEnabled();
	}

	@Override
	public void onShowCheckedColumns() {
		getView().setColumnsData(formData.getFormColumns(), readOnlyMode);
	}

	private void manageDeleteRowButtonEnabled() {
		if (!readOnlyMode) {
			MyView view = getView();
			DataRow dataRow = view.getSelectedRow();
			view.enableRemoveRowButton(dataRow != null
					&& !dataRow.isManagedByScripts());
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
				+ formData.getId() + "/" + getView().getCheckedColumnsClicked(), "", "");
	}

	@Override
	public void onSignersClicked() {
		signersPresenter.setFormData(formData);
		signersPresenter.setReadOnlyMode(readOnlyMode);
		addToPopupSlot(signersPresenter);
	}

	@Override
	public void onCancelClicked() {
		if (readOnlyMode || (formData.getId() == null)) {
			goToFormDataList();
		} else {
			boolean isOK = Window
					.confirm("Вы уверены, что хотите прекратить редактирование данных налоговой формы?");
			if (isOK) {
				unlockForm(formData.getId());
				revealForm(true);
			}
		}
	}

	@Override
	public void onSaveClicked() {
		SaveFormDataAction action = new SaveFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, new AbstractCallback<FormDataResult>() {
			@Override
			public void onReqSuccess(FormDataResult result) {
				processFormDataResult(result);
				super.onReqSuccess(result);
			}
		});

	}

	private void processFormDataResult(FormDataResult result) {
		formData = result.getFormData();
		getView().setLogMessages(result.getLogEntries());
		getView().setRowsData(formData.getDataRows());
	}

	@Override
	public void onAddRowClicked() {
		AddRowAction action = new AddRowAction();
		action.setFormData(formData);
		dispatcher.execute(action, new AbstractCallback<FormDataResult>() {
			@Override
			public void onReqSuccess(FormDataResult result) {
				processFormDataResult(result);
				super.onReqSuccess(result);
			}
		});
	}

	@Override
	public void onRemoveRowClicked() {
		DataRow dataRow = getView().getSelectedRow();
		if (dataRow != null && !dataRow.isManagedByScripts()) {
			formData.getDataRows().remove(dataRow);
			getView().setRowsData(formData.getDataRows());
		}
	}

	@Override
	public void onRecalculateClicked() {
		RecalculateFormDataAction action = new RecalculateFormDataAction();
		action.setFormData(formData);
		dispatcher.execute(action, new AbstractCallback<FormDataResult>() {
			@Override
			public void onReqSuccess(FormDataResult result) {
				processFormDataResult(result);
				super.onReqSuccess(result);
			}
		});
	}

	@Override
	public void onCheckClicked() {
		CheckFormDataAction checkAction = new CheckFormDataAction();
		checkAction.setFormData(formData);
		dispatcher.execute(checkAction, new AbstractCallback<FormDataResult>() {
			@Override
			protected void onReqSuccess(FormDataResult result) {
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
