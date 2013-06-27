package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;


public class OpenDialogView extends PopupViewWithUiHandlers<OpenDialogUiHandlers>
		implements OpenDialogPresenter.MyView, ReportPeriodDataProvider {

	public interface Binder extends UiBinder<PopupPanel, OpenDialogView> {
	}

	private static final String DEPARTMENT_PICKER_HEADER = "Выбор подразделения";
	private DeclarationDataFilter filter;
	private DeclarationDataFilterAvailableValues filterValues;

	private final PopupPanel widget;
	private ReportPeriodPicker periodPicker;
//	private NewDepartmentPicker departmentPicker;

	@UiField
	NewDepartmentPicker departmentPicker;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

	@UiField(provided = true)
	ListBoxWithTooltip period;

//	@UiField(provided = true)
//	ListBoxWithTooltip<DeclarationType> declarationType;

	@Inject
	public OpenDialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

//		declarationType = new ListBoxWithTooltip<DeclarationType>(new AbstractRenderer<DeclarationType>() {
//			@Override
//			public String render(DeclarationType object) {
//				if (object == null) {
//					return "";
//				}
//				return object.getName();
//			}
//		});
		period = new ListBoxWithTooltip<DictionaryTaxPeriod>(new AbstractRenderer<DictionaryTaxPeriod>() {
			@Override
			public String render(DictionaryTaxPeriod object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		widget = uiBinder.createAndBindUi(this);
		widget.setAnimationEnabled(true);
	}

	@Override
	public void setDeclarationFilter(DeclarationDataFilter filter) {
		this.filter = filter;
	}

	@Override
	public void setDeclarationFilterValues(DeclarationDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;

		setDeclarationType();
	}

	private void setDeclarationType() {
//		declarationType.setValue(null);
//		declarationType.setAcceptableValues(filterValues.getDeclarationTypes());

		if (filter.getDeclarationTypeId() != null) {
			for (DeclarationType availableType : filterValues.getDeclarationTypes()) {
				if (availableType.getId() == filter.getDeclarationTypeId()) {
//					declarationType.setValue(availableType);
					return;
				}
			}
		}
	}

	@Override
	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		periodPicker = new ReportPeriodPicker(this, false);
		periodPicker.setTaxPeriods(taxPeriods);
	}

	@Override
	public void setDepartments(List<Department> departments) {

//		departmentPickerPanel.clear();
//		departmentPicker = new NewDepartmentPicker(DEPARTMENT_PICKER_HEADER, false);
		departmentPicker.setTreeValues(departments, new HashSet<Integer>());
//		departmentPickerPanel.add(departmentPicker);

//		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
//			Integer departmentId = filter.getDepartmentIds().iterator().next();
//			for (Department department : departments) {
//				if (department.getId() == departmentId) {
//					Map<String, Integer> value = new HashMap<String, Integer>(1);
//					value.put(department.getName(), departmentId);
//					departmentPicker.setSelectedItems(value);
//					return;
//				}
//			}
//		}
	}

	@Override
	public void setCurrentReportPeriod(ReportPeriod reportPeriod) {
		periodPicker.setSelectedReportPeriods(Arrays.asList(reportPeriod));
	}

	@Override
	public void setDictionaryTaxPeriod(List<DictionaryTaxPeriod> dictionaryTaxPeriod) {
		period.setAcceptableValues(dictionaryTaxPeriod);
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		periodPicker.setReportPeriods(reportPeriods);
	}

	@Override
	public DeclarationDataFilter updateAndGetDeclarationFilter() {
		if (periodPicker.getSelectedReportPeriods().entrySet().iterator().hasNext()) {
			filter.setReportPeriodIds(Arrays.asList(periodPicker.
					getSelectedReportPeriods().entrySet().iterator().next().getKey()));
		}
		if (departmentPicker.getSelectedItems().entrySet().iterator().hasNext() ) {
			filter.setDepartmentIds(departmentPicker.getSelectedItems().isEmpty() ? new ArrayList<Integer>() :
					Arrays.asList(departmentPicker.
							getSelectedItems().entrySet().iterator().next().getValue()));
		}
//		if(declarationType.getValue() != null) {
//			filter.setDeclarationTypeId(declarationType.getValue().getId());
//		}

		return filter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		getUiHandlers().onTaxPeriodSelected(taxPeriod);
	}

	@UiHandler("continueButton")
	public void onContinue(ClickEvent event) {
		getUiHandlers().onContinue();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}
}
