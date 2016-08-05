package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event.UpdateTable;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDeclarationPopUpFilterAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDeclarationPopUpFilterResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;
import java.util.Set;

/**
 * @author auldanov
 */
public class DeclarationDestinationsPresenter extends PresenterWidget<DeclarationDestinationsPresenter.MyView> implements DeclarationDestinationsUiHandlers {
    private final DispatchAsync dispatchAsync;
	TaxType taxType;

    @Override
    public void onConfirm() {
	    AddDeclarationSourceAction action = new AddDeclarationSourceAction();
	    action.setTaxType(taxType);
	    action.setDeclarationTypeId(getView().getSelectedDeclarationTypes());
	    action.setDepartmentId(getView().getSelectedDepartments());
	    dispatchAsync.execute(action,
			    CallbackUtils.defaultCallback(
					    new AbstractCallback<AddDeclarationSourceResult>() {
						    @Override
						    public void onSuccess(AddDeclarationSourceResult result) {
								UpdateTable.fire(DeclarationDestinationsPresenter.this, getView().getSelectedDepartments());
							    LogAddEvent.fire(DeclarationDestinationsPresenter.this, result.getUuid());
								getView().init();
								getView().hide();
						    }
					    }, this));
    }

	@Override
	public void onCancel() {
		if (getView().getSelectedDepartments().isEmpty() && getView().getSelectedDeclarationTypes().isEmpty()) {
			getView().hide();
		} else {
			Dialog.confirmMessage("Подтверждение закрытия формы", "Вы хотите отменить создание назначения?", new DialogHandler() {
				@Override
				public void yes() {
                    getView().hide();
				}
			});
		}

	}

	public interface MyView extends PopupView, HasUiHandlers<DeclarationDestinationsUiHandlers>{
		List<Integer> getSelectedDepartments();
	    List<Long> getSelectedDeclarationTypes();
	    void setDepartments(List<Department> departments, Set<Integer> availableValues);
        void setDeclarationTypeFilter(TaxType taxType);
        // обновлели надписей в зависимости от вида налога
        void updateLabel(TaxType taxType);

        void updateCreateButtonStatus();

		void init();
	}

    @Inject
    public DeclarationDestinationsPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe, TaxType taxType) {
	    this.taxType = taxType;
        slotForMe.addToPopupSlot(DeclarationDestinationsPresenter.this);
        getView().updateLabel(taxType);
        getView().updateCreateButtonStatus();
        getView().setDeclarationTypeFilter(taxType);

	    GetDeclarationPopUpFilterAction action = new GetDeclarationPopUpFilterAction();
	    action.setTaxType(taxType);
	    dispatchAsync.execute(action,
			    CallbackUtils.defaultCallback(
					    new AbstractCallback<GetDeclarationPopUpFilterResult>() {
						    @Override
						    public void onSuccess(GetDeclarationPopUpFilterResult result) {
								getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
						    }
					    }, this));

    }

}
