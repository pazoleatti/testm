package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FormDataElementName;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class DeclarationDestinationsView extends PopupViewWithUiHandlers<DeclarationDestinationsUiHandlers>
        implements DeclarationDestinationsPresenter.MyView,Editor<FormDataFilter> {

    public static final String MODAL_WINDOW_TITLE = "Создание назначения налоговой формы";

    public interface Binder extends UiBinder<PopupPanel, DeclarationDestinationsView> {
	}

    private boolean isEditForm = false;

    @UiField
    ModalWindow modalWindowTitle;

    @UiField
    @Ignore
    Label formTypeLabel;

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

    /**
     * Список id назначений (department_form_type) выделенные на форме
     * при открытии формы редактирования данных, теряется четкое сопостовление
     * типов, видов, и департаментов
     */
    private List<FormTypeKind> selectedDFT;

    // количество полей обязательных для заполнения
    private final static int REQUIDED_FIELDS_COUNT = 2;

	@Inject
	public DeclarationDestinationsView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

        initWidget(uiBinder.createAndBindUi(this));
        Date current = new Date();
        formTypeId.setPeriodDates(current, current);
        setButtonStatusUpdateHandlers();
	}

    @Override
    public void setElementNames(Map<FormDataElementName, String> names) {
    }

    private void setButtonStatusUpdateHandlers() {
        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> listValueChangeEvent) {
                updateSaveEditButtonsStatus();
            }
        });

        formTypeId.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> listValueChangeEvent) {
                updateSaveEditButtonsStatus();
            }
        });

        performersPickerWidget.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> listValueChangeEvent) {
                updateSaveEditButtonsStatus();
            }
        });
    }

    @UiHandler("createButton")
	public void onSave(ClickEvent event){
        /**
         * проверка заполненности полей нет, так
         * как кнопка доступна только в случае если все поля заполнены
         */
        getUiHandlers().onConfirm();
	}

    @UiHandler("editButton")
    public void onEdit(ClickEvent event){
        /**
         * проверка заполненности полей нет, так
         * как кнопка доступна только в случае если все поля заполнены
         */
        getUiHandlers().onEdit(selectedDFT);
    }



	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
        if (isEditForm){
            Dialog.confirmMessage("Подтверждение закрытия формы",
                    "Вы хотите отменить редактирование назначения?",
                    new DialogHandler() {
                @Override
                public void yes() {
                    super.yes();
                    hide();
                }
            });
        } else{
            /*
            * Если хотя бы одно значение было установлено пользователем: Система выводит Диалог - вопрос:
            *
            */
            if (getFieldsCount() != REQUIDED_FIELDS_COUNT){
                Dialog.confirmMessage("Подтверждение закрытия формы", "Вы хотите отменить создание назначения?", new DialogHandler() {
                    @Override
                    public void yes() {
                        hide();
                        super.yes();
                    }
                });
            } else {
                // Если ни одно значение не было установлено пользователем в элементах формы: Система закрывает форму создания назначения
                hide();
            }
        }

	}

    /**
     * Обновить доступность кнопок Редактировать и Создать
     */
    private void updateSaveEditButtonsStatus(){
        if (getFieldsCount() == 0){
            if (isEditForm){
                editButton.setEnabled(true);
            } else {
                createButton.setEnabled(true);
            }
        } else {
            if (isEditForm){
                editButton.setEnabled(false);
            } else {
                createButton.setEnabled(false);
            }
        }
    }

    private int getFieldsCount(){
        int cnt = 0;
        // Подразделение
        if (departmentPicker.getValue().size() == 0){
            cnt++;
        }

        // Вид налоговой формы
        if (formTypeId.getValue() == null || formTypeId.getValue().isEmpty()){
            cnt++;
        }

        return cnt;
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
    }

    @Override
    public void prepareCreationForm(){
        isEditForm = false;
        resetForm();
        modalWindowTitle.setTitle("Форма создания назначения");
        // кнопки "создать" и "изменить"
        createButton.setVisible(true);
        editButton.setVisible(false);
        // Подразделение
        departmentPicker.setEnabled(true);
        // Вид налоговой формы
        formTypeId.setEnabled(true);
        formTypeId.setValue(null, true);

        updateSaveEditButtonsStatus();
    }

    @Override
    public List<Integer> getDepartments() {
        return departmentPicker.getValue();
    }

    @Override
    public List<Integer> getPerformers(){
        return performersPickerWidget.getValue();
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
        modalWindowTitle.setTitle("Форма редактирования назначения");
        // установка выбранных строк
        selectedDFT = formTypeKinds;

        List<Integer> departmens = new ArrayList<Integer>();
        Set<Long> types = new HashSet<Long>(formTypeKinds.size(), 1.1f);
        Set<Integer> performers = new HashSet<Integer>();
        for (FormTypeKind f: formTypeKinds){
            departmens.add(f.getDepartment().getId());
            types.add(f.getFormTypeId());
            if (f.getPerformers() != null && !f.getPerformers().isEmpty()) {
                for (Department performer : f.getPerformers()) {
                    performers.add(performer.getId());
                }
            }
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

        performersPickerWidget.setValue(new ArrayList<Integer>(performers), true);

        updateSaveEditButtonsStatus();
    }

    @Override
    public void updateLabel(TaxType taxType) {
        modalWindowTitle.setTitle(MODAL_WINDOW_TITLE);
    }
}
