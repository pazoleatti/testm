package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
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
                        getView().setDepartment(formData.getPerformer() != null && formData.getPerformer().getPrintDepartmentId() != null ?
                                formData.getPerformer().getPrintDepartmentId() : formData.getDepartmentId());
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
}
