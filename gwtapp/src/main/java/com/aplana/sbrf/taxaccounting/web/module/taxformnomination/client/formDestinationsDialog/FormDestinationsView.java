package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class FormDestinationsView extends PopupViewWithUiHandlers<FormDestinationsUiHandlers>
        implements FormDestinationsPresenter.MyView,Editor<FormDataFilter> {

    public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы";
    public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы";
    public static final String FORM_DATA_TYPE_TITLE_D = "Вид формы";
    public static final String MODAL_WINDOW_TITLE = "Создание назначения налоговой формы";
    public static final String MODAL_WINDOW_TITLE_D = "Создание назначения формы";

    public interface Binder extends UiBinder<PopupPanel, FormDestinationsView> {
	}

    private boolean isEditForm = false;

    @UiField
    ModalWindow modalWindowTitle;

    @UiField
    @Ignore
    Label formDataKindLabel;

    @UiField
    @Ignore
    Label formTypeLabel;

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
        Date current = new Date();
        formTypeId.setPeriodDates(current, current);
        formDataKind.setPeriodDates(current, current);
        setButtonStatusUpdateHandlers();
	}

    @Override
    public void setElementNames(Map<FormDataElementName, String> names) {
        String app = ":";
        for (Map.Entry<FormDataElementName, String> name : names.entrySet()) {
            if (name.getValue() == null) {
                continue;
            }
            switch (name.getKey()) {
                case FORM_KIND_REFBOOK:
                    formDataKind.setTitle(name.getValue() + app);
                    break;
                case FORM_TYPE_REFBOOK:
                    formTypeId.setTitle(name.getValue() + app);
                    break;
                default:
                    break;
            }
        }
    }

    private void setButtonStatusUpdateHandlers() {
        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> listValueChangeEvent) {
                updateSaveEditButtonsStatus();
            }
        });

        formDataKind.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> listValueChangeEvent) {
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

        // Тип налоговой формы
        if (formDataKind.getValue() == null || formDataKind.getValue().isEmpty()){
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
        formDataKind.setValue(null, false);
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
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        DOM.setElementPropertyBoolean(formDataKind.getElement(), "disabled", false);
        // Вид налоговой формы
        formTypeId.setEnabled(true);
        formTypeId.setValue(null, true);
        // тип налоговой формы
        formDataKind.setEnabled(true);
        formDataKind.setValue(null, false);
        formDataKind.setMultiSelect(false);

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
        modalWindowTitle.setTitle("Форма редактирования назначения");
        // установка выбранных строк
        selectedDFT = formTypeKinds;

        List<Integer> departmens = new ArrayList<Integer>();
        Set<Long> kinds = new HashSet<Long>(formTypeKinds.size(), 1.1f);
        Set<Long> types = new HashSet<Long>(formTypeKinds.size(), 1.1f);
        Set<Integer> performers = new HashSet<Integer>();
        for (FormTypeKind f: formTypeKinds){
            departmens.add(f.getDepartment().getId());
            kinds.add((long) f.getKind().getId());
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


        // тип налоговой формы
        // TODO да пребудет время когда люди будут использвать List и Set по назначениею
        formDataKind.setMultiSelect(true);
        formDataKind.setValue(new ArrayList<Long>(kinds), true);
        formDataKind.setEnabled(false);

        performersPickerWidget.setValue(new ArrayList<Integer>(performers), true);

        updateSaveEditButtonsStatus();
    }

    @Override
    public void updateLabel(TaxType taxType) {
        if (!taxType.equals(TaxType.DEAL) && !taxType.equals(TaxType.ETR) && !taxType.equals(TaxType.MARKET)) {
            formDataKindLabel.setText(FORM_DATA_KIND_TITLE);
            formTypeLabel.setText(FORM_DATA_TYPE_TITLE);
            modalWindowTitle.setTitle(MODAL_WINDOW_TITLE);
        } else {
            formDataKindLabel.setText(FORM_DATA_KIND_TITLE_D);
            formTypeLabel.setText(FORM_DATA_TYPE_TITLE_D);
            modalWindowTitle.setTitle(MODAL_WINDOW_TITLE_D);
        }
    }

    @Override
    public void prepareFormDataKind(List<FormDataKind> dataKinds) {
        List<String> list = new ArrayList<String>(dataKinds.size());

        for (FormDataKind kind : dataKinds) {
            list.add("record_id = " + kind.getId());
        }
        formDataKind.setFilter(StringUtils.join(list.toArray(), " or ", null));
    }
}
