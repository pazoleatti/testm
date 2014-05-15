package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.ValueListBox;
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
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CreateFormDataView extends PopupViewWithUiHandlers<CreateFormDataUiHandlers> implements CreateFormDataPresenter.MyView,
        Editor<FormDataFilter> {

    public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы";
    public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы";
    public static final String FORM_DATA_TYPE_TITLE_D = "Вид формы";
    public static final String FORM_DATA_TITLE = "Создание налоговой формы";
    public static final String FORM_DATA_TITLE_D = "Создание формы";

    public interface Binder extends UiBinder<PopupPanel, CreateFormDataView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, CreateFormDataView> {
    }

    private final MyDriver driver;

    private boolean isMonthly = false;

    @UiField
    @Ignore
    ModalWindow title;

    @UiField
    @Ignore
    Label formDataKindLabel;

    @UiField
    @Ignore
    Label formTypeIdLabel;

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
    HorizontalPanel monthPanel;

    @UiField(provided = true)
    ValueListBox<Months> formMonth;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @Inject
    public CreateFormDataView(Binder uiBinder, final MyDriver driver, EventBus eventBus) {
        super(eventBus);

        formMonth = new ValueListBox<Months>(new AbstractRenderer<Months>() {
            @Override
            public String render(Months object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);
        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
        formTypeId.setPeriodDates(new Date(), new Date());
    }

    @Override
    public void init() {
        // Сброс состояния формы
        // убрал потому что при сеттинге нового бина это установка в нул и так будет
//        reportPeriodIds.setValue(null);
//        departmentPicker.setValue(null);
//        formDataKind.setValue(null);
//        formDataKind.setDereferenceValue(null);
//        //formTypeId.setValue(null);
//        formMonth.setValue(null);
        monthPanel.setVisible(false);
        updateEnabled();
    }

    private void updateEnabled() {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        formDataKind.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // "Вид налоговой формы" недоступен если не выбран тип НФ
        formTypeId.setEnabled(formDataKind.getValue() != null && !formDataKind.getValue().isEmpty());
        // "Месяц" недоступен если не выбран "Вид налоговой формы"
        formMonth.setEnabled(formTypeId.getValue() != null && !formTypeId.getValue().isEmpty() && isMonthly);
        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(formMonth.getValue() != null);
    }


    @UiHandler("reportPeriodIds")
    public void onReportPeriodChange(ValueChangeEvent<List<Integer>> event) {
        departmentPicker.setValue(null, true);
        if (getUiHandlers() != null) {
            getUiHandlers().onReportPeriodChange();
        }

        updateEnabled();
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
        if ((formDataKind.getValue() != null) && !formDataKind.getValue().isEmpty()) {
            getUiHandlers().onDataKindChanged();
        }
        if (formTypeId.getValue() != null) {
            formTypeId.setValue(null, true);
        }
        updateEnabled();
    }

    @UiHandler("formTypeId")
    public void onFormTypeIdChange(ValueChangeEvent<List<Long>> event) {
        formMonth.setValue(null);
        if (getUiHandlers() != null && formTypeId.getValue() != null && reportPeriodIds.getValue() != null) {
            getUiHandlers().isMonthly(formTypeId.getValue().get(0).intValue(), reportPeriodIds.getValue().get(0));
        }

        updateEnabled();
    }

    @UiHandler("continueButton")
    public void onSave(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onConfirm();
        }
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        Dialog.confirmMessage("Отмена создания", "Отменить создание?", new DialogHandler() {
            @Override
            public void yes() {
                Dialog.hideMessage();
                hide();
            }
        });
    }

    @UiHandler("formMonth")
    public void onChangeFormMonth(ValueChangeEvent<Months> event) {
        updateEnabled();
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
        if ((types == null) || types.isEmpty()) {
            /**
             * TODO продумать как сделать правильней,
             * на текущий момент синтаксис IN (..) не реализован в парсере фильтра,
             * так же нет варианта остановить подрузку на самом фронтенде
            */
            formTypeId.setFilter("2 = 1");
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
    }

    @Override
    public FormDataFilter getFilterData() {
        return driver.flush();
    }

    @Override
    public void setFilter(String filter) {
        formTypeId.setFilter(filter);
    }

    @Override
    public void setAcceptableMonthList(List<Months> monthList) {
        formMonth.setValue(null, true);
        formMonth.setAcceptableValues(monthList);

    }

    @Override
    public void setFormMonthEnabled(boolean isMonthly) {
        // Если ежемесячный, то устанавливается formMonth = true
        this.isMonthly = isMonthly;
        monthPanel.setVisible(isMonthly);
        formMonth.setEnabled(isMonthly);
        // Кнопка "Создать" пока неактивна
        if (isMonthly) {
            continueButton.setEnabled(false);
            // Иначе, кнопка активна
        } else {
            continueButton.setEnabled(true);
        }
    }

    @Override
    public void updateLabel() {
        boolean isTaxTypeDeal = getUiHandlers().getTaxType().equals(TaxType.DEAL);
        if (!isTaxTypeDeal) {
            formDataKindLabel.setText(FORM_DATA_KIND_TITLE);
            formTypeIdLabel.setText(FORM_DATA_TYPE_TITLE);
            title.setText(FORM_DATA_TITLE);
        } else {
            formDataKindLabel.setText(FORM_DATA_KIND_TITLE_D);
            formTypeIdLabel.setText(FORM_DATA_TYPE_TITLE_D);
            title.setText(FORM_DATA_TITLE_D);
        }
    }
}
