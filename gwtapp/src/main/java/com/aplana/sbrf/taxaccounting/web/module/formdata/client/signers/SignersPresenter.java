package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;
import java.util.Set;

/**
 * Презентор "Параметры печатной формы"
 */

public class SignersPresenter extends PresenterWidget<SignersPresenter.MyView> implements SignersUiHandlers {
	private boolean creteLock;
    private boolean readOnlyMode;
    private FormData formData;
    private HandlerRegistration closeFormDataHandlerRegistration;

    private final DispatchAsync dispatcher;

	public interface MyView extends PopupView, HasUiHandlers<SignersUiHandlers> {
		void setPerformer(FormDataPerformer performer);
		void setSigners(List<FormDataSigner> signers);
		void setReadOnlyMode(boolean readOnlyMode);
        void setDepartments(List<Department> departments, Set<Integer> availableDepartments);
        void setDepartment(Integer department);
        void setReportDepartmentName(String department);
    }

	@Inject
	public SignersPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
		super(eventBus, view);
        this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
        GetPerformerAction action = new GetPerformerAction();
        action.setFormData(formData);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetPerformerResult>() {
                    @Override
                    public void onSuccess(GetPerformerResult result) {
                        FormData formData1 = result.getFormData();
                        LogAddEvent.fire(SignersPresenter.this, result.getUuid());
                        readOnlyMode = result.isReadOnlyMode();
                        creteLock = result.isCreteLock();
                        getView().setPerformer(formData1.getPerformer());
                        getView().setSigners(formData1.getSigners());
                        getView().setReadOnlyMode(SignersPresenter.this.readOnlyMode);
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                        Integer department = formData1.getPerformer() != null && formData1.getPerformer().getPrintDepartmentId() != null ?
                                formData1.getPerformer().getPrintDepartmentId() : formData1.getDepartmentId();
                        getView().setDepartment(department);
                        String reportDepartmentName = getReportDepartmentName(result.getDepartments(), department);
                        getView().setReportDepartmentName(reportDepartmentName);

                        closeFormDataHandlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
                            @Override
                            public void onClose(CloseEvent<Window> event) {
                                closeFormDataHandlerRegistration.removeHandler();
                                unlockForm();
                            }
                        });
                    }
                }, this));
	}

	@Override
	public void onSave(FormDataPerformer performer, List<FormDataSigner> signers) {
        formData.setPerformer(performer);
		formData.setSigners(signers);
        SavePerformerAction action = new SavePerformerAction();
        action.setFormData(formData);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<SavePerformerResult>() {
                    @Override
                    public void onSuccess(SavePerformerResult result) {
                        if (result.getUuid() == null) {
                            getView().hide();
                        } else {
                            LogAddEvent.fire(SignersPresenter.this, result.getUuid());
                        }
                    }
                }, this));
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}

    private String getReportDepartmentName(List<Department> departments, Integer department) {
        Department reportDepartment = null;
        // ищем подразделение по id
        for (Department dep : departments) {
            if (dep.getId() == department){
                reportDepartment = dep;
                break;
            }
        }
        if (reportDepartment != null) {
            // рекурсивно проходим по родителям пока не упремся в корень, ТБ или никуда
            Integer parentId = reportDepartment.getParentId();
            Department parentDepartment = reportDepartment;
            while (parentDepartment != null && parentDepartment.getType() != DepartmentType.ROOT_BANK && parentDepartment.getType() != DepartmentType.TERR_BANK) {
                parentDepartment = null;
                for (Department dep : departments) {
                    if (dep.getId() == parentId){
                        parentDepartment = dep;
                        break;
                    }
                }
                if (parentDepartment != null) {
                    parentId = parentDepartment.getParentId();
                }
            }
            // если уперлись в ТБ, то выводим составное имя
            if (parentDepartment != null && reportDepartment.getType() != DepartmentType.TERR_BANK && parentDepartment.getType() == DepartmentType.TERR_BANK) {
                return parentDepartment.getName() + "/" + reportDepartment.getName();
            } else {
                // иначе только конец
                return reportDepartment.getName();
            }
        }
        return null;
    }

    @Override
    public void onHide() {
        super.onHide();
        closeFormDataHandlerRegistration.removeHandler();
        unlockForm();
    }

    private void unlockForm() {
        if (!readOnlyMode && creteLock) {
            UnlockFormData action = new UnlockFormData();
            action.setFormId(formData.getId());
            dispatcher.execute(action, CallbackUtils.emptyCallback());
        }
    }
}
