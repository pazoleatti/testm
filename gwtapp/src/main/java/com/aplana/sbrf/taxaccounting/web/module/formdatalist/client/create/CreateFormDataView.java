package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class CreateFormDataView extends PopupViewWithUiHandlers<CreateFormDataUiHandlers> implements CreateFormDataPresenter.MyView,
        Editor<FormDataFilter> {

    public interface Binder extends UiBinder<PopupPanel, CreateFormDataView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, CreateFormDataView> {
    }

    private final MyDriver driver;

    @UiField
    @Path("departmentIds")
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    RefBookPickerWidget formDataKind;

    @UiField
    RefBookPickerWidget formTypeId;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @Inject
    public CreateFormDataView(Binder uiBinder, final MyDriver driver, EventBus eventBus) {
        super(eventBus);

        initWidget(uiBinder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);

        // Нельзя аннотацией, баг GWT https://code.google.com/p/google-web-toolkit/issues/detail?id=6091
//        formTypeId.addValueChangeHandler(new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<Integer> event) {
//                updateEnabled();
//            }
//        });
//
//        formDataKind.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<List<Long>> event) {
//                if (formDataKind.getValue() == null || formDataKind.getValue().isEmpty()){
//                    formTypeId.setValue(null);
//                    updateEnabled();
//                    return;
//                }
//                if (getUiHandlers() != null){
//                    getUiHandlers().onFormKindChange();
//                    updateEnabled();
//                }
//            }
//        });

        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
        formTypeId.setPeriodDates(new Date(), new Date());
    }

    @Override
    public void init() {
        // Сброс состояния формы
        reportPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        formDataKind.setValue(new ArrayList<Long>(), true);
        formDataKind.setDereferenceValue(null);
        formTypeId.setValue(new ArrayList<Long>(), true);
        formTypeId.setDereferenceValue(null);
        updateEnabled();
    }

    private void updateEnabled() {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        formDataKind.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // "Вид налоговой формы" недоступен если не выбран тип НФ
        formTypeId.setEnabled(formDataKind.getValue() != null && !formDataKind.getValue().isEmpty());
        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(formTypeId.getValue() != null && !formTypeId.getValue().isEmpty());
    }


    @UiHandler("reportPeriodIds")
    public void onReportPeriodChange(ValueChangeEvent<List<Integer>> event) {
        departmentPicker.setValue(null, true);
        getUiHandlers().onReportPeriodChange();
    }

    @UiHandler("departmentPicker")
    public void onDepartmentChange(ValueChangeEvent<List<Integer>> event) {
        formDataKind.setValue(new ArrayList<Long>(), true);
        formDataKind.setDereferenceValue(null);
	    getUiHandlers().onDepartmentChanged();
        updateEnabled();
    }

    @UiHandler("formDataKind")
    public void onDataKindChange(ValueChangeEvent<List<Long>> event) {
        formTypeId.setValue(new ArrayList<Long>(), true);
        formTypeId.setDereferenceValue(null);
        if ((formDataKind.getValue() != null) && !formDataKind.getValue().isEmpty()) {
            getUiHandlers().onDataKindChanged();
        }
        updateEnabled();
    }

    @UiHandler("formTypeId")
    public void onFormTypeIdChange(ValueChangeEvent<List<Long>> event) {
        updateEnabled();
    }

    @UiHandler("continueButton")
    public void onSave(ClickEvent event) {
        getUiHandlers().onConfirm();
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        Dialog.confirmMessage("Создание налоговой формы", "Хотите отменить создание налоговой формы?", new DialogHandler() {
            @Override
            public void yes() {
                Dialog.hideMessage();
                hide();
            }
        });
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

	@Override
	public void setAcceptableKinds(List<FormDataKind> dataKinds) {
        if (dataKinds == null) {
            formDataKind.setFilter(null);
            return;
        }
        if (dataKinds.isEmpty()) {
            formDataKind.setFilter("");
            return;
        }
		StringBuilder filter = new StringBuilder();
		for (FormDataKind k : dataKinds) {
			filter.append(k.getId() + ",");
		}
		filter.deleteCharAt(filter.length()-1);
		formDataKind.setFilter(filter.toString());
	}

	@Override
	public void setAcceptableTypes(List<FormType> types) {
        if (types == null) {
            formTypeId.setFilter(null);
            return;
        }
        if (types.isEmpty()) {
            formTypeId.setFilter(RefBook.RECORD_ID_ALIAS + "=-1");
            return;
        }
		StringBuilder str = new StringBuilder();
		for (FormType ft : types) {
			str.append(RefBook.RECORD_ID_ALIAS + "=" + ft.getId() + " or ");
		}
		str.delete(str.length()-3, str.length()-1);
		formTypeId.setFilter(str.toString());

    }

    @Override
    public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentPicker.setAvalibleValues(list, availableValues);
    }

    @Override
    public void setFilterData(FormDataFilter filter) {
        driver.edit(filter);
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //departmentPicker.setValue(filter.getDepartmentIds());
    }

    @Override
    public FormDataFilter getFilterData() {
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //filter.setDepartmentIds(departmentPicker.getValue());
        return driver.flush();
    }

    @Override
    public void setFilter(String filter) {
        formTypeId.setFilter(filter);
    }

}
