package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.*;
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

import java.util.List;

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
	public FormDataPresenter(
			EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher
	) {
		super(eventBus, view, proxy, placeManager, dispatcher);
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			super.prepareFromRequest(request);
			readOnlyMode = Boolean.parseBoolean(request.getParameter(READ_ONLY,	"true"));

			GetFormData action = new GetFormData();

			// Идентификатор жизненного цикла
			Integer wfId = Integer.parseInt(request.getParameter(WORK_FLOW_ID, "-1"));
			if (wfId != -1) {
				action.setWorkFlowMove(WorkflowMove.fromId(wfId));
			} else {
				action.setWorkFlowMove(null);
			}

			// WTF? Почему Long.MAX_VALUE?
			action.setFormDataId(Long.parseLong(request.getParameter(FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))));

			action.setDepartmentId(integerParameter(request, DEPARTMENT_ID));
			action.setFormDataKind(longParameter(request, FORM_DATA_KIND_ID));
			action.setFormDataTypeId(longParameter(request, FORM_DATA_TYPE_ID));
			action.setLockFormData(!readOnlyMode);

			dispatcher.execute(action,
					new AbstractCallback<GetFormDataResult>() {
						@Override
						public void onReqSuccess(GetFormDataResult result) {
							isFormDataLocked = result.isFormDataLocked();
							isLockedByCurrentUser = result.isLockedByCurrentUser();
							formData = result.getFormData();
							accessParams = result.getFormDataAccessParams();
							final boolean isLockModeEnabled = isFormDataLocked && !isLockedByCurrentUser;

							if (isLockModeEnabled){
								readOnlyMode = true;
							}

							if (!readOnlyMode && accessParams.isCanEdit()) {
								//Открываем форму в режиме редактирования
								showEditModeButtons();
								getView().setWorkflowButtons(null);
							} else {
								//Открываем форму в режиме чтения
								if(isLockModeEnabled){
									//Если другой пользователь уже открыл данную форму в режиме редактирования (залочил ее),
									//то выводим сообщение пользователю и "сокращаем" список возможных действий в этом режиме
									setFormDataLockedMode(true, result.getLockedByUser(), result.getLockDate());
								} else {
									setFormDataLockedMode(false, null, null);
								}
								showReadOnlyModeButtons(isLockModeEnabled);
								getView().setWorkflowButtons(accessParams.getAvailableWorkflowMoves());
							}
							manageDeleteRowButtonEnabled();

							getView().setLogMessages(result.getLogEntries());
							getView().setAdditionalFormInfo(
									result.getFormData().getFormType().getName(),
									result.getFormData().getFormType().getTaxType().getName(),
									result.getFormData().getKind().getName(),
									result.getDepartmenName(),
									result.getReportPeriod(),
									result.getFormData().getState().getName()
							);

							getView().setColumnsData(formData.getFormColumns(), readOnlyMode);
							getView().setRowsData(formData.getDataRows());
							getView().addCustomHeader(result.isNumberedHeader());
							getView().addCustomTableStyles(result.getAllStyles());
							TitleUpdateEvent.fire(
									this,
									readOnlyMode ? "Налоговая форма" : "Редактирование налоговой формы",
									formData.getFormType().getName()
							);


							List<LogEntry> le = result.getLogEntries();
							boolean hasError = false;
							for (LogEntry logEntry : le) {
								if (LogLevel.ERROR.equals(logEntry.getLevel())) {
									hasError = true;
									break;
								}
							}

							if (hasError) {
								if (!isVisible()) {
									MessageEvent.fire(
											FormDataPresenter.this,
											"Не удалось открыть/создать налоговую форму",
											result.getLogEntries()
									);
								}
								getProxy().manualRevealFailed();
							} else {
								getProxy().manualReveal(FormDataPresenter.this);
							}

							super.onReqSuccess(result);
						}

						@Override
						protected void onReqFailure(Throwable throwable) {
							try {
								throw throwable;
							} catch (WrongInputDataServiceException exception) {
								getProxy().manualRevealFailed();
								MessageEvent.fire(this, "Не удалось создать налоговую форму: " + exception.getMessage());
							} catch (Throwable exception) {
								getProxy().manualRevealFailed();
								MessageEvent.fire(this, "Не удалось открыть/создать налоговую форму", exception);
							}
						}

						@Override
						protected boolean needErrorOnFailure() {
							return false;
						}
					});
		} catch (Exception e) {
			getProxy().manualRevealFailed();
			MessageEvent.fire(this, "Не удалось открыть/создать налоговую форму", e);
		}
	}

	private Integer integerParameter(PlaceRequest request, String name) {
		String rawParam;
		rawParam = request.getParameter(name, "null");
		Integer value;
		if (rawParam.equals("") || rawParam.equals("null")) {
			value=null;
		} else {
			value = Integer.parseInt(rawParam);
		}
		return value;
	}

	private Long longParameter(PlaceRequest request, String name) {
		String rawParam;
		rawParam = request.getParameter(name, "null");
		return (rawParam.equals("") || rawParam.equals("null")) ? null : Long.parseLong(rawParam);
	}

	@Override
	public void onSelectRow() {
		manageDeleteRowButtonEnabled();
	}

	private void manageDeleteRowButtonEnabled() {
		if (!readOnlyMode) {
			MyView view = getView();
			DataRow dataRow = view.getSelectedRow();
			view.enableRemoveRowButton(dataRow != null && !dataRow.isManagedByScripts());
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
		Window.open(GWT.getHostPageBaseURL() + "download/downloadController/" + formData.getId(), "", "");
		// Window.alert("В разработке");
	}

	@Override
	public void onCancelClicked() {
		if (readOnlyMode || (formData.getId() == null)) {
			goToFormDataList();
		} else {
			boolean isOK = Window.confirm("Вы уверены, что хотите прекратить редактирование данных налоговой формы?");
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
		dispatcher.execute(
				action,
				new AbstractCallback<FormDataResult>() {
					@Override
					public void onReqSuccess(FormDataResult result) {
						processFormDataResult(result);
						super.onReqSuccess(result);
					}
				}
		);
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
		dispatcher.execute(
				action,
				new AbstractCallback<FormDataResult>() {
					@Override
					public void onReqSuccess(FormDataResult result) {
						processFormDataResult(result);
						super.onReqSuccess(result);
					}
				}
		);
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
