package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.gwt.client.*;
import com.aplana.gwt.client.TextBox;
import com.aplana.gwt.client.mask.ui.TextMaskBox;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private ListBoxWithTooltip<Integer> declarationTypePicker;

    private ValueListBox<WorkflowState> formStatePicker;

	private Map<Integer, String> declarationTypeMap;

    private TextBox taxOrganisationPicker;

    private TextBox kppPicker;

    private ValueListBox<Boolean> correctionTag;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder) {
        super();

        reportPeriodPicker = new PeriodPickerPopupWidget(true);

        departmentPicker = new DepartmentPickerPopupWidget(true);

        formStatePicker = new ValueListBox<WorkflowState>(new AbstractRenderer<WorkflowState>() {
            @Override
            public String render(WorkflowState object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });
        formStatePicker.setWidth("100%");

		declarationTypePicker = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return declarationTypeMap.get(object);
			}
		});
        declarationTypePicker.setShowTooltip(true);
        declarationTypePicker.setWidth("100%");

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
        correctionTag.addStyleName("correction");
        correctionTag.setWidth("200px");

        Date date = new Date();
        taxOrganisationPicker = new TextBox();
        taxOrganisationPicker.setMaxLength(4);
        taxOrganisationPicker.setTitle("Выбор налогового органа");

        kppPicker = new TextBox();
        kppPicker.setMaxLength(9);
        kppPicker.setTitle("Выбор КПП");

	    initWidget(binder.createAndBindUi(this));
    }

    @Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodPicker.setPeriods(reportPeriods);
	}

    @Override
    public void setFormStateList(List<WorkflowState> list){
        formStatePicker.setValue(null);
        formStatePicker.setAcceptableValues(list);
    }

    @Override
	public void setDataFilter(DeclarationDataFilter formDataFilter) {
        this.formDataFilter = formDataFilter;
        departmentPicker.setValue(formDataFilter.getDepartmentIds());
        reportPeriodPicker.setValue(formDataFilter.getReportPeriodIds());
        declarationTypePicker.setValue(formDataFilter.getDeclarationTypeId());
        formStatePicker.setValue(formDataFilter.getFormState());
        correctionTag.setValue(formDataFilter.getCorrectionTag());
    }

	@Override
	public DeclarationDataFilter getFilterData() {
        formDataFilter.setDepartmentIds(departmentPicker.getValue());
        formDataFilter.setReportPeriodIds(reportPeriodPicker.getValue());
        formDataFilter.setDeclarationTypeId(declarationTypePicker.getValue());
        formDataFilter.setFormState(formStatePicker.getValue());
        formDataFilter.setTaxOrganCode(taxOrganisationPicker.getValue());
        formDataFilter.setTaxOrganKpp(kppPicker.getValue());
        formDataFilter.setCorrectionTag(correctionTag.getValue());
		return formDataFilter;
	}

	@Override
	public void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments){
		departmentPicker.setAvalibleValues(list, availableDepartments);
	}

	@Override
	public void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap){
		this.declarationTypeMap = declarationTypeMap;
		declarationTypePicker.setValue(null);
		declarationTypePicker.setAcceptableValues(declarationTypeMap.keySet());
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
        style.setProperty("height", taxType == TaxType.DEAL ? 22 : 65, Style.Unit.PX);

        switch (taxType) {
            case DEAL:
                fillDeal();
                break;
            case PROPERTY:
                fillProperty();
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

    private void fillDeal() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        Label label =  new Label("Период:");
        horizontalPanel.add(label);
        horizontalPanel.add(reportPeriodPicker);
        horizontalPanel.add(correctionTag);
        label = new Label("Подразделение:");
        horizontalPanel.add(label);
        horizontalPanel.add(departmentPicker);
        label = new Label("Состояние:");
        horizontalPanel.add(label);
        horizontalPanel.add(formStatePicker);
        horizontalPanel.setCellWidth(departmentPicker, "25%");
        horizontalPanel.setCellWidth(reportPeriodPicker, "25%");
        horizontalPanel.setCellWidth(formStatePicker, "25%");
        panel.add(horizontalPanel);
    }

    private void fillProperty() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        VerticalPanel verticalPanel1 = new VerticalPanel();
        VerticalPanel verticalPanel2 = new VerticalPanel();
        VerticalPanel verticalPanel3 = new VerticalPanel();
        VerticalPanel verticalPanel4 = new VerticalPanel();
        VerticalPanel verticalPanel5 = new VerticalPanel();
        VerticalPanel verticalPanel6 = new VerticalPanel();
        VerticalPanel verticalPanel7 = new VerticalPanel();

        verticalPanel1.setWidth("100%");
        verticalPanel2.setWidth("100%");
        verticalPanel3.setWidth("100%");
        verticalPanel4.setWidth("100%");
        verticalPanel5.setWidth("100%");
        verticalPanel6.setWidth("100%");
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

        Label label = new Label("Период:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel1.add(label);
        label = new Label("Подразделение:");
        verticalPanel1.add(label);
        verticalPanel2.add(reportPeriodPicker);
        verticalPanel2.add(departmentPicker);
        verticalPanel3.add(correctionTag);
        label = new Label("Вид декларации:");
        label.setWordWrap(false);
        verticalPanel4.add(label);
        label = new Label("Состояние:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel4.add(label);
        verticalPanel5.add(declarationTypePicker);
        verticalPanel5.add(formStatePicker);
        label = new Label("Налоговый орган:");
        label.setWordWrap(false);
        verticalPanel6.add(label);
        label = new Label("КПП:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel6.add(label);
        verticalPanel7.add(taxOrganisationPicker);
        verticalPanel7.add(kppPicker);

        panel.add(horizontalPanel);
    }

    private void fillDefault() {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        VerticalPanel verticalPanel1 = new VerticalPanel();
        VerticalPanel verticalPanel2 = new VerticalPanel();
        VerticalPanel verticalPanel3 = new VerticalPanel();
        VerticalPanel verticalPanel4 = new VerticalPanel();
        VerticalPanel verticalPanel5 = new VerticalPanel();
        VerticalPanel verticalPanel6 = new VerticalPanel();
        VerticalPanel verticalPanel7 = new VerticalPanel();

        verticalPanel1.setWidth("100%");
        verticalPanel2.setWidth("100%");
        verticalPanel3.setWidth("100%");
        verticalPanel4.setWidth("100%");
        verticalPanel5.setWidth("100%");
        verticalPanel6.setWidth("100%");
        verticalPanel7.setWidth("100%");

        horizontalPanel.add(verticalPanel1);
        horizontalPanel.add(verticalPanel2);
        horizontalPanel.add(verticalPanel3);
        horizontalPanel.add(verticalPanel4);
        horizontalPanel.add(verticalPanel5);
        horizontalPanel.add(verticalPanel6);
        horizontalPanel.add(verticalPanel7);

        horizontalPanel.setCellWidth(verticalPanel2, "28%");
        horizontalPanel.setCellWidth(verticalPanel5, "44%");
        horizontalPanel.setCellWidth(verticalPanel7, "28%");

        Label label = new Label("Период:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel1.add(label);
        label = new Label("Подразделение:");
        verticalPanel1.add(label);
        verticalPanel2.add(reportPeriodPicker);
        verticalPanel2.add(departmentPicker);
        verticalPanel3.add(correctionTag);
        label = new Label("Вид декларации:");
        label.setWordWrap(false);
        verticalPanel4.add(label);
        verticalPanel5.add(declarationTypePicker);
        label = new Label("Состояние:");
        label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel6.add(label);
        verticalPanel7.add(formStatePicker);

        panel.add(horizontalPanel);
    }

    @Override
    public void clean() {
        formStatePicker.setValue(null);
        declarationTypePicker.setValue(null);
        reportPeriodPicker.setValue(null);
        departmentPicker.setValue(null);
        if (getUiHandlers() != null) {
            getUiHandlers().onApplyFilter();
        }
    }
}
