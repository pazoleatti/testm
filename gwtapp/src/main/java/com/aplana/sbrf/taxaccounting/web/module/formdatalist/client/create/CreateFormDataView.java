package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.ValueListBox;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FormDataElementName;
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
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class CreateFormDataView extends PopupViewWithUiHandlers<CreateFormDataUiHandlers> implements CreateFormDataPresenter.MyView,
        Editor<FormDataFilter> {

    public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы:";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы:";
    public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы:";
    public static final String FORM_DATA_TYPE_TITLE_D = "Вид формы:";
    public static final String FORM_DATA_TITLE = "Создание налоговой формы";
    public static final String FORM_DATA_TITLE_D = "Создание формы";
    public static final String FORM_DATA_CORRECTION = "Форма будет создана в корректирующем периоде, дата сдачи корректировки: ";

    public interface Binder extends UiBinder<PopupPanel, CreateFormDataView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, CreateFormDataView> {
    }

    private final MyDriver driver;

    /** Признак ежемесячной формы */
    private boolean isMonthly = false;
    /** признак формы с периодом сравнения */
    private boolean comparative = false;
    /** Признак расчета нарастающим итогом */
    private boolean isAccruing = false;

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
    HorizontalPanel monthPanel, correctionPanel, comparativPeriodPanel, accruingPanel;

    @UiField
    @Ignore
    Label correctionDate;

    @UiField(provided = true)
    ValueListBox<Months> formMonth;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @UiField
    PeriodPickerPopupWidget comparativePeriodId;

    @UiField
    CheckBox accruing;

    @Inject
    public CreateFormDataView(Binder uiBinder, final MyDriver driver, EventBus eventBus) {
        super(eventBus);

        formMonth = new ValueListBox<Months>(new AbstractRenderer<Months>() {
            @Override
            public String render(Months object) {
                if (object == null) {
                    return "";
                }
                return object.getTitle();
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
        monthPanel.setVisible(false);
        correctionPanel.setVisible(false);
        updateEnabled();
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

    @Override
    public void updateEnabled() {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        formDataKind.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // "Вид налоговой формы" недоступен если не выбран тип НФ
        formTypeId.setEnabled(formDataKind.getValue() != null && !formDataKind.getValue().isEmpty());
        // "Месяц" недоступен если не выбран "Вид налоговой формы"
        formMonth.setEnabled(formTypeId.getValue() != null && !formTypeId.getValue().isEmpty() && isMonthly);
        // "Месяц" только для ежемесячных форм
        monthPanel.setVisible(formTypeId.getValue() != null && !formTypeId.getValue().isEmpty() && isMonthly);
        // дата корректировки
        correctionPanel.setVisible(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty() &&
                correctionDate.getText() != null && !correctionDate.getText().isEmpty());
        // Период сравнения и признак нарастающих итогов доступен только для вида формы с признаком использования двух периодов
        comparativPeriodPanel.setVisible(formTypeId.getValue() != null && !formTypeId.getValue().isEmpty() && comparative);
        accruingPanel.setVisible(formTypeId.getValue() != null && !formTypeId.getValue().isEmpty() && isAccruing);

        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(
                (formTypeId.getValue() != null && !formTypeId.getValue().isEmpty()) &&
                (!isMonthly || formMonth.getValue() != null) && (
                        //Если нф с периодом сравнения, то он должен быть заполнен
                        !comparative || (
                                comparativePeriodId.getValue() != null && !comparativePeriodId.getValue().isEmpty())
                        )
        );
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

    @UiHandler("comparativePeriodId")
    public void onComparativePeriodIdChange(ValueChangeEvent<List<Integer>> event) {
        if (isAccruing) {
            getUiHandlers().checkFormType(formTypeId.getValue().get(0).intValue(), reportPeriodIds.getValue().get(0), (comparativePeriodId.getValue() != null && !comparativePeriodId.getValue().isEmpty()) ? comparativePeriodId.getValue().get(0) : null);
        } else {
            updateEnabled();
        }
    }

    @UiHandler("formTypeId")
    public void onFormTypeIdChange(ValueChangeEvent<List<Long>> event) {
        formMonth.setValue(null);
        if (getUiHandlers() != null && formTypeId.getValue() != null && !formTypeId.getValue().isEmpty() && reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty()) {
            comparativePeriodId.setValue(null);
            accruing.setValue(false);
            getUiHandlers().checkFormType(formTypeId.getValue().get(0).intValue(), reportPeriodIds.getValue().get(0), null);
        } else {
            updateEnabled();
        }
    }

    @UiHandler("continueButton")
    public void onSave(ClickEvent event) {
        if (getUiHandlers() != null) {
            if (isMonthly && formMonth.getValue() == null) {
                Dialog.errorMessage("Ошибка", "Не задан месяц!");
                return;
            }
            if (comparative && (comparativePeriodId.getValue() == null || comparativePeriodId.getValue().isEmpty())) {
                Dialog.errorMessage("Ошибка", "Не задан период сравнения!");
                return;
            }
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
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods, ReportPeriod reportPeriod) {
        reportPeriodIds.setPeriods(reportPeriods);
        if (reportPeriod != null)  {
            reportPeriodIds.setValue(Arrays.asList(reportPeriod.getId()));
        }
    }

    @Override
    public Integer getDefaultReportPeriodId() {
        return reportPeriodIds.getDefaultReportPeriod();
    }

    @Override
    public void setReportPeriodType(String type) {
        reportPeriodIds.setType(type);
    }

    @Override
    public void setAcceptableComparativePeriods(List<ReportPeriod> comparativPeriods) {
        comparativePeriodId.setPeriods(comparativPeriods);
    }

    @Override
	public void setAcceptableKinds(List<FormDataKind> dataKinds) {
        if (dataKinds == null) {
            formDataKind.setFilter(null);
            return;
        }
        if (dataKinds.isEmpty()) {
            formDataKind.setFilter("2 = 1");
            return;
        }
        List<String> list = new ArrayList<String>(dataKinds.size());

        for (FormDataKind kind : dataKinds) {
            list.add("record_id = " + kind.getId());
        }
        formDataKind.setFilter(StringUtils.join(list.toArray(), " or ", null));
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
    public void setCorrectionDate(String correctionDate) {
        correctionPanel.setVisible(correctionDate != null);
        this.correctionDate.setText((correctionDate != null) ? (FORM_DATA_CORRECTION + correctionDate) : "");
    }

    @Override
    public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues, Integer departmentId) {
        departmentPicker.setAvalibleValues(list, availableValues);
        if (departmentId != null) {
            departmentPicker.setValue(Arrays.asList(departmentId));
        }
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
    }

    @Override
    public void setComparative(boolean comparative) {
        this.comparative = comparative;
        comparativPeriodPanel.setVisible(comparative);
        accruingPanel.setVisible(comparative);
    }

    @Override
    public void setAccruing(boolean accruing, boolean enabled) {
        isAccruing = accruing;
        if (isAccruing) {
            this.accruing.setEnabled(enabled);
            if (!enabled) {
                this.accruing.setValue(false);
            }
        }
    }

    @Override
    public void updateLabel() {
        TaxType taxType = getUiHandlers().getTaxType();
        if (taxType.isTax()) {
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
