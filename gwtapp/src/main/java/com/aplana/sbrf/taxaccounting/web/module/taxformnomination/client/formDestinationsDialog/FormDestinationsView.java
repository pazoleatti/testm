package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class FormDestinationsView extends PopupViewWithUiHandlers<FormDestinationsUiHandlers>
        implements FormDestinationsPresenter.MyView,Editor<FormDataFilter> {

    public interface Binder extends UiBinder<PopupPanel, FormDestinationsView> {
	}

    private boolean isEditForm = false;

    @UiField
    RefBookPickerWidget formDataKind;

    @UiField
    RefBookPickerWidget formTypeId;

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
    private final static int REQUIDED_FIELDS_COUNT = 3;

	@Inject
	public FormDestinationsView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

        initWidget(uiBinder.createAndBindUi(this));
        formTypeId.setPeriodDates(null, new Date());
        formDataKind.setPeriodDates(null, new Date());
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
     * Проверка заполненности полей, при создании назначений
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
        if (formDataKind.getValue() == null || formDataKind.getValue().size() == 0){
            emptyFields.add("Тип налоговой формы");
        }

        // Вид налоговой формы
        if (formTypeId.getValue() == null || formTypeId.getValue().size() == 0){
            emptyFields.add("Вид налоговой формы");
        }

        return emptyFields;
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartment) {
        departmentPicker.setAvalibleValues(departments, availableDepartment);
    }

    @Override
    public void setPerformers(List<Department> performers, Set<Integer> availablePerformers) {
        performersPickerWidget.setAvalibleValues(performers, availablePerformers);
    }

    private void resetForm(){
        performersPickerWidget.setValue(null);
        departmentPicker.setValue(null);
        formTypeId.setValue(null, false);
        formDataKind.setValue(null, false);
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
        formTypeId.setEnabled(true);
        formTypeId.setValue(null, true);
        // тип налоговой формы
        formDataKind.setEnabled(true);
        formDataKind.setValue(null, false);
        // TODO нужно отключать мультиселект, сейчас это еще не предусмотрено
        //formDataKind.setM
    }

    @Override
    public List<Integer> getDepartments() {
        return departmentPicker.getValue();
    }

    @Override
    public Integer getPerformer(){
        // а больше одного и быть не может, так как мультиселект отключен
        return performersPickerWidget.getValue().size() == 1 ? performersPickerWidget.getValue().get(0) : null;
    }

    @Override
    public List<Long> getFormDataKind() {
        return formDataKind.getValue();
    }

    @Override
    public List<Long> getFormTypes() {
        return formTypeId.getValue();
    }

    @Override
    public void setFilterForFormTypes(String filter) {
        formTypeId.setFilter(filter);
    }

    @Override
    public void prepareEditForm(List<FormTypeKind> formTypeKinds){
        isEditForm = true;
        resetForm();
        // установка выбранных строк
        selectedDFT = formTypeKinds;

        List<Integer> departmens = new ArrayList<Integer>();
        Set<Long> kinds = new HashSet<Long>(formTypeKinds.size(), 1.1f);
        Set<Long> types = new HashSet<Long>(formTypeKinds.size(), 1.1f);
        for (FormTypeKind f: formTypeKinds){
            departmens.add(f.getDepartment().getId());
            kinds.add((long) f.getKind().getId());
            types.add(f.getFormTypeId());
        }
        // кнопки "создать" и "изменить"
        createButton.setVisible(false);
        editButton.setVisible(true);
        // Подразделение
        departmentPicker.setEnabled(false);
        departmentPicker.setValue(departmens);

        // Вид налоговой формы
        // TODO да пребудет время когда люди будут использвать List и Set по назначениею
        formTypeId.setValue(new ArrayList<Long>(types), true);
        formTypeId.setEnabled(false);


        // тип налоговой формы
        // TODO да пребудет время когда люди будут использвать List и Set по назначениею
        formDataKind.setValue(new ArrayList<Long>(kinds), true);
        formDataKind.setEnabled(false);

        // TODO нужно включать мультиселект, сейчас это еще не предусмотрено
        //formDataKind.setM
    }
}
