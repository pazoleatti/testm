package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetDepartmentTreeAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetDepartmentTreeResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;
import java.util.Set;

/**
 * Презентор "Исполнитель и подписанты"
 */

public class SignersPresenter extends PresenterWidget<SignersPresenter.MyView> implements SignersUiHandlers {
	private boolean readOnlyMode;
	private FormData formData;

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
		getView().setReadOnlyMode(readOnlyMode);
		getView().setPerformer(formData.getPerformer());
		getView().setSigners(formData.getSigners());

        GetDepartmentTreeAction action = new GetDepartmentTreeAction();
        action.setFormData(formData);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentTreeResult>() {
                    @Override
                    public void onSuccess(GetDepartmentTreeResult result) {
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                        Integer department = formData.getPerformer() != null && formData.getPerformer().getPrintDepartmentId() != null ?
                                formData.getPerformer().getPrintDepartmentId() : formData.getDepartmentId();
                        getView().setDepartment(department);
                        getView().setReportDepartmentName(getReportDepartmentName(result.getDepartments(), department));
                    }
                }, this));
	}

	@Override
	public void onSave(FormDataPerformer performer, List<FormDataSigner> signers) {
        formData.setPerformer(performer);
		formData.setSigners(signers);
		getView().hide();
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}

	public void setReadOnlyMode(boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
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
            if (parentDepartment != null && parentDepartment.getType() == DepartmentType.TERR_BANK) {
                return parentDepartment.getName() + "/" + reportDepartment.getName();
            } else {
                // иначе только конец
                return reportDepartment.getName();
            }
        }
        return null;
    }
}
