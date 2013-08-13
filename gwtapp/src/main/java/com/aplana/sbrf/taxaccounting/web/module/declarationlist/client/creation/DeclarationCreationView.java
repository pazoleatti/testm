package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;


public class DeclarationCreationView extends PopupViewWithUiHandlers<DeclarationCreationUiHandlers>
		implements DeclarationCreationPresenter.MyView, ReportPeriodSelectHandler {

	public interface Binder extends UiBinder<PopupPanel, DeclarationCreationView> {
	}

	private static final String DEPARTMENT_PICKER_HEADER = "Выбор подразделения";
	private DeclarationDataFilter filter;
	private DeclarationDataFilterAvailableValues filterValues;

	private final PopupPanel widget;
	private ReportPeriodPicker periodPicker;
	private DepartmentPickerPopupWidget departmentPicker;

	@UiField
	Panel reportPeriodPanel;

	@UiField
	Panel departmentPickerPanel;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

	@UiField(provided = true)
	ListBoxWithTooltip<DeclarationType> declarationType;

	@Inject
	public DeclarationCreationView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

		declarationType = new ListBoxWithTooltip<DeclarationType>(new AbstractRenderer<DeclarationType>() {
			@Override
			public String render(DeclarationType object) {
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
		declarationType.setValue(null);
		declarationType.setAcceptableValues(filterValues.getDeclarationTypes());

		if (filter.getDeclarationTypeId() != null) {
			for (DeclarationType availableType : filterValues.getDeclarationTypes()) {
				if (availableType.getId() == filter.getDeclarationTypeId()) {
					declarationType.setValue(availableType);
					return;
				}
			}
		}
	}

	@Override
	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		reportPeriodPanel.clear();
		periodPicker = new ReportPeriodPicker(this, false);
		periodPicker.setTaxPeriods(taxPeriods);
		reportPeriodPanel.add(periodPicker);
	}

	@Override
	public void setDepartments(List<Department> departments) {
		departmentPickerPanel.clear();
		departmentPicker = new DepartmentPickerPopupWidget(DEPARTMENT_PICKER_HEADER, false);
		departmentPicker.setAvalibleValues(departments, filterValues.getDepartmentIds());
		departmentPickerPanel.add(departmentPicker);

		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			Integer departmentId = filter.getDepartmentIds().iterator().next();
			for (Department department : departments) {
				if (department.getId() == departmentId) {
					departmentPicker.setValue(Arrays.asList(departmentId));
					return;
				}
			}
		}
	}

	@Override
	public void setCurrentReportPeriod(ReportPeriod reportPeriod) {
		periodPicker.setSelectedReportPeriods(Arrays.asList(reportPeriod));
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
		if (departmentPicker.getValue().iterator().hasNext() ) {
			filter.setDepartmentIds(departmentPicker.getValue().isEmpty() ? new ArrayList<Integer>() :
					Arrays.asList(departmentPicker.
							getValue().iterator().next()));
		}
		if(declarationType.getValue() != null) {
			filter.setDeclarationTypeId(declarationType.getValue().getId());
		}

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

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
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
