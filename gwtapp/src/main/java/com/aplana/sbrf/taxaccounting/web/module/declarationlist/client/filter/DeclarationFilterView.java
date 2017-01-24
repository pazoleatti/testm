package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.TextBox;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class DeclarationFilterView extends ViewWithUiHandlers<DeclarationFilterUIHandlers>
        implements DeclarationFilterPresenter.MyView {

    private DeclarationDataFilter formDataFilter;

    interface MyBinder extends UiBinder<Widget, DeclarationFilterView> {}

    @UiField
    HTMLPanel panel;

    @UiField
    HTML separator;

    private PeriodPickerPopupWidget reportPeriodPicker;

    private DepartmentPickerPopupWidget departmentPicker;

    private RefBookPickerWidget declarationTypePicker;

    private ValueListBox<State> formStatePicker;

    private RefBookPickerWidget declarationKindPicker;

    private TextBox taxOrganisationPicker;

    private TextBox kppPicker;

    private TextBox fileNamePicker;

    private ValueListBox<Boolean> correctionTag;

    private RefBookPickerWidget asnuPicker;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder) {
        super();

        reportPeriodPicker = new PeriodPickerPopupWidget(true);
        reportPeriodPicker.setSetDefaultValue(true);

        departmentPicker = new DepartmentPickerPopupWidget(true);
        departmentPicker.setSetDefaultValue(true);

        formStatePicker = new ValueListBox<State>(new AbstractRenderer<State>() {
            @Override
            public String render(State object) {
                if (object == null) {
                    return "";
                }
                return object.getTitle();
            }
        });
        formStatePicker.setWidth("100%");

        declarationKindPicker = new RefBookPickerWidget(false, false);
        declarationKindPicker.setVersionEnabled(false);
        declarationKindPicker.setAttributeId(9321L);
        declarationKindPicker.setWidth("100%");
        declarationKindPicker.setPeriodDates(new Date(), new Date());
        declarationKindPicker.setManualUpdate(true);
        declarationKindPicker.setMultiSelect(true);

        declarationTypePicker = new RefBookPickerWidget(false, false);
        declarationTypePicker.setVersionEnabled(false);
        declarationTypePicker.setAttributeId(2071L);
        declarationTypePicker.setWidth("100%");
        declarationTypePicker.setPeriodDates(new Date(), new Date());
        declarationTypePicker.setManualUpdate(true);
        declarationTypePicker.setMultiSelect(true);

        asnuPicker = new RefBookPickerWidget(false, false);
        asnuPicker.setVersionEnabled(false);
        asnuPicker.setAttributeId(9003L);
        asnuPicker.setWidth("100%");
        asnuPicker.setPeriodDates(new Date(), new Date());
        asnuPicker.setManualUpdate(true);

        correctionTag = new ListBoxWithTooltip<Boolean>(new AbstractRenderer<Boolean>() {
            @Override
            public String render(Boolean object) {
                if (object == Boolean.TRUE) {
                    return "Только корректирующие";
                } else if (object == Boolean.FALSE) {
                    return "Только не корректирующие";
                } else {
                    return "Все периоды";
                }
            }
        });
        correctionTag.addStyleName("l-lp");
        correctionTag.setWidth("200px");

        formStatePicker.setWidth("100px");

        taxOrganisationPicker = new TextBox();
        taxOrganisationPicker.setMaxLength(4);
        taxOrganisationPicker.setTitle("Выбор налогового органа");

        kppPicker = new TextBox();
        kppPicker.setMaxLength(9);
        kppPicker.setTitle("Выбор КПП");

        fileNamePicker = new TextBox();
        fileNamePicker.setMaxLength(255);
        fileNamePicker.setTitle("Файл");

        initWidget(binder.createAndBindUi(this));
    }

    @Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodPicker.setPeriods(reportPeriods);
	}

    @Override
    public void setFormStateList(List<State> list){
        formStatePicker.setValue(null);
        formStatePicker.setAcceptableValues(list);
    }

    @Override
	public void setDataFilter(DeclarationDataFilter formDataFilter) {
        this.formDataFilter = formDataFilter;
        departmentPicker.setValue(formDataFilter.getDepartmentIds());
        reportPeriodPicker.setValue(formDataFilter.getReportPeriodIds());
        if (formDataFilter.getDeclarationTypeIds() != null) {
            declarationTypePicker.setValue(formDataFilter.getDeclarationTypeIds());
        } else {
            declarationTypePicker.setValue(null);
        }
        formStatePicker.setValue(formDataFilter.getFormState());
        correctionTag.setValue(formDataFilter.getCorrectionTag());
    }

	@Override
	public DeclarationDataFilter getFilterData() {
        formDataFilter.setDepartmentIds(departmentPicker.getValue());
        formDataFilter.setReportPeriodIds(reportPeriodPicker.getValue());
        List<Long> values = declarationTypePicker.getValue();
        if (values != null && !values.isEmpty()) {
            formDataFilter.setDeclarationTypeIds(values);
        } else {
            formDataFilter.setDeclarationTypeIds(null);
        }
        formDataFilter.setFormState(formStatePicker.getValue());
        formDataFilter.setTaxOrganCode(taxOrganisationPicker.getValue());
        formDataFilter.setTaxOrganKpp(kppPicker.getValue());
        formDataFilter.setCorrectionTag(correctionTag.getValue());
        List<Long> asnuPickerValues = asnuPicker.getValue();
        if (asnuPickerValues != null && !asnuPickerValues.isEmpty()) {
            formDataFilter.setAsnuId(asnuPickerValues.get(0));
        } else {
            formDataFilter.setAsnuId(null);
        }
        List<Long> kindPickerValues = declarationKindPicker.getValue();
        if (kindPickerValues != null && !kindPickerValues.isEmpty()) {
            formDataFilter.setFormKindIds(kindPickerValues);
        } else {
            formDataFilter.setFormKindIds(null);
        }
        formDataFilter.setFileName(fileNamePicker.getValue());
		return formDataFilter;
	}

	@Override
	public void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments){
		departmentPicker.setAvalibleValues(list, availableDepartments);
	}

	@Override
	public void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap){
        declarationTypePicker.setValue(null);
        if ((declarationTypeMap == null) || declarationTypeMap.isEmpty()) {
            /**
             * TODO продумать как сделать правильней,
             * на текущий момент синтаксис IN (..) не реализован в парсере фильтра,
             * так же нет варианта остановить подрузку на самом фронтенде
             */
            declarationTypePicker.setFilter("2 = 1");
            return;
        }
        StringBuilder str = new StringBuilder();
        for (Integer dtId : declarationTypeMap.keySet()) {
            if (dtId != null) str.append(RefBook.RECORD_ID_ALIAS + "=" + dtId + " or ");
        }
        str.delete(str.length() - 3, str.length() - 1);
        declarationTypePicker.setFilter(str.toString());
	}

    @Override
    public void setKindFilter(List<DeclarationFormKind> dataKinds) {
        List<String> list = new ArrayList<String>(dataKinds.size());

        for (DeclarationFormKind kind : dataKinds) {
            list.add("record_id = "+kind.getId());
        }
        declarationKindPicker.setFilter(StringUtils.join(list.toArray(), " or ", null));
    }

    @UiHandler("apply")
	void onApplyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyFilter();
		}
	}

    @Override
    public void updateFilter(TaxType taxType) {
        // http://conf.aplana.com/pages/viewpage.action?pageId=11383562
        panel.clear();
        // Верстка по-умолчанию
        if (taxType == null) {
            taxType = TaxType.INCOME;
        }

        Style style = separator.getElement().getStyle();
        style.setProperty("height", (taxType == TaxType.PFR || taxType == TaxType.NDFL) ? (65 + 22) : 22, Style.Unit.PX);

        switch (taxType) {
            case DEAL:
                fillDeal();
                break;
            case PROPERTY:
            case TRANSPORT:
            case LAND:
                fillTransportAndPropertyAndLand(taxType);
                break;
            case INCOME:
                fillIncome();
                break;
            case NDFL:
            case PFR:
                fillNdfl();
                break;
            default:
                fillDefault();
                break;
        }
    }

    @Override
    public void setCorrectionTagList(List<Boolean> list) {
        correctionTag.setValue(null);
        correctionTag.setAcceptableValues(list);
    }

    private Label getLabel(String text) {
        return getLabel(text, true);
    }

    private Label getLabel(String text, boolean leftPadding) {
        Label label = new Label(text);
        if (leftPadding) {
            label.addStyleName("l-lp");
        }
        label.addStyleName("l-rp");
        label.setWordWrap(false);
        return label;
    }

    private void fillDeal() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");

        Label label = getLabel("Период:", false);
        horizontalPanel.add(label);
        horizontalPanel.add(reportPeriodPicker);
        horizontalPanel.add(correctionTag);

        label = getLabel("Вид уведомления:");

        horizontalPanel.add(label);
        horizontalPanel.add(declarationTypePicker);

        label = getLabel("Подразделение:");
        horizontalPanel.add(label);
        horizontalPanel.add(departmentPicker);

        label = getLabel("Состояние:");
        horizontalPanel.add(label);
        horizontalPanel.add(formStatePicker);

        horizontalPanel.setCellWidth(departmentPicker, "30%");
        horizontalPanel.setCellWidth(reportPeriodPicker, "30%");
        horizontalPanel.setCellWidth(declarationTypePicker, "30%");
        panel.add(horizontalPanel);
    }

    private void fillTransportAndPropertyAndLand(TaxType taxType) {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        VerticalPanel verticalPanel1 = new VerticalPanel();
        VerticalPanel verticalPanel2 = new VerticalPanel();
        VerticalPanel verticalPanel3 = new VerticalPanel();
        VerticalPanel verticalPanel4 = new VerticalPanel();
        VerticalPanel verticalPanel5 = new VerticalPanel();
        VerticalPanel verticalPanel6 = new VerticalPanel();
        VerticalPanel verticalPanel7 = new VerticalPanel();

        verticalPanel2.setWidth("100%");
        verticalPanel3.setWidth("100%");
        verticalPanel5.setWidth("100%");
        verticalPanel7.setWidth("100%");

        horizontalPanel.add(verticalPanel1);
        horizontalPanel.add(verticalPanel2);
        horizontalPanel.add(verticalPanel3);
        horizontalPanel.add(verticalPanel4);
        horizontalPanel.add(verticalPanel5);
        horizontalPanel.add(verticalPanel6);
        horizontalPanel.add(verticalPanel7);

        horizontalPanel.setCellWidth(verticalPanel2, "33%");
        horizontalPanel.setCellWidth(verticalPanel5, "33%");
        horizontalPanel.setCellWidth(verticalPanel7, "33%");

        Label label = getLabel("Период:", false);
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel1.add(label);

        label = getLabel("Подразделение:", false);
        verticalPanel1.add(label);
        verticalPanel2.add(reportPeriodPicker);
        verticalPanel2.add(departmentPicker);
        verticalPanel3.add(correctionTag);

        label = getLabel("Вид декларации:");
        verticalPanel4.add(label);

        label = getLabel("Состояние:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel4.add(label);
        verticalPanel5.add(declarationTypePicker);
        verticalPanel5.add(formStatePicker);


        if (taxType == TaxType.TRANSPORT) {
            label = getLabel("Налоговый орган (кон.):");
        } else if (taxType == TaxType.LAND) {
            label = getLabel("Код налогового органа (кон.):");
        } else {
            label = getLabel("Налоговый орган:");
        }
        verticalPanel6.add(label);

        label = getLabel("КПП:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel6.add(label);
        verticalPanel7.add(taxOrganisationPicker);
        verticalPanel7.add(kppPicker);

        panel.add(horizontalPanel);
    }

    private void fillDefault() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");

        Label label = getLabel("Период:", false);
        horizontalPanel.add(label);
        horizontalPanel.add(reportPeriodPicker);
        horizontalPanel.add(correctionTag);

        label = getLabel("Подразделение:");
        horizontalPanel.add(label);
        horizontalPanel.add(departmentPicker);

        label = getLabel("Вид налоговой формы:");

        horizontalPanel.add(label);
        horizontalPanel.add(declarationTypePicker);

        label = getLabel("Состояние:");
        horizontalPanel.add(label);
        horizontalPanel.add(formStatePicker);

        horizontalPanel.setCellWidth(departmentPicker, "30%");
        horizontalPanel.setCellWidth(reportPeriodPicker, "30%");
        horizontalPanel.setCellWidth(declarationTypePicker, "30%");
        panel.add(horizontalPanel);
    }

    private void fillIncome() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        VerticalPanel verticalPanel1 = new VerticalPanel();
        VerticalPanel verticalPanel2 = new VerticalPanel();
        VerticalPanel verticalPanel3 = new VerticalPanel();
        VerticalPanel verticalPanel4 = new VerticalPanel();
        VerticalPanel verticalPanel5 = new VerticalPanel();
        VerticalPanel verticalPanel6 = new VerticalPanel();
        VerticalPanel verticalPanel7 = new VerticalPanel();

        verticalPanel2.setWidth("100%");
        verticalPanel3.setWidth("100%");
        verticalPanel5.setWidth("100%");
        verticalPanel7.setWidth("100%");

        horizontalPanel.add(verticalPanel1);
        horizontalPanel.add(verticalPanel2);
        horizontalPanel.add(verticalPanel3);
        horizontalPanel.add(verticalPanel4);
        horizontalPanel.add(verticalPanel5);
        horizontalPanel.add(verticalPanel6);
        horizontalPanel.add(verticalPanel7);

        horizontalPanel.setCellWidth(verticalPanel2, "33%");
        horizontalPanel.setCellWidth(verticalPanel5, "33%");
        horizontalPanel.setCellWidth(verticalPanel7, "33%");

        Label label = getLabel("Период:", false);
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel1.add(label);

        label = getLabel("Подразделение:", false);
        verticalPanel1.add(label);
        verticalPanel2.add(reportPeriodPicker);
        verticalPanel2.add(departmentPicker);
        verticalPanel3.add(correctionTag);

        label = getLabel("Вид налоговой формы:");
        verticalPanel4.add(label);

        label = getLabel("Состояние:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel4.add(label);
        verticalPanel5.add(declarationTypePicker);
        verticalPanel5.add(formStatePicker);

        label = getLabel("КПП:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel6.add(label);
        verticalPanel7.add(kppPicker);

        panel.add(horizontalPanel);
    }

    private void fillNdfl() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        VerticalPanel verticalPanel1 = new VerticalPanel();
        VerticalPanel verticalPanel2 = new VerticalPanel();
        VerticalPanel verticalPanel3 = new VerticalPanel();
        VerticalPanel verticalPanel4 = new VerticalPanel();
        VerticalPanel verticalPanel5 = new VerticalPanel();
        VerticalPanel verticalPanel6 = new VerticalPanel();
        VerticalPanel verticalPanel7 = new VerticalPanel();

        verticalPanel2.setWidth("100%");
        verticalPanel3.setWidth("100%");
        verticalPanel5.setWidth("100%");
        verticalPanel7.setWidth("100%");

        horizontalPanel.add(verticalPanel1);
        horizontalPanel.add(verticalPanel2);
        horizontalPanel.add(verticalPanel3);
        horizontalPanel.add(verticalPanel4);
        horizontalPanel.add(verticalPanel5);
        horizontalPanel.add(verticalPanel6);
        horizontalPanel.add(verticalPanel7);

        horizontalPanel.setCellWidth(verticalPanel2, "33%");
        horizontalPanel.setCellWidth(verticalPanel5, "33%");
        horizontalPanel.setCellWidth(verticalPanel7, "33%");

        Label label = getLabel("Период:", false);
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel1.add(label);

        label = getLabel("Подразделение:", false);
        verticalPanel1.add(label);
        verticalPanel2.add(reportPeriodPicker);
        verticalPanel2.add(departmentPicker);
        verticalPanel3.add(correctionTag);

        label = getLabel("Тид налоговой формы:");
        //label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel4.add(label);
        label = getLabel("Вид налоговой формы:");
        //label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel4.add(label);
        label = getLabel("Состояние:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel4.add(label);

        verticalPanel5.add(declarationKindPicker);
        verticalPanel5.add(declarationTypePicker);
        verticalPanel5.add(formStatePicker);

        label = getLabel("АСНУ:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel6.add(label);
        verticalPanel7.add(asnuPicker);

        label = getLabel("Файл:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel6.add(label);
        verticalPanel7.add(fileNamePicker);

        panel.add(horizontalPanel);
    }

    @Override
    public void clean() {
        formStatePicker.setValue(null);
        declarationTypePicker.setValue(null);
        reportPeriodPicker.setValue(null);
        departmentPicker.setValue(null);
        asnuPicker.setValue(null);
        if (getUiHandlers() != null) {
            getUiHandlers().onApplyFilter();
        }
    }

    @Override
    public void clearFilter() {
        kppPicker.setValue("");
        taxOrganisationPicker.setValue("");
        fileNamePicker.setValue("");
    }

    @Override
    public void setReportPeriodType(String type) {
        reportPeriodPicker.setType(type);
    }

    @Override
    public void setUserDepartmentId(Integer userDepartmentId) {
        departmentPicker.setUserDepartmentId(userDepartmentId);
    }
}
