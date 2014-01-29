package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.TaxFormNominationPresenter;
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
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;
import java.util.Set;

/**
 * @author auldanov
 */
public class DeclarationDestinationsPresenter extends PresenterWidget<DeclarationDestinationsPresenter.MyView> implements DeclarationDestinationsUiHandlers {
    private final PlaceManager placeManager;
    private final DispatchAsync dispatchAsync;
	TaxType taxType;

    @Override
    public void onConfirm() {
	    StringBuilder errorMsg = new StringBuilder("Не заполнены обязательные атрибуты, необходимые для создания назначения: ");
	    if (getView().getSelectedDepartments().isEmpty()) {
			errorMsg.append("Подразделение; ");
	    }
	    if (getView().getSelectedDeclarationTypes().isEmpty()) {
		    errorMsg.append("Вид декларации; ");
	    }
	    if (getView().getSelectedDepartments().isEmpty() || getView().getSelectedDeclarationTypes().isEmpty()) {
		    Dialog.errorMessage("Ошибка", errorMsg.toString());
		    return;
	    }

	    AddDeclarationSourceAction action = new AddDeclarationSourceAction();
	    action.setTaxType(taxType);
	    action.setDeclarationTypeId(getView().getSelectedDeclarationTypes());
	    action.setDepartmentId(getView().getSelectedDepartments());
	    dispatchAsync.execute(action,
			    CallbackUtils.defaultCallback(
					    new AbstractCallback<AddDeclarationSourceResult>() {
						    @Override
						    public void onSuccess(AddDeclarationSourceResult result) {
								UpdateTable.fire(DeclarationDestinationsPresenter.this);
							    getView().hide();
						    }
					    }, this));
    }

	@Override
	public void onCancel() {
		if (getView().getSelectedDepartments().isEmpty() && getView().getSelectedDeclarationTypes().isEmpty()) {
			getView().hide();
		} else {
			Dialog.confirmMessage("Подтверждение закрытия формы", "Сохранить изменения?", new DialogHandler() {
				@Override
				public void yes() {
					onConfirm();
				}
				@Override
				public void no() {
					getView().hide();
				}
			});
		}

	}

	public interface MyView extends PopupView, HasUiHandlers<DeclarationDestinationsUiHandlers>{
		List<Integer> getSelectedDepartments();
	    List<Integer> getSelectedDeclarationTypes();
	    void setDepartments(List<Department> departments, Set<Integer> availableValues);
	    void setDeclarationTypes(List<DeclarationType> declarationTypes);
    }

    @Inject
    public DeclarationDestinationsPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe, TaxType taxType) {
        //getView().resetForm();
	    this.taxType = taxType;
        slotForMe.addToPopupSlot(DeclarationDestinationsPresenter.this);

	    GetDeclarationPopUpFilterAction action = new GetDeclarationPopUpFilterAction();
	    action.setTaxType(taxType);
	    dispatchAsync.execute(action,
			    CallbackUtils.defaultCallback(
					    new AbstractCallback<GetDeclarationPopUpFilterResult>() {
						    @Override
						    public void onSuccess(GetDeclarationPopUpFilterResult result) {
								getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
							    getView().setDeclarationTypes(result.getDeclarationTypes());
						    }
					    }, this));

    }

}
