package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDestanationPopupDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDestanationPopupDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;
import java.util.Set;


public class FormDestinationsPresenter extends PresenterWidget<FormDestinationsPresenter.MyView> implements FormDestinationsUiHandlers {

    private final PlaceManager placeManager;
    private final DispatchAsync dispatchAsync;

    public interface MyView extends PopupView, HasUiHandlers<FormDestinationsUiHandlers> {
        // установка данных в поле "подразделения"
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);
        // загрузка данных для поля "Тип налоговой формы"
        void setFormDataKinds(List<FormDataKind> formDataKinds);
        // установить данные в выпадающий список "Вип налоговой формы"
        void setFormTypesMap(List<FormType> formTypes);
        // установка данных в поле "исполнители"
        void setPerformers(List<Department> performers, Set<Integer> availablePerformers);
        // сброс формы, перед показом
        void resetForm();
    }


    @Inject
    public FormDestinationsPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    /**
     * Первичная инициализация формы, вызывается извне используемым модулем
     */
    public void initForm(TaxType taxType){
        GetDestanationPopupDataAction action = new GetDestanationPopupDataAction();
        action.setTaxType(taxType);

        dispatchAsync.execute(action,
            CallbackUtils.defaultCallback(
                new AbstractCallback<GetDestanationPopupDataResult>() {
                    @Override
                    public void onSuccess(GetDestanationPopupDataResult result) {
                        // установка данных в поле "подразделения"
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                        // загрузка данных для поля "Тип налоговой формы"
                        getView().setFormDataKinds(result.getFormDataKinds());
                        // загрузка данных для поля "Вип налоговой формы"
                        getView().setFormTypesMap(result.getFormTypes());
                        // установка данных в поле "исполнители"
                        getView().setPerformers(result.getPerformers(), result.getAvailablePerformers());
                    }
                }, this));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    public void onConfirm() {
        //TODO логика
        getView().hide();
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe) {
        getView().resetForm();
        slotForMe.addToPopupSlot(FormDestinationsPresenter.this);
        //TODO логика загрузки данных

    }

}
