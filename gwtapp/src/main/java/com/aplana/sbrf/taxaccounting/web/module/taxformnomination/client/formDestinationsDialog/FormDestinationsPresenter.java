package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FieldsNamesService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FormDataElementName;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event.UpdateTable;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FormDestinationsPresenter extends PresenterWidget<FormDestinationsPresenter.MyView> implements FormDestinationsUiHandlers {

    private final DispatchAsync dispatchAsync;

    private TaxType taxType;

    public interface MyView extends PopupView, HasUiHandlers<FormDestinationsUiHandlers> {
        void setElementNames(Map<FormDataElementName, String> names);

        // установка данных в поле "подразделения"
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);
        // установка данных в поле "исполнители"
        void setPerformers(List<Department> performers, Set<Integer> availablePerformers);
        // подготовить форму для создания
        void prepareCreationForm();
        // показать модальную форму редактирования
        void prepareEditForm(List<FormTypeKind> formTypeKinds);
        // получить список поздазделений
        List<Integer> getDepartments();
         // получить список исполнителей
        List<Integer> getPerformers();
        // получить тип формы
        List<Long> getFormDataKind();
        // получить вид формы
        List<Long> getFormTypes();
        // установить фильтр для справочника
        void setFilterForFormTypes(String filter);
        // обновлели надписей в зависимости от вида налога
        void updateLabel(TaxType taxType);
        // фильтр типа налоговой формы
        void prepareFormDataKind(List<FormDataKind> types);
    }


    @Inject
    public FormDestinationsPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    /**
     * Первичная инициализация формы, вызывается извне используемым модулем
     */
    public void initForm(final TaxType taxType){
        GetDestanationPopupDataAction action = new GetDestanationPopupDataAction();
        this.taxType = taxType;

        dispatchAsync.execute(action,
            CallbackUtils.defaultCallback(
                new AbstractCallback<GetDestanationPopupDataResult>() {
                    @Override
                    public void onSuccess(GetDestanationPopupDataResult result) {
                        // установка данных в поле "подразделения"
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                        // установка данных в поле "исполнители"
                        getView().setPerformers(result.getPerformers(), result.getAvailablePerformers());
                        // установить фильтр для видов нф
                        getView().setFilterForFormTypes("TAX_TYPE LIKE '"+taxType.getCode()+"'");
                    }
                }, this));
    }

    @Override
    public void onConfirm() {
        AssignFormsAction action = new AssignFormsAction();
        action.setDepartments(getView().getDepartments());
        action.setFormDataKind(FormDataKind.fromId(getView().getFormDataKind().iterator().next().intValue()));
        action.setFormTypes(getView().getFormTypes());
        action.setPerformers(getView().getPerformers());

        LogCleanEvent.fire(this);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<AssignFormsResult>() {
                    @Override
                    public void onSuccess(AssignFormsResult result) {

                        if (result.isIssetRelations()){
                            LogAddEvent.fire(FormDestinationsPresenter.this, result.getUuid());
                            // показать сообщение
                            Dialog.warningMessage(
                                    "Предупреждение",
                                    "Часть назначений " + (taxType.isTax() ? "налоговых форм" : "форм") +" подразделениям была выполнена ранее.");
                        } else {
                            // Если в БД не было найдено ни одного сочетания, которое пытался создать пользователь (то есть ни разу не был выполнен сценарий 5А), Система выводит Диалог - сообщение:
                            Dialog.infoMessage(
                                    "Сообщение",
                                    "Назначения " + (taxType.isTax() ? "налоговых форм" : "форм") + " подразделениям выполнены успешно.");
                        }
                        getView().hide();
                        UpdateTable.fire(FormDestinationsPresenter.this, getView().getDepartments());
                    }
                }, this));
    }

    @Override
    public void onEdit(List<FormTypeKind> formTypeKinds){
        // Для каждого выбранного пользователем назначения, выполняется изменение / добавление исполнителя
        EditFormsAction action = new EditFormsAction();
        action.setFormTypeKinds(formTypeKinds);
        action.setPerformers(getView().getPerformers());

        dispatchAsync.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<EditFormResult>() {
                    @Override
                    public void onSuccess(EditFormResult result) {
                        getView().hide();
                        UpdateTable.fire(FormDestinationsPresenter.this);
                    }
                }, this));
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe, final TaxType taxType) {
        FillFormTypesAction action = new FillFormTypesAction();
        action.setTaxType(taxType);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<FillFormTypesResult>() {
                    @Override
                    public void onSuccess(FillFormTypesResult result) {
                        List<FormDataKind> dataKinds = new ArrayList<FormDataKind>();
                        dataKinds.addAll(result.getFormTypes());
                        getView().prepareFormDataKind(dataKinds);
                        getView().prepareCreationForm();
                        getView().updateLabel(result.getTaxType());
                        changeFilterElementNames(taxType);
                        slotForMe.addToPopupSlot(FormDestinationsPresenter.this);
                    }
                }, this));
    }

    public void initAndShowEditDialog(final HasPopupSlot slotForMe, List<FormTypeKind> formTypeKinds, TaxType taxType){
        getView().prepareEditForm(formTypeKinds);
        slotForMe.addToPopupSlot(FormDestinationsPresenter.this);
        changeFilterElementNames(taxType);
    }

    public void changeFilterElementNames(TaxType taxType) {
        getView().setElementNames(FieldsNamesService.get(taxType));
    }

}
