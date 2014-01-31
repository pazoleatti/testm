package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class FormDestinationsView extends PopupViewWithUiHandlers<FormDestinationsUiHandlers>
        implements FormDestinationsPresenter.MyView,Editor<FormDataFilter> {

    public interface Binder extends UiBinder<PopupPanel, FormDestinationsView> {
	}

    private boolean isEditForm = false;

    @UiField(provided = true)
    ValueListBox<FormDataKind> formDataKind;

    @UiField(provided = true)
    ValueListBox<Integer> formTypeId;

	@UiField
	Button createButton;

    @UiField
    Button editButton;

	@UiField
	Button cancelButton;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    DepartmentPickerPopupWidget performersPickerWidget;

    private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

    /**
     * Список id назначений (department_form_type) выделенные на форме
     * при открытии формы редактирования данных, теряется четкое сопостовление
     * типов, видов, и департаментов
     */
    private List<FormTypeKind> selectedDFT;

    // количество полей обязательных для заполнения
    private final static int REQUIDED_FIELDS_COUNT = 4;

	@Inject
	public FormDestinationsView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

        formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        formTypeId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return formTypesMap.get(object);
            }
        });

		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("createButton")
	public void onSave(ClickEvent event){
        // проверка заполненности полей
        if (!checkRequiredFields()){
            Dialog.errorMessage(
                    "Ошибка",
                    "Не заполнены обязательные атрибуты, необходимые для создания назначения: " + StringUtils.join(getEmptyFieldsNames().toArray(), ',')
            );
        } else {
            getUiHandlers().onConfirm();
        }
	}

    @UiHandler("editButton")
    public void onEdit(ClickEvent event){
        if (!checkRequiredFields()){
            Dialog.errorMessage(
                    "Ошибка",
                    "Не заполнены обязательные атрибуты, необходимые для создания назначения: " + StringUtils.join(getEmptyFieldsNames().toArray(), ',')
            );
        } else {
            getUiHandlers().onEdit(selectedDFT);
        }
    }



	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
        if (isEditForm){
            Dialog.confirmMessage("Подтверждение закрытия формы",
                    "Сохранить изменения?",
                    new DialogHandler() {
                @Override
                public void yes() {
                    onEdit(null);
                    super.yes();
                }

                @Override
                public void no() {
                    super.no();
                    hide();
                }

                @Override
                public void close() {
                    super.close();
                    hide();
                }
            });
        } else{
            /*
            * Если хотя бы одно значение было установлено пользователем: Система выводит Диалог - вопрос:
            *
            */
            if (getEmptyFieldsNames().size() != REQUIDED_FIELDS_COUNT){
                Dialog.confirmMessage("Подтверждение закрытия формы", "Сохранить изменения?", new DialogHandler() {
                    @Override
                    public void yes() {
                        // Выполнение события "Нажатие на кнопку "Создать"
                        onSave(null);
                        super.yes();
                    }

                    @Override
                    public void no() {
                        super.no();
                        hide();
                    }

                    @Override
                    public void close() {
                        super.close();
                        hide();
                    }
                });
            } else {
                // Если ни одно значение не было установлено пользователем в элементах формы: Система закрывает форму создания назначения
                hide();
            }
        }

	}

    /**
     * Проверка заполненности полей
     * @return true - если заполнены все требуемые поля, false в обратном случае
     */
    private boolean checkRequiredFields(){
        return getEmptyFieldsNames().size() == 0;
    }

    private List<String> getEmptyFieldsNames(){
        List<String> emptyFields = new ArrayList<String>();
        // Подразделение
        if (departmentPicker.getValue().size() == 0){
            emptyFields.add("Подразделение");
        }

        // Тип налоговой формы
        if (formDataKind.getValue() == null){
            emptyFields.add("Тип налоговой формы");
        }

        // Вид налоговой формы
        if (formTypeId.getValue() == null){
            emptyFields.add("Вид налоговой формы");
        }

        // Исполнитель
        if (performersPickerWidget.getValue().size() == 0){
            emptyFields.add("Исполнитель");
        }

        return emptyFields;
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartment) {
        departmentPicker.setAvalibleValues(departments, availableDepartment);
    }

    @Override
    public void setFormDataKinds(List<FormDataKind> formDataKinds) {
        formDataKind.setAcceptableValues(formDataKinds);
    }

    @Override
    public void setFormTypesMap(List<FormType> formTypes){
        formTypesMap.clear();
        for (FormType formType : formTypes) {
            formTypesMap.put(formType.getId(), formType.getName());
        }

        formTypeId.setValue(null);
        formTypeId.setAcceptableValues(formTypesMap.keySet());
    }

    @Override
    public void setPerformers(List<Department> performers, Set<Integer> availablePerformers) {
        performersPickerWidget.setAvalibleValues(performers, availablePerformers);
    }

    private void resetForm(){
        performersPickerWidget.setValue(null);
        departmentPicker.setValue(null);
        formTypeId.setValue(null);
        formDataKind.setValue(null);
    }

    @Override
    public void prepareCreationForm(){
        isEditForm = false;
        resetForm();
        // кнопки "создать" и "изменить"
        createButton.setVisible(true);
        editButton.setVisible(false);
        // Подразделение
        departmentPicker.setEnabled(true);
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        DOM.setElementPropertyBoolean(formDataKind.getElement(), "disabled", false);
        // Вид налоговой формы
        DOM.setElementPropertyBoolean(formTypeId.getElement(), "disabled", false);
    }

    @Override
    public List<Integer> getDepartments() {
        return departmentPicker.getValue();
    }

    @Override
    public Integer getPerformer(){
        return performersPickerWidget.getValue().get(0);
    }

    @Override
    public FormDataKind getFormDataKind() {
        return formDataKind.getValue();
    }

    @Override
    public List<Integer> getFormTypes() {
        // TODO ждду виджет с мульттиселектом
        return new ArrayList(formTypeId.getValue());
    }

    @Override
    public void prepareEditForm(List<FormTypeKind> formTypeKinds){
        isEditForm = true;
        resetForm();
        // установка выбранных строк
        selectedDFT = formTypeKinds;

        List<Integer> departmens = new ArrayList<Integer>();
        List<FormDataKind> kinds = new ArrayList<FormDataKind>();
        List<Integer> types = new ArrayList<Integer>();
        for (FormTypeKind f: formTypeKinds){
            departmens.add(f.getDepartment().getId());
            kinds.add(f.getKind());
            types.add(f.getFormTypeId().intValue());
        }
        // кнопки "создать" и "изменить"
        createButton.setVisible(false);
        editButton.setVisible(true);
        // Подразделение
        departmentPicker.setEnabled(false);
        departmentPicker.setValue(departmens);

        // "Тип налоговой формы" недоступен если не выбрано подразделение
        DOM.setElementPropertyBoolean(formDataKind.getElement(), "disabled", true);
        // TODO установить множественно
        formDataKind.setValue(kinds.iterator().next());

        // Вид налоговой формы
        DOM.setElementPropertyBoolean(formTypeId.getElement(), "disabled", true);
        formTypeId.setValue(types.iterator().next());
    }
}
